package hu.konczdam.codefun.controller

import hu.konczdam.codefun.config.jwt.JwtUtils
import hu.konczdam.codefun.dataacces.NewRoomDto
import hu.konczdam.codefun.dataacces.UserDto
import hu.konczdam.codefun.model.Room
import hu.konczdam.codefun.service.RoomService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.stereotype.Controller

@Controller
class RoomController {

    companion object {
        const val MSG_PREFIX = "/rooms"
        const val TOPIC_PREFIX = "/topic" + MSG_PREFIX
    }

    @Autowired
    private lateinit var roomService: RoomService

    @Autowired
    private lateinit var jwtUtils: JwtUtils

    @SubscribeMapping(MSG_PREFIX)
    fun roomList(): List<Room> {
        return roomService.getRoomList()
    }

    @MessageMapping(MSG_PREFIX + "/addRoom")
    @SendTo(TOPIC_PREFIX + "/newRoom")
    fun addRoom(newRoom: NewRoomDto, @Header("Authorization", required = true) header: String): Room {
        val ownerId = jwtUtils.getIdFromJwtToken(header)?.toLong()
        if (ownerId == null) {
            throw Exception("Invalid token!")
        }
        return roomService.addRoom(ownerId, newRoom.description)
    }


    @MessageMapping(MSG_PREFIX + "/joinRoom")
    @SendTo(TOPIC_PREFIX + "/updateRoom")
    fun joinToRoom(@Header("Authorization", required = true) header: String, roomOwnerId: String): List<UserDto> {
        val userId = jwtUtils.getIdFromJwtToken(header)?.toLong()
        if (userId == null) {
            throw Exception("Invalid token!")
        }

        return roomService.addUserToRoom(userId, roomOwnerId.toLong())
    }



}