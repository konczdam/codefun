package hu.konczdam.codefun.controller

import hu.konczdam.codefun.config.jwt.JwtUtils
import hu.konczdam.codefun.dataacces.LoginDto
import hu.konczdam.codefun.dataacces.UserCreationDto
import hu.konczdam.codefun.docker.service.CodeExecutorManagerService
import hu.konczdam.codefun.payload.response.JwtResponse
import hu.konczdam.codefun.service.UserService
import hu.konczdam.codefun.services.UserDetailsImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.net.URI
import javax.validation.Valid

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
@RequestMapping("/auth")
class AuthController {

    @Autowired
    private lateinit var authenticationManager: AuthenticationManager

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var jwtUtils: JwtUtils

    @Autowired
    private lateinit var codeExecutorManagerService: CodeExecutorManagerService

    @PostMapping
    fun authenticateUser(
            @RequestBody @Valid loginDto: LoginDto
    ) : ResponseEntity<JwtResponse> {

        val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                        loginDto.email,
                        loginDto.password
                )
        )

        SecurityContextHolder.getContext().authentication = authentication

        val jwt = jwtUtils.generateJwtToken(authentication)
        val userDetails = authentication.principal as UserDetailsImpl
        val roles = userDetails.authorities.map { it.authority }

        return ResponseEntity.ok(
                JwtResponse(
                        token = jwt,
                        id = userDetails.id,
                        username = userDetails.username,
                        email = userDetails.email,
                        roles = roles
                )
        )
    }

    @PostMapping("/register")
    fun registerUser(
            @RequestBody @Valid userCreationDto: UserCreationDto
    ): ResponseEntity<*> {
        val user = userService.createUser(userCreationDto)
        return ResponseEntity.created(URI("/api/user/${user.id}")).body("User created!")
    }

    @PostMapping("/test")
    fun test() {
        codeExecutorManagerService.test()
    }
}