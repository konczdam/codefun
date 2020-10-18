package hu.konczdam.codefun.dataacces

import hu.konczdam.codefun.model.Language
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class UserCreationDto (

        @Email
        @Size(max = 50)
        val email: String,

        @NotBlank
        val username: String,

        @Size(min = 6)
        val password: String,

        val roles: Set<String>,

        val preferredLanguages: MutableSet<Language>

)