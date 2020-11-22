package hu.konczdam.codefun.dataacces

import javax.validation.constraints.Size

data class PasswordUpdateRequestDto (
     val oldPassword: String,

     @Size(min = 6)
     val newPassword: String
)