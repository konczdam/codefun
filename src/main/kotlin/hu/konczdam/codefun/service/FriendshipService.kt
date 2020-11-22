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

    @Transactional
    fun removeRequest(friendshipRequestDto: FriendshipRequestDto) {
        friendRequestRepository.deleteByRequesterIdAndReceiverId(
                requesterId = friendshipRequestDto.requesterId.toLong(),
                receiverId = friendshipRequestDto.receiverId.toLong()
        )
    }

    @Transactional
    fun acceptRequest(friendshipRequestDto: FriendshipRequestDto) {
        val friendRequest = friendRequestRepository.findByRequesterIdAndReceiverId(
                requesterId = friendshipRequestDto.requesterId.toLong(),
                receiverId = friendshipRequestDto.receiverId.toLong()
        )

        val requester = friendRequest.requester
        val receiver = friendRequest.receiver

        requester.friends.add(receiver)
        requester.friendOf.add(receiver)

        friendRequestRepository.delete(friendRequest)
    }

    @Transactional
    fun removeFriend(friendshipRequestDto: FriendshipRequestDto) {
        val user = userService.getUserById(friendshipRequestDto.receiverId.toLong())
        val requesterId = friendshipRequestDto.requesterId.toLong()
        user.friends.removeIf { it.id!!.equals(requesterId) }
        user.friendOf.removeIf { it.id!!.equals(requesterId) }
    }
}
