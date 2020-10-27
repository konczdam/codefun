package hu.konczdam.codefun.service

import hu.konczdam.codefun.converter.toDto
import hu.konczdam.codefun.dataacces.UserDto
import hu.konczdam.codefun.model.Room
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class RoomService {

    @Autowired
    private lateinit var userService: UserService

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

}