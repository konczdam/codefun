package hu.konczdam.codefun.dataacces

data class UserUpdateDto(
        val id: Long,

        val successRate: Float,

        val submitted: Boolean,

        val status: String
)