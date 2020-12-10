package hu.konczdam.codefun.service

import hu.konczdam.codefun.controller.RoomController
import hu.konczdam.codefun.converter.toDto
import hu.konczdam.codefun.dataacces.RoomUpdateDto
import hu.konczdam.codefun.dataacces.TestCaseExecuteDTO
import hu.konczdam.codefun.dataacces.UserDto
import hu.konczdam.codefun.dataacces.UserUpdateDto
import hu.konczdam.codefun.docker.ParseResponse
import hu.konczdam.codefun.docker.service.CodeExecutorManagerService
import hu.konczdam.codefun.model.GameType
import hu.konczdam.codefun.model.Language
import hu.konczdam.codefun.model.Message
import hu.konczdam.codefun.model.Room
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
class RoomService {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var challengeService: ChallengeService

    @Autowired
    private lateinit var outgoing: SimpMessagingTemplate

    @Autowired
    private lateinit var codeExecutorManagerService: CodeExecutorManagerService

    private val roomList: MutableList<Room> = mutableListOf()

    private val roomCloseJobs: MutableMap<Long, Job> = mutableMapOf()

    fun addRoom(ownerId: Long, description: String): Room {
        val owner = userService.getUserById(ownerId)
        if (getRoomList().any { it.owner.id == ownerId} ) {
            throw Exception("User already has a room!")
        }
        val newRoom = Room(owner = owner.toDto(), description = description )
        addRoom(newRoom)
        return newRoom
    }

    fun removeRoom(ownerId: Long, roomId: Long) {
        if (ownerId != roomId) {
            throw Exception("A user can only delete its own room!")
        }
        val roomToDelete = getRoomList().first { it.owner.id == ownerId }
        removeRoom(roomToDelete)
    }

    fun usersInChatRoom(ownerId: Long): List<UserDto> {
        val room = getRoomList().first { it.owner.id == ownerId }
        val result = mutableListOf(room.owner)
        result.addAll(room.others)
        return result
    }

    fun addUserToRoom(userId: Long, ownerId: Long): List<UserDto> {
        val newUser = userService.getUserById(userId)
        val oldRoom = getRoomList().first { it.owner.id == ownerId }
        val newRoom = oldRoom.subscribe(newUser.toDto())
        updateRoom(oldRoom, newRoom)
        return usersInChatRoom(ownerId)
    }

    fun removeUserFromRoom(userId: Long, ownerId: Long): List<UserDto> {
        val oldUser = userService.getUserById(userId)
        val oldRoom = getRoomList().first { it.owner.id == ownerId }
        val oldUserDto = oldUser.toDto()
        val newRoom = oldRoom.unSubscribe(oldUserDto)
        updateRoom(oldRoom, newRoom)
        return usersInChatRoom(ownerId)
    }

    fun getRoomList(): List<Room> {
        synchronized(this) {
            return roomList
        }
    }

    fun addMessage(roomId: String, message: Message): List<Message> {
        val roomToAddMessage = getRoomList().first { it.owner.id == roomId.toLong() }
        synchronized(roomToAddMessage) {
            roomToAddMessage.messages.add(message)
            return roomToAddMessage.messages
        }
    }

    fun getMessagesFromRoom(roomId: Long): List<Message> {
        return getRoomList().first { it.owner.id == roomId }.messages
    }

    private fun addRoom(room: Room) {
        synchronized(this) {
            roomList.add(room)
        }
    }

    private fun removeRoom(room: Room) {
        synchronized(this) {
            roomList.remove(room)
        }
    }

    private fun updateRoom(oldRoom: Room, newRoom: Room): List<Room> {
        synchronized(this) {
            this.roomList.remove(oldRoom)
            this.roomList.add(newRoom)
            return roomList
        }
    }

    fun setGameType(roomId: Long, gameType: String): GameType {
        val result = GameType.valueOf(gameType)
        val room = getRoomList().first { it.owner.id == roomId}
        synchronized(this) {
            room.gameType = result
        }
        return result
    }

    fun setOnlyFriendsAllowed(roomId: Long, friendOnly: Boolean) {
        val room = getRoomList().first { it.owner.id == roomId }
        synchronized(this) {
            room.onlyFriends = friendOnly
        }
        val roomUpdateDto = RoomUpdateDto(roomId = roomId, friendsOnly = friendOnly)
        outgoing.convertAndSend("${RoomController.TOPIC_PREFIX}/updateRoom", roomUpdateDto)
    }

    fun startGame(roomId: Long): Room {
        val room = getRoomList().first { it.owner.id == roomId }
        val randomChallenge = challengeService.getRandomChallenge()
        synchronized(this) {
            with(room) {
                gameStarted = true
                gameStartedDate = Date()
                challenge = randomChallenge
            }
        }

        val job: Job = GlobalScope.launch(context = Dispatchers.Default) {
            delay((15 * 60 + 4) * 1000)
            if (isActive) {
                endGame(roomId)
            }
        }
        roomCloseJobs.plusAssign(roomId to job)
        return room
    }

    fun endGame(roomId: Long) {
        val room = getRoomList().first { it.owner.id == roomId }
        val peopleInTheRoomOrdered = determineFinalOrderOfPlayer(
                players = room.others + room.owner,
                gameType = room.gameType
        )

        outgoing.convertAndSend(
                "${RoomController.TOPIC_PREFIX}/$roomId/gameEnd",
                peopleInTheRoomOrdered.map { it.id }
        )

        val winner = peopleInTheRoomOrdered[0]
        userService.incrementGamesPlayedAndGamesWon(winner.id)
        userService.incrementGamesPlayed( peopleInTheRoomOrdered.drop(1).map { it.id })
        removeRoom(room)

    }

    fun determineFinalOrderOfPlayer(
            players: List<UserDto>,
            gameType: GameType
    ): List<UserDto> {
        return when (gameType) {
            GameType.NORMAL ->
                players.sortedWith(compareByDescending<UserDto> { it.successRate }.thenBy { it.timeTaken })


            GameType.CODE_GOLF ->
                players.sortedWith(compareByDescending<UserDto> { it.successRate }.thenBy { it.finalCodeLength })
        }
    }

    fun executeCodeForUser(
            roomId: Long,
            userId: Long,
            testCaseExecuteDTO: TestCaseExecuteDTO
    ): ParseResponse {
        val dataOfSubmitting = Date()
        val room = getRoomList().first { it.owner.id == roomId }
        val user = (room.others + room.owner).first { it.id == userId }
        val language = Language.valueOf(testCaseExecuteDTO.language)

        var userUpdateDto = UserUpdateDto(
                userId = userId,
                successRate = user.successRate,
                submitted = testCaseExecuteDTO.submitted,
                status = if (testCaseExecuteDTO.submitted) "submitting" else "Running testcases",
                language = language.name
        )

        outgoing.convertAndSend("${RoomController.TOPIC_PREFIX}/$roomId/userUpdate", userUpdateDto)


        val codeRunResponse = when (language) {
            Language.JAVA -> codeExecutorManagerService.executeJavaCode(
                    challengeId =  testCaseExecuteDTO.challengeId.toLong(),
                    code = testCaseExecuteDTO.code,
                    testIds = testCaseExecuteDTO.testIds.map { it.toLong() }
            )
            else -> throw UnsupportedOperationException("this language is not supported yet: $language")
        }


        if (testCaseExecuteDTO.submitted) {
            val newSuccessRate: Float = if ( testCaseExecuteDTO.testIds.size == 1) {
                user.successRate
            } else {
                codeRunResponse.testResults.filter { it.passed }.count().toFloat() /
                        testCaseExecuteDTO.testIds.size
            }
            val timeTaken = dataOfSubmitting.time - room.gameStartedDate!!.time
            userUpdateDto = userUpdateDto.copy(
                    successRate =  newSuccessRate,
                    status = "submitted",
                    submitted = true,
                    finalCodeLength = testCaseExecuteDTO.code.length,
                    timeTaken = timeTaken.toInt(), // time taken to solve the problem
                    code = testCaseExecuteDTO.code,
                    runTime = codeRunResponse.timeTaken // runtime
            )
            user.apply {
                submitted = true
                finalCodeLength = testCaseExecuteDTO.code.length
                this.timeTaken = timeTaken
                successRate = newSuccessRate
            }
            checkEverybodySubmitted(room)
        } else {
            userUpdateDto = userUpdateDto.copy( status = "coding...")
        }
        outgoing.convertAndSend("${RoomController.TOPIC_PREFIX}/$roomId/userUpdate", userUpdateDto)
        return codeRunResponse
    }

    private fun checkEverybodySubmitted(room: Room) {
        val usersInRoom = room.others + room.owner
        if (usersInRoom.all { it.submitted }) {
            val job = roomCloseJobs[room.owner.id]
            GlobalScope.launch {
                job?.cancelAndJoin()
            }

            endGame(room.owner.id)
        }
    }

    fun removeUserFromGame(userId: Long) {
        val rooms = getRoomList()
        val roomOwnedByUser = rooms.filter { it.owner.id.equals(userId) }.firstOrNull()
        if (roomOwnedByUser != null) {
            removeRoom(userId, userId)
            outgoing.convertAndSend("${RoomController.TOPIC_PREFIX}/roomClosed", userId)
            return
        }

        val roomUserIsIn = rooms.filter { it.others.any { user -> user.id.equals(userId)}}.firstOrNull()
        if (roomUserIsIn != null) {
            val roomId = roomUserIsIn.owner.id
            val usersLeftInRoom = removeUserFromRoom(userId, roomId)
            outgoing.convertAndSend("${RoomController.TOPIC_PREFIX}/updateRoom", usersLeftInRoom)
        }
    }

}