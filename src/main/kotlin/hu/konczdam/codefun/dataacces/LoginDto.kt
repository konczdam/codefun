package hu.konczdam.codefun.dataacces

import javax.validation.constraints.Email
import javax.validation.constraints.Size

data class  LoginDto (

        @Email
        @Size(max = 50)
        val email: String,

        @Size(min = 5)
        val password: String
)