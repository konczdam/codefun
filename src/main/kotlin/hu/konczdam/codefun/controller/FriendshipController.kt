package hu.konczdam.codefun.controller

import hu.konczdam.codefun.dataacces.FriendshipRequestDto
import hu.konczdam.codefun.service.FriendshipService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/friendship")
@PreAuthorize("hasRole('USER')")
class FriendshipController {

    @Autowired
    private lateinit var friendshipService: FriendshipService

    @PostMapping("/addRequest")
    fun addFriendRequest(
            @RequestBody friendshipRequestDto: FriendshipRequestDto
    ): ResponseEntity<*> {
        friendshipService.addFriendshipRequest(friendshipRequestDto)
        return ResponseEntity.ok("friendshirequest added!")
    }

    @PostMapping("/cancelRequest")
    fun cancelRequest(
            @RequestBody friendshipRequestDto: FriendshipRequestDto
    ): ResponseEntity<*> {
        friendshipService.removeRequest(friendshipRequestDto)
        return ResponseEntity.ok("request was successfully cancelled!")
    }

    @PostMapping("/rejectRequest")
    fun rejectRequest(
            @RequestBody friendshipRequestDto: FriendshipRequestDto
    ): ResponseEntity<*> {
        friendshipService.removeRequest(friendshipRequestDto)
        return ResponseEntity.ok("request was successfully rejected!")

    }

    @PostMapping("/acceptRequest")
    fun acceptRequest(
            @RequestBody friendshipRequestDto: FriendshipRequestDto
    ): ResponseEntity<*> {
        friendshipService.acceptRequest(friendshipRequestDto)
        return ResponseEntity.ok("request was successfully accepted!")
    }

    @PostMapping("/removeFriend")
    fun removeFriend(
            @RequestBody friendshipRequestDto: FriendshipRequestDto
    ): ResponseEntity<*> {
        friendshipService.removeFriend(friendshipRequestDto)
        return ResponseEntity.ok("User deleted from friend list")
    }
}