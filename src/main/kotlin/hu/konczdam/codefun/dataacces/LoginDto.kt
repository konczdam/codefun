package hu.konczdam.codefun.dataacces

import javax.validation.constraints.Email
import javax.validation.constraints.Size

data class LoginDto (

        @field:Email
        @field:Size(max = 50)
        val email: String,

        @field:Size(min = 5)
        val password: String
)