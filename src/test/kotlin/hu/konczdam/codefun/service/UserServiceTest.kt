package hu.konczdam.codefun.service

import hu.konczdam.codefun.dataacces.PasswordUpdateRequestDto
import hu.konczdam.codefun.dataacces.UserCreationDto
import hu.konczdam.codefun.model.Language
import hu.konczdam.codefun.model.User
import hu.konczdam.codefun.repository.UserRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.bcrypt.BCrypt
import java.util.*
import org.mockito.Mockito.`when` as whem

@SpringBootTest
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var userService: UserService

    @Test
    fun findUserById_test() {
        val user = User(
                email = "example@gmail.com",
                password = "examplePassword",
                username = "konczdam"
        )
        val userId = 44L
        user.id = userId

        whem(userRepository.findById(userId)).thenReturn(Optional.of(user))
        val userFromService = userService.getUserById(user.id!!)
        Assertions.assertEquals(user, userFromService)
    }

    @Test
    fun changePasswordForUser_test() {
        val oldPassword = "almakörte"
        val user = User(
                email = "example@gmail.com",
                password = BCrypt.hashpw(oldPassword, BCrypt.gensalt()),
                username = "konczdam"
        )
        val userId = 44L
        user.id = userId
        val newPassword = "aasdwqejiji2"
        val passwordUpdateRequestDto = PasswordUpdateRequestDto(
                oldPassword, newPassword
        )

        whem(userRepository.findById(userId)).thenReturn(Optional.of(user))
        userService.changePasswordForUser(user.id!!, passwordUpdateRequestDto)

        Assertions.assertTrue(BCrypt.checkpw(newPassword, user.password))

    }
}