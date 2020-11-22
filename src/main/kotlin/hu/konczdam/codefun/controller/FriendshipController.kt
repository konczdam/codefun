package hu.konczdam.codefun.controller

import hu.konczdam.codefun.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/friendship")
@PreAuthorize("hasRole('USER')")
class FriendshipController {

    @Autowired
    private lateinit var userService: UserService


}