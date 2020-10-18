package hu.konczdam.codefun.model

data class Room constructor(
    val owner: User,

    val description: String
) {
    val others: MutableList<User> = mutableListOf()

    fun subscribe(user: User): Room {
        val newRoom = this.copy()
        newRoom.others.addAll(this.others)
        newRoom.others.add(user)
        return newRoom
    }

    fun unSubscribe(user: User): Room {
        val newRoom = this.copy()
        newRoom.others.addAll(this.others)
        newRoom.others.remove(user)
        return newRoom
    }
}