package hu.konczdam.codefun.service

import hu.konczdam.codefun.converter.toDto
import hu.konczdam.codefun.dataacces.UserDto
import hu.konczdam.codefun.model.GameType
import hu.konczdam.codefun.model.Message
import hu.konczdam.codefun.model.Room
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.annotation.PostConstruct

@Service
class RoomService {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var challengeService: ChallengeService

    private val roomList: MutableList<Room> = mutableListOf()

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
            room.apply {
                gameStarted = true
                gameStartedDate = Date()
                challenge = randomChallenge
            }
        }
        return room
    }

}