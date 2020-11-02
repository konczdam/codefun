package hu.konczdam.codefun.dataacces

data class RoomDto (
        val owner: UserDto,

        val description: String,

        val others: List<UserDto>
)