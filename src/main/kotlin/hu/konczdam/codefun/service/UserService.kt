package hu.konczdam.codefun.service

import hu.konczdam.codefun.dataacces.PageRequest
import hu.konczdam.codefun.dataacces.PasswordUpdateRequestDto
import hu.konczdam.codefun.dataacces.UserCreationDto
import hu.konczdam.codefun.getDirectionValueFromString
import hu.konczdam.codefun.model.ERole
import hu.konczdam.codefun.model.Role
import hu.konczdam.codefun.model.User
import hu.konczdam.codefun.repository.RoleRepository
import hu.konczdam.codefun.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.data.domain.PageRequest as SpringPageRequest
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import org.springframework.transaction.annotation.Transactional
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

    @Transactional(readOnly =  true)
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

    @Transactional(readOnly =  true)
    fun getPageOfUsersExcludingCaller(
            pageRequest: PageRequest,
            name: String?,
            callerId: Long
    ): Page<User> {
        val sortDirection = getDirectionValueFromString(pageRequest.sortDirection)
        val springPageRequest = SpringPageRequest.of(
                pageRequest.page,
                pageRequest.size.let { if (it > 0 ) it else Int.MAX_VALUE },
                Sort.by(sortDirection, pageRequest.sortProperty)
        )
        return userRepository.findFiltereAndSortedUsers(name, callerId, springPageRequest)
    }

    @Throws(IllegalArgumentException::class)
    @Transactional
    fun changePasswordForUser(userId: Long, passwordUpdateRequestDto: PasswordUpdateRequestDto) {
        val user = getUserById(userId)
        val oldPasswordCorrect = BCrypt.checkpw(passwordUpdateRequestDto.oldPassword, user.password)
        if (!oldPasswordCorrect) {
            throw IllegalArgumentException("Current Password doesn't match")
        }
        user.password = BCrypt.hashpw(passwordUpdateRequestDto.newPassword, BCrypt.gensalt())
        userRepository.save(user)
    }

}
