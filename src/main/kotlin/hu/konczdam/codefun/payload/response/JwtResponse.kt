package hu.konczdam.codefun.payload.response

data class JwtResponse (
        val token: String,

        val type: String = "Bearer",

        val id: Long,

        val username: String,

        val email: String,

        val roles: List<String>
)