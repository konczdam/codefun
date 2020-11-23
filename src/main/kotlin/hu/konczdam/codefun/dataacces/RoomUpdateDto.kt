package hu.konczdam.codefun.dataacces

class RoomUpdateDto constructor(
        val roomId: Long,

        val gameStarted: Boolean? = null,

        var friendsOnly: Boolean? = null
)