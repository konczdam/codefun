package hu.konczdam.codefun.converter

import hu.konczdam.codefun.dataacces.RoomDto
import hu.konczdam.codefun.model.Room
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers

@Mapper
interface RoomConverter {

    fun convertToDto(room: Room): RoomDto

    companion object {
        val mapper = Mappers.getMapper(RoomConverter::class.java)
    }
}

fun Room.toDto(): RoomDto {
    return RoomConverter.mapper.convertToDto(this)
}

fun Collection<Room>.toDtoList(): Collection<RoomDto> {
    return this.map { it.toDto() }
}