package hu.konczdam.codefun.converter

import hu.konczdam.codefun.dataacces.UserDto
import hu.konczdam.codefun.model.User
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers

@Mapper
interface UserConverter {

    fun convertToDto(user: User): UserDto

    companion object {
        val mapper = Mappers.getMapper(UserConverter::class.java)
    }

}

fun User.toDto(): UserDto {
    return UserConverter.mapper.convertToDto(this)
}

fun Collection<User>.toDtoList(): Collection<UserDto> {
    return this.map { it.toDto() }
}
