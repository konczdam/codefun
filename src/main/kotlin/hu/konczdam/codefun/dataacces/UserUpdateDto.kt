package hu.konczdam.codefun.dataacces

data class UserUpdateDto(
        val userId: Long,

        val successRate: Float,

        val submitted: Boolean,

        val status: String
)