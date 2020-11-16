package hu.konczdam.codefun.service

import hu.konczdam.codefun.dataacces.UserCreationDto
import hu.konczdam.codefun.model.ERole
import hu.konczdam.codefun.model.Role
import hu.konczdam.codefun.model.User
import hu.konczdam.codefun.repository.RoleRepository
import hu.konczdam.codefun.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Service
import javax.transaction.Transactional
import kotlin.RuntimeException

@Service
class UserService {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var roleRepository: RoleRepository

    @Transactional
    @Throws(RuntimeException::class)
    fun createUser(userCreationDto: UserCreationDto): User {
        val roles: MutableSet<Role> = mutableSetOf()

        userCreationDto.roles.forEach { role ->
            when (role) {
                "user" -> {
                    var userRole = roleRepository.findByName(ERole.ROLE_USER)
                    if (userRole == null)
                        userRole = roleRepository.save(Role(name = ERole.ROLE_USER))
                    roles.add(userRole)
                }
                else -> {
                    throw RuntimeException("Error: Unexpected role!")
                }
            }
        }
        val user = User(
                username = userCreationDto.username,
                email = userCreationDto.email,
                password = BCrypt.hashpw(userCreationDto.password, BCrypt.gensalt()),
                roles = roles,
                preferredLanguages = userCreationDto.preferredLanguages
        )
        return userRepository.save(user)
    }

    @Transactional
    fun getUserById(userId: Long): User {
        val user = userRepository.findById(userId)
        if (user.isPresent) {
            return user.get()
        }
        throw Exception("User not found in DB")
    }

    @Transactional
    fun incrementGamesPlayed(userIds: List<Long>) {
        val users = userRepository.findAllById(userIds)
        users.forEach { it.gamesPlayed++ }
        userRepository.saveAll(users)
    }

    @Transactional
    fun incrementGamesPlayedAndGamesWon(userId: Long) {
        val user = getUserById(userId)
        user.gamesPlayed++
        user.gamesWon++
        userRepository.save(user)
    }

}
