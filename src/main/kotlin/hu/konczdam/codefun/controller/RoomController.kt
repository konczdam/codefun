package hu.konczdam.codefun.controller

import hu.konczdam.codefun.dataacces.NewRoomDto
import hu.konczdam.codefun.model.Room
import hu.konczdam.codefun.service.RoomService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.stereotype.Controller

@Controller
class RoomController {

    companion object {
        const val MSG_PREFIX = "/rooms"
        const val TOPIC_PREFIX = MSG_PREFIX + "topic"
    }

    @Autowired
    private lateinit var roomService: RoomService

    @SubscribeMapping(MSG_PREFIX)
    fun roomList(): List<Room> {
        return roomService.getRoomList()
    }

    @MessageMapping(MSG_PREFIX + "/addRoom")
    @SendTo(MSG_PREFIX + "/newRoom")
    fun addRoom(newRoom: NewRoomDto): Room {
        return roomService.addRoom(newRoom.ownerId, newRoom.description)
    }

}