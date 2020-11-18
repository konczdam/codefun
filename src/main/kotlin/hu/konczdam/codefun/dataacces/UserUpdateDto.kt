package hu.konczdam.codefun.dataacces

data class UserUpdateDto(
        val userId: Long,

        val successRate: Float,

        val submitted: Boolean,

        val status: String,

        val finalCodeLength: Int = -1,

        val timeTaken: Int = -1,

        val code: String = "",

        val language: String,

        val runTime: Int = -1
)