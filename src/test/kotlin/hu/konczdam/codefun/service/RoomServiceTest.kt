package hu.konczdam.codefun.service

import hu.konczdam.codefun.dataacces.TestCaseExecuteDTO
import hu.konczdam.codefun.dataacces.UserCreationDto
import hu.konczdam.codefun.dataacces.UserDto
import hu.konczdam.codefun.docker.ParseResponse
import hu.konczdam.codefun.docker.service.CodeExecutorManagerService
import hu.konczdam.codefun.model.*
import hu.konczdam.codefun.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.mockito.Mockito.`when` as whem
import com.nhaarman.mockitokotlin2.any


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
class RoomServiceTest {

    companion object {
        val basicUserDto = UserDto(
                username = "teszt",
                email = "teszt@alma.hu",
                preferredLanguages = mutableSetOf(),
                id = -1
        )
        var userId = 0
    }

    @Autowired
    @InjectMocks
    private lateinit var roomService: RoomService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var challengeService: ChallengeService

    @Mock
    private lateinit var codeExecutorManagerService: CodeExecutorManagerService

    @Test
    fun determineFinalOrderOfPlayersNormalGameType_test() {
        val player1 = basicUserDto.copy(id = 1).apply { successRate = 20f ; timeTaken = 40 }
        val player2 = basicUserDto.copy(id = 2).apply { successRate = 40f ; timeTaken = 40 }
        val player3 = basicUserDto.copy(id = 3).apply { successRate = 20f ; timeTaken = 60 }
        val players = listOf(player1, player2, player3)

        val result = roomService.determineFinalOrderOfPlayer(players, GameType.NORMAL)

        assertEquals(3, result.size)
        assertEquals(player2, result[0])
        assertEquals(player1, result[1])
        assertEquals(player3, result[2])
    }

    @Test
    fun determineFinalOrderOfPlayersCodeGolfGameType_test() {
        val player1 = basicUserDto.copy(id = 1).apply { successRate = 20f ; finalCodeLength = 40 }
        val player2 = basicUserDto.copy(id = 2).apply { successRate = 40f ; finalCodeLength = 40 }
        val player3 = basicUserDto.copy(id = 3).apply { successRate = 20f ; finalCodeLength = 60 }
        val players = listOf(player1, player2, player3)

        val result = roomService.determineFinalOrderOfPlayer(players, GameType.CODE_GOLF)

        assertEquals(3, result.size)
        assertEquals(player2, result[0])
        assertEquals(player1, result[1])
        assertEquals(player3, result[2])
    }

    @Test
    fun addRoom_test() {
        val player = createAndSaveUser()
        val description = "description"
        roomService.addRoom(player.id!!, description)

        val roomList = roomService.getRoomList()

        assertEquals(1, roomList.size)
        assertEquals(player.id, roomList[0].owner.id)
        assertEquals(description, roomList[0].description)

        roomService.removeRoom(player.id!!, player.id!!)
        userRepository.delete(player)
    }

    @Test
    fun removeRoom_test() {
        val player = createAndSaveUser()
        val description = "description"
        roomService.addRoom(player.id!!, description)
        roomService.removeRoom(player.id!!, player.id!!)

        val roomList = roomService.getRoomList()

        assertEquals(0, roomList.size)

        userRepository.delete(player)
    }

    @Test
    fun addUserToRoom_test() {
        val player = createAndSaveUser()
        val player2 = createAndSaveUser()
        val description = "description"
        roomService.addRoom(player.id!!, description)
        roomService.addUserToRoom(userId = player2.id!!, ownerId = player.id!!)

        val roomList = roomService.getRoomList()

        assertEquals(1, roomList.size)
        val room = roomList[0]
        assertEquals(1, room.others.size)
        assertEquals(player2.id, room.others[0].id)

        roomService.removeRoom(player.id!!, player.id!!)
        userRepository.delete(player)
        userRepository.delete(player2)
    }

    @Test
    fun usersInChatRoom_test() {
        val player = createAndSaveUser()
        val player2 = createAndSaveUser()
        val description = "description"
        roomService.addRoom(player.id!!, description)
        roomService.addUserToRoom(userId = player2.id!!, ownerId = player.id!!)

        val usersInChatRoom = roomService.usersInChatRoom(player.id!!)
        assertEquals(2, usersInChatRoom.size)
        assertTrue(usersInChatRoom.any { it.id.equals(player.id)})
        assertTrue(usersInChatRoom.any { it.id.equals(player2.id)})


        roomService.removeRoom(player.id!!, player.id!!)
        userRepository.delete(player)
        userRepository.delete(player2)
    }

    @Test
    fun removeUserFromRoom_test() {
        val player = createAndSaveUser()
        val player2 = createAndSaveUser()
        val description = "description"
        roomService.addRoom(player.id!!, description)
        roomService.addUserToRoom(userId = player2.id!!, ownerId = player.id!!)
        roomService.removeUserFromRoom(userId = player2.id!!, ownerId = player.id!!)

        val usersInChatRoom = roomService.usersInChatRoom(player.id!!)
        assertEquals(1, usersInChatRoom.size)
        assertTrue(usersInChatRoom.any { it.id.equals(player.id)})
        assertFalse(usersInChatRoom.any { it.id.equals(player2.id)})


        roomService.removeRoom(player.id!!, player.id!!)
        userRepository.delete(player)
        userRepository.delete(player2)
    }

    @Test
    fun addMessage_test() {
        val player = createAndSaveUser()
        val description = "description"
        roomService.addRoom(player.id!!, description)
        val message = "message"
        val newMessage = Message(
                userId = player.id!!.toString(),
                username = player.username,
                message = message
        )
        roomService.addMessage(player.id!!.toString(), newMessage)

        val messages = roomService.getRoomList()[0].messages

        assertEquals(1, messages.size)
        assertEquals(newMessage, messages[0])

        roomService.removeRoom(player.id!!, player.id!!)
        userRepository.delete(player)
    }

    @Test
    fun getMessagesFromRoom_test() {
        val player = createAndSaveUser()
        val description = "description"
        roomService.addRoom(player.id!!, description)
        val message = "message"
        val newMessage = Message(
                userId = player.id!!.toString(),
                username = player.username,
                message = message
        )
        roomService.addMessage(player.id!!.toString(), newMessage)

        val messages = roomService.getMessagesFromRoom(player.id!!)

        assertEquals(1, messages.size)
        assertEquals(newMessage, messages[0])

        roomService.removeRoom(player.id!!, player.id!!)
        userRepository.delete(player)
    }


    @Test
    fun setGameType_test() {
        val player = createAndSaveUser()
        val description = "description"
        roomService.addRoom(player.id!!, description)
        roomService.setGameType(player.id!!, GameType.CODE_GOLF.name)

        val room = roomService.getRoomList()[0]
        assertEquals(GameType.CODE_GOLF, room.gameType)

        roomService.removeRoom(player.id!!, player.id!!)
        userRepository.delete(player)
    }

    @Test
    fun setOnlyFriendsAllowed_test() {
        val player = createAndSaveUser()
        val description = "description"
        roomService.addRoom(player.id!!, description)
        roomService.setOnlyFriendsAllowed(player.id!!, true)

        val room = roomService.getRoomList()[0]
        assertTrue(room.onlyFriends)

        roomService.removeRoom(player.id!!, player.id!!)
        userRepository.delete(player)
    }

    @Test
    fun startGame_test() {
        val player = createAndSaveUser()
        val description = "description"
        roomService.addRoom(player.id!!, description)
        val challenge = createAndSaveChallengeTest("asdass")

        MockitoAnnotations.initMocks(this)
        whem(challengeService.getRandomChallenge()).thenReturn(challenge)

        val room = roomService.startGame(player.id!!)

        assertTrue(room.gameStarted)
        assertNotNull(room.gameStartedDate)
        assertEquals(challenge, room.challenge)


        roomService.removeRoom(player.id!!, player.id!!)
        userRepository.delete(player)
    }

    @Test
    fun executeCode_test() {
        val player = createAndSaveUser()
        val description = "description"
        roomService.addRoom(player.id!!, description)
        val challenge = createAndSaveChallengeTest("asdass")

        MockitoAnnotations.initMocks(this)
        whem(codeExecutorManagerService.executeJavaCode(any(), any(), any())).thenReturn(ParseResponse(
                errorMessage = "asdasdas"
        ))

        val challengeId = "5"
        val code = "assasasa"
        val testIds = listOf("2")
        roomService.executeCodeForUser(player.id!!, player.id!!, TestCaseExecuteDTO(
                challengeId = challengeId,
                code = code,
                testIds = testIds,
                submitted = false,
                language = Language.JAVA.name
        ))

        verify(codeExecutorManagerService, times(1))
                .executeJavaCode(challengeId.toLong(), code, testIds.map { it.toLong()})

        roomService.removeRoom(player.id!!, player.id!!)
        userRepository.delete(player)
    }

    private fun createAndSaveUser(): User {
        val userCreationDto = UserCreationDto(
                email = "${userId}",
                password = "almakörte",
                username = "${userId++}",
                roles = mutableSetOf("user"),
                preferredLanguages = mutableSetOf(Language.JAVA, Language.JAVASCRIPT)
        )
        return userService.createUser(userCreationDto)
    }

    private fun createAndSaveChallengeTest(seed: String): Challenge {
        val challenge = Challenge(
                title = seed + "title",
                description = seed + "description"
        )
        challenge.challengeTests.add(
                ChallengeTest(
                        input = seed + "input1",
                        expectedOutput = seed + "output1",
                        displayName = seed + "displayName1"
                )
        )
        challenge.challengeTests.add(
                ChallengeTest(
                        input = seed + "input2",
                        expectedOutput = seed + "output2",
                        displayName = seed + "displayName2"
                )
        )
        return challenge
    }
}