package hu.konczdam.codefun.service

import hu.konczdam.codefun.controller.RoomController
import hu.konczdam.codefun.converter.toDto
import hu.konczdam.codefun.dataacces.TestCaseExecuteDTO
import hu.konczdam.codefun.dataacces.UserDto
import hu.konczdam.codefun.dataacces.UserUpdateDto
import hu.konczdam.codefun.docker.ParseResponse
import hu.konczdam.codefun.docker.service.CodeExecutorManagerService
import hu.konczdam.codefun.model.GameType
import hu.konczdam.codefun.model.Message
import hu.konczdam.codefun.model.Room
import hu.konczdam.codefun.model.User
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
        synchronized(this) {
            roomList.remove(roomToDelete)
        }
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
            endGame(roomId)
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

        outgoing.convertAndSend("${RoomController.TOPIC_PREFIX}/$roomId/gameEnd", peopleInTheRoomOrdered)

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
            GameType.NORMAL -> players.sortedWith( compareBy({ it.successRate }, { it.timeTaken }))

            GameType.CODE_GOLF -> players.sortedWith( compareBy({ it.successRate }, { it.finalCodeLength }))
        }
    }

    fun executeCodeForUser(
            roomId: Long,
            userId: Long,
            testCaseExecuteDTO: TestCaseExecuteDTO
    ): ParseResponse {
        val dateOfSubmitting = Date()
        val room = getRoomList().first { it.owner.id == roomId }
        val user = (room.others + room.owner).first { it.id == userId }
        var userUpdateDto = UserUpdateDto(
                id = userId,
                successRate = user.successRate,
                submitted = false,
                status = "Running testcases"
        )

        outgoing.convertAndSend("${RoomController.TOPIC_PREFIX}/$roomId/userUpdate", userUpdateDto)

        val codeRunResponse = codeExecutorManagerService.executeJavaCode(
                challengeId =  testCaseExecuteDTO.challengeId.toLong(),
                code = testCaseExecuteDTO.code,
                testIds = testCaseExecuteDTO.testIds.map { it.toLong() }
        )

        val newSuccessRate: Float = if ( testCaseExecuteDTO.testIds.size == 1) {
            user.successRate
        } else {
            codeRunResponse.testResults.filter { it.passed }.count().toFloat() /
                    codeRunResponse.testResults.size
        }

        if (testCaseExecuteDTO.submitted) {
            userUpdateDto = userUpdateDto.copy(successRate =  newSuccessRate, status = "submitted", submitted = true)
            user.submitted = true
            user.finalCodeLength = testCaseExecuteDTO.code.length
            user.timeTaken = dateOfSubmitting.time -  room.gameStartedDate!!.time
            checkEverybodySubmitted(room)
        } else {
            userUpdateDto = userUpdateDto.copy(successRate =  newSuccessRate, status = "coding...")
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

}