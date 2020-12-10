package hu.konczdam.codefun.service

import hu.konczdam.codefun.dataacces.PasswordUpdateRequestDto
import hu.konczdam.codefun.dataacces.UserCreationDto
import hu.konczdam.codefun.model.Language
import hu.konczdam.codefun.model.User
import hu.konczdam.codefun.repository.UserRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.test.context.ActiveProfiles
import java.lang.IllegalArgumentException

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
class UserServiceTestIT {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun findUserById_test() {
        val userCreationDto = UserCreationDto(
                email = "example@gmail.com",
                password = "almakörte",
                username = "konczdam",
                roles = mutableSetOf("user"),
                preferredLanguages = mutableSetOf(Language.JAVA, Language.JAVASCRIPT)
        )
        val user =  userService.createUser(userCreationDto)

        val userFromService = userService.getUserById(user.id!!)

        Assertions.assertEquals(user, userFromService)
        userRepository.delete(user)
    }

    @Test
    fun incrementGamesPlayed_test() {
        val userCreationDto = UserCreationDto(
                email = "example@gmail.com",
                password = "almakörte",
                username = "konczdam",
                roles = mutableSetOf("user"),
                preferredLanguages = mutableSetOf(Language.JAVA, Language.JAVASCRIPT)
        )
        var user1 =  userService.createUser(userCreationDto)
        var user2 = userService.createUser(userCreationDto.copy(email = "example2@gmail.com"))

        userService.incrementGamesPlayed(listOf(user1.id!!, user2.id!!))
        userService.incrementGamesPlayed(listOf( user2.id!!))


        user1 = userService.getUserById(user1.id!!)
        user2 = userService.getUserById(user2.id!!)

        Assertions.assertEquals(1, user1.gamesPlayed)
        Assertions.assertEquals(2, user2.gamesPlayed)

        userRepository.delete(user1)
        userRepository.delete(user2)
    }

    @Test
    fun incrementGamesPlayedAndGamesWon_test() {
        val userCreationDto = UserCreationDto(
                email = "example@gmail.com",
                password = "almakörte",
                username = "konczdam",
                roles = mutableSetOf("user"),
                preferredLanguages = mutableSetOf(Language.JAVA, Language.JAVASCRIPT)
        )
        var user1 =  userService.createUser(userCreationDto)

        userService.incrementGamesPlayedAndGamesWon(user1.id!!)


        user1 = userService.getUserById(user1.id!!)

        Assertions.assertEquals(1, user1.gamesPlayed)
        Assertions.assertEquals(1, user1.gamesWon)

        userRepository.delete(user1)
    }

    @Test
    fun changePasswordForUser_test() {
        val oldPassword = "almakörte"
        val userCreationDto = UserCreationDto(
                email = "example@gmail.com",
                password = oldPassword,
                username = "konczdam",
                roles = mutableSetOf("user"),
                preferredLanguages = mutableSetOf(Language.JAVA, Language.JAVASCRIPT)
        )
        var user1 =  userService.createUser(userCreationDto)
        val newPassword = "aasdwqejiji2"
        val passwordUpdateRequestDto = PasswordUpdateRequestDto(
                oldPassword, newPassword
        )

        userService.changePasswordForUser(user1.id!!, passwordUpdateRequestDto)


        user1 = userService.getUserById(user1.id!!)

        Assertions.assertTrue(BCrypt.checkpw(newPassword, user1.password))

        userRepository.delete(user1)
    }

    @Test
    fun changePasswordForUserExceptionThrow_test() {
        val oldPassword = "almakörte"
        val userCreationDto = UserCreationDto(
                email = "example@gmail.com",
                password = oldPassword,
                username = "konczdam",
                roles = mutableSetOf("user"),
                preferredLanguages = mutableSetOf(Language.JAVA, Language.JAVASCRIPT)
        )
        val user1 =  userService.createUser(userCreationDto)
        val newPassword = "aasdwqejiji2"
        val passwordUpdateRequestDto = PasswordUpdateRequestDto(
                oldPassword + "asddas", newPassword
        )

        var exception: Exception? = null
        try {
            userService.changePasswordForUser(user1.id!!, passwordUpdateRequestDto)
        } catch (e: Exception) {
            exception = e
        }

        Assertions.assertNotNull(exception)
        Assertions.assertTrue(exception is IllegalArgumentException)
        exception?.message?.startsWith("Current Password doesn't match")?.let { Assertions.assertTrue(it) }

        userRepository.delete(user1)
    }
}