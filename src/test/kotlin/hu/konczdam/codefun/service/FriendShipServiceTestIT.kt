package hu.konczdam.codefun.service

import hu.konczdam.codefun.dataacces.FriendshipRequestDto
import hu.konczdam.codefun.dataacces.UserCreationDto
import hu.konczdam.codefun.model.Language
import hu.konczdam.codefun.model.User
import hu.konczdam.codefun.repository.FriendRequestRepository
import hu.konczdam.codefun.repository.UserRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
class FriendShipServiceTestIT {

    companion object {
        var userId = 0
    }

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var friendshipService: FriendshipService

    @Autowired
    private lateinit var friendRequestRequestRepository: FriendRequestRepository

    @Test
    fun addFriendShipRequest_test() {
        val user1 = createAndSaveUser()
        val user2 = createAndSaveUser()

        val friendshipRequestDto = FriendshipRequestDto(
                requesterId = user1.id!!.toString(),
                receiverId = user2.id!!.toString()
        )

        friendshipService.addFriendshipRequest(friendshipRequestDto)

        val request = friendRequestRequestRepository.findByRequesterIdAndReceiverId(
                requesterId = user1.id!!,
                receiverId = user2.id!!
        )

        Assertions.assertNotNull(request)

        friendRequestRequestRepository.delete(request)
        userRepository.delete(user1)
        userRepository.delete(user2)
    }

    @Test
    fun removeFriendShipRequest_test() {
        val user1 = createAndSaveUser()
        val user2 = createAndSaveUser()

        val friendshipRequestDto = FriendshipRequestDto(
                requesterId = user1.id!!.toString(),
                receiverId = user2.id!!.toString()
        )

        friendshipService.addFriendshipRequest(friendshipRequestDto)
        friendshipService.removeRequest(friendshipRequestDto)

        var exception: Exception? = null
        try {
            friendRequestRequestRepository.findByRequesterIdAndReceiverId(
                    requesterId = user1.id!!,
                    receiverId = user2.id!!
            )
        } catch (e: Exception) {
            exception = e
        }

        Assertions.assertNotNull(exception)
        Assertions.assertTrue(exception is EmptyResultDataAccessException)

        userRepository.delete(user1)
        userRepository.delete(user2)
    }

    @Test
    fun acceptRequest_test() {
        val user1 = createAndSaveUser()
        val user2 = createAndSaveUser()

        val friendshipRequestDto = FriendshipRequestDto(
                requesterId = user1.id!!.toString(),
                receiverId = user2.id!!.toString()
        )

        friendshipService.addFriendshipRequest(friendshipRequestDto)
        friendshipService.acceptRequest(friendshipRequestDto)

        val user1Friends = userService.getAllFriendIds(user1.id!!)
        val user2Friends = userService.getAllFriendIds(user2.id!!)


        Assertions.assertEquals(1, user1Friends.size)
        Assertions.assertTrue(user1Friends.contains(user2.id!!))

        Assertions.assertEquals(1, user2Friends.size)
        Assertions.assertTrue(user2Friends.contains(user1.id!!))

        friendshipService.removeFriend(friendshipRequestDto)
        userRepository.delete(user1)
        userRepository.delete(user2)
    }

    @Test
    fun removeFriend_test() {
        val user1 = createAndSaveUser()
        val user2 = createAndSaveUser()

        val friendshipRequestDto = FriendshipRequestDto(
                requesterId = user1.id!!.toString(),
                receiverId = user2.id!!.toString()
        )

        friendshipService.addFriendshipRequest(friendshipRequestDto)
        friendshipService.acceptRequest(friendshipRequestDto)
        friendshipService.removeFriend(friendshipRequestDto)

        val user1Friends = userService.getAllFriendIds(user1.id!!)
        val user2Friends = userService.getAllFriendIds(user2.id!!)


        Assertions.assertEquals(0, user1Friends.size)

        Assertions.assertEquals(0, user2Friends.size)

        userRepository.delete(user1)
        userRepository.delete(user2)
    }

    private fun createAndSaveUser(): User {
        val userCreationDto = UserCreationDto(
                email = "${userId}",
                password = "almakörte",
                username = "${userId++}",
                roles = mutableSetOf("user"),
                preferredLanguages = mutableSetOf(Language.JAVA, Language.JAVASCRIPT)
        )
        return userService.createUser(userCreationDto)
    }
}