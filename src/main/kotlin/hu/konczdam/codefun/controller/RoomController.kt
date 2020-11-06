package hu.konczdam.codefun.controller

import hu.konczdam.codefun.config.jwt.JwtUtils
import hu.konczdam.codefun.dataacces.NewRoomDto
import hu.konczdam.codefun.dataacces.UserDto
import hu.konczdam.codefun.model.Message
import hu.konczdam.codefun.model.Room
import hu.konczdam.codefun.service.RoomService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
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

    @Autowired
    private lateinit var outgoing: SimpMessagingTemplate

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

    @MessageMapping(MSG_PREFIX + "/{roomId}/leaveRoom")
    @SendTo(TOPIC_PREFIX + "/updateRoom")
    fun leaveRoom(
            @Header("Authorization", required = true) header: String,
            @DestinationVariable roomId: String
    ): List<UserDto> {
        val userId = jwtUtils.getIdFromJwtToken(header)?.toLong()
        if (userId == null) {
            throw Exception("Invalid token!")
        }

        return roomService.removeUserFromRoom(userId, roomId.toLong())
    }

    @SubscribeMapping(MSG_PREFIX + "/{roomId}")
    fun getInitialMessagesInARoom(
            @Header("Authorization", required = true) header: String,
            @DestinationVariable roomId: String
    ): List<Message> {
       return roomService.getRoomList().first { it.owner.id == roomId.toLong()}.messages
    }

    @MessageMapping(MSG_PREFIX + "/{roomId}/send")
    fun sendMessage(
            @Header("Authorization", required = true) header: String,
            message: Message,
            @DestinationVariable roomId: String
    ) {
        val userId = jwtUtils.getIdFromJwtToken(header)?.toLong()
        if (userId != message.userId.toLong()) {
            throw Exception("Invalid token!")
        }
        val messages = roomService.addMessage(roomId, message)
        outgoing.convertAndSend("$TOPIC_PREFIX/$roomId/newMessage", message)
    }

    @MessageMapping(MSG_PREFIX + "/{roomId}/close")
    fun closeRoom(
            @Header("Authorization", required = true) header: String,
            @DestinationVariable roomId: String
    ) {
        val userId = jwtUtils.getIdFromJwtToken(header)
        if (userId == null) {
            throw Exception("Invalid token!")
        }

        roomService.removeRoom(userId, roomId)
        outgoing.convertAndSend("$TOPIC_PREFIX/$roomId/roomClosed", "Room closed")
    }

    @MessageMapping(MSG_PREFIX + "/setGameType")
    fun setSelectedGameType(
            @Header("Authorization", required = true) header: String,
            gameType: String
    ) {
        val roomId = jwtUtils.getIdFromJwtToken(header)?.toLong()
        if (roomId == null) {
            throw Exception("Invalid token!")
        }
        val gameTypeEnum = roomService.setGameType(roomId, gameType)
        outgoing.convertAndSend("$TOPIC_PREFIX/$roomId/gameTypeSet", gameTypeEnum)
    }

    @MessageMapping(MSG_PREFIX + "/startGame")
    fun startGame(
            @Header("Authorization", required = true) header: String
    ) {
        val roomId = jwtUtils.getIdFromJwtToken(header)

        roomService.startGame(roomId)
    }


}