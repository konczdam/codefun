package hu.konczdam.codefun.service

import hu.konczdam.codefun.dataacces.FriendshipRequestDto
import hu.konczdam.codefun.model.FriendRequest
import hu.konczdam.codefun.repository.FriendRequestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FriendshipService {

    @Autowired
    private lateinit var friendRequestRepository: FriendRequestRepository

    @Autowired
    private lateinit var userService: UserService

    @Transactional
    fun addFriendshipRequest(friendshipRequestDto: FriendshipRequestDto) {
        val requester = userService.getUserById(friendshipRequestDto.requesterId.toLong())

        val receiver = userService.getUserById(friendshipRequestDto.receiverId.toLong())

        val friendshipRequest = FriendRequest(requester, receiver)
        friendRequestRepository.save(friendshipRequest)
    }
}
