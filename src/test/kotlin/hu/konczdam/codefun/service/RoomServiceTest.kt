package hu.konczdam.codefun.service

import hu.konczdam.codefun.dataacces.UserDto
import hu.konczdam.codefun.model.GameType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest


@SpringBootTest
class RoomServiceTest {

    companion object {
        val basicUserDto = UserDto(
                username = "teszt",
                email = "teszt@alma.hu",
                preferredLanguages = mutableSetOf(),
                id = -1
        )
    }

    @Autowired
    private lateinit var roomService: RoomService

    @Test
    fun determineFinalOrderOfPlayers_test() {
        val player1 = basicUserDto.copy(id = 1).apply { successRate = 20f ; timeTaken = 40 }
        val player2 = basicUserDto.copy(id = 2).apply { successRate = 40f ; timeTaken = 40 }
        val player3 = basicUserDto.copy(id = 3).apply { successRate = 20f ; timeTaken = 60 }
        val players = listOf(player1, player2, player3)

        val result = roomService.determineFinalOrderOfPlayer(players, GameType.NORMAL)

        assertEquals(3, result.size)
        assertEquals(player2, result[0])
        assertEquals(player1, result[1])
        assertEquals(player3, result[2])
    }
}