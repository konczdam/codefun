package hu.konczdam.codefun.model

import hu.konczdam.codefun.dataacces.UserDto
import java.util.*

data class Room constructor(
        val owner: UserDto,

        val description: String

) {
    val others: MutableList<UserDto> = mutableListOf()

    val messages: MutableList<Message> = mutableListOf()

    var gameType: GameType = GameType.NORMAL

    var gameStarted = false

    var gameStartedDate: Date? = null

    fun subscribe(user: UserDto): Room {
        val newRoom = this.copy()
        newRoom.others.addAll(this.others)
        newRoom.others.add(user)
        return newRoom
    }

    fun unSubscribe(user: UserDto): Room {
        val newRoom = this.copy()
        newRoom.others.addAll(this.others)
        newRoom.others.remove(user)
        return newRoom
    }
}