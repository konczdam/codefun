package hu.konczdam.codefun.controller

import hu.konczdam.codefun.dataacces.NewRoomDto
import hu.konczdam.codefun.dataacces.RoomUpdateDto
import hu.konczdam.codefun.dataacces.TestCaseExecuteDTO
import hu.konczdam.codefun.dataacces.UserDto
import hu.konczdam.codefun.docker.ParseResponse
import hu.konczdam.codefun.docker.service.JavaCodeRunnerService
import hu.konczdam.codefun.model.Message
import hu.konczdam.codefun.model.Room
import hu.konczdam.codefun.service.RoomService
import hu.konczdam.codefun.services.UserDetailsImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
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
    private lateinit var outgoing: SimpMessagingTemplate

    @Autowired
    private lateinit var javaCodeRunnerService: JavaCodeRunnerService

    private fun getUserIdFromPrincipal(principal: UsernamePasswordAuthenticationToken): Long {
        return (principal.principal as UserDetailsImpl).id
    }

    @SubscribeMapping(MSG_PREFIX)
    fun roomList(): List<Room> {
        return roomService.getRoomList()
    }

    @MessageMapping(MSG_PREFIX + "/addRoom")
    @SendTo(TOPIC_PREFIX + "/newRoom")
    fun addRoom(
            newRoom: NewRoomDto,
            principal: UsernamePasswordAuthenticationToken
    ): Room {
        return roomService.addRoom(getUserIdFromPrincipal(principal), newRoom.description)
    }


    @MessageMapping(MSG_PREFIX + "/joinRoom")
    @SendTo(TOPIC_PREFIX + "/updateRoom")
    fun joinToRoom(
            roomOwnerId: String,
            principal: UsernamePasswordAuthenticationToken
    ): List<UserDto> {

        return roomService.addUserToRoom(getUserIdFromPrincipal(principal), roomOwnerId.toLong())
    }

    @MessageMapping(MSG_PREFIX + "/{roomId}/leaveRoom")
    @SendTo(TOPIC_PREFIX + "/updateRoom")
    fun leaveRoom(
            @DestinationVariable roomId: String,
            principal: UsernamePasswordAuthenticationToken
    ): List<UserDto> {
        return roomService.removeUserFromRoom(getUserIdFromPrincipal(principal), roomId.toLong())
    }

    @SubscribeMapping(MSG_PREFIX + "/{roomId}")
    fun getInitialMessagesInARoom(
            @DestinationVariable roomId: String
    ): List<Message> {
       return roomService.getMessagesFromRoom(roomId.toLong())
    }

    @MessageMapping(MSG_PREFIX + "/{roomId}/send")
    fun sendMessage(
            message: Message,
            @DestinationVariable roomId: String
    ) {
        roomService.addMessage(roomId, message)
        outgoing.convertAndSend("$TOPIC_PREFIX/$roomId/newMessage", message)
    }

    @MessageMapping(MSG_PREFIX + "/{roomId}/close")
    fun closeRoom(
            @DestinationVariable roomId: String,
            principal: UsernamePasswordAuthenticationToken
    ) {
        roomService.removeRoom(getUserIdFromPrincipal(principal), roomId.toLong())
        outgoing.convertAndSend("$TOPIC_PREFIX/roomClosed", roomId)
    }

    @MessageMapping(MSG_PREFIX + "/setGameType")
    fun setSelectedGameType(
            gameType: String,
            principal: UsernamePasswordAuthenticationToken
    ) {
        val roomId = getUserIdFromPrincipal(principal)
        val gameTypeEnum = roomService.setGameType(roomId, gameType)
        outgoing.convertAndSend("$TOPIC_PREFIX/$roomId/gameTypeSet", gameTypeEnum)
    }

    @MessageMapping(MSG_PREFIX + "/startGame")
    fun startGame(
            principal: UsernamePasswordAuthenticationToken
    ) {
        val roomId = getUserIdFromPrincipal(principal)
        val room = roomService.startGame(roomId)
        val roomUpdateDto = RoomUpdateDto(roomId, gameStarted = true)
        outgoing.convertAndSend("$TOPIC_PREFIX/updateRoom", roomUpdateDto)
        outgoing.convertAndSend("$TOPIC_PREFIX/$roomId/gameStarted", room)
    }

    @MessageMapping(MSG_PREFIX + "/{roomId}/executeCode")
    @SendToUser("/topic/codeRunResponse")
    fun executeCode(
            @DestinationVariable roomId: String,
            testCaseExecuteDTO: TestCaseExecuteDTO,
            principal: UsernamePasswordAuthenticationToken
    ): ParseResponse {
        val result = roomService.executeCodeForUser(
                roomId = roomId.toLong(),
                userId = getUserIdFromPrincipal(principal),
                testCaseExecuteDTO = testCaseExecuteDTO
        )
        return result
    }

}