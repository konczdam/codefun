package hu.konczdam.codefun.controller

import hu.konczdam.codefun.dataacces.FriendshipRequestDto
import hu.konczdam.codefun.service.FriendshipService
import hu.konczdam.codefun.service.UserService
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
    private lateinit var userService: UserService

    @Autowired
    private lateinit var friendshipService: FriendshipService

    @PostMapping("/addRequest")
    fun addFriendRequest(
            @RequestBody friendshipRequestDto: FriendshipRequestDto
    ): ResponseEntity<*> {
        friendshipService.addFriendshipRequest(friendshipRequestDto)
        return ResponseEntity.ok("friendshirequest added!")
    }

}