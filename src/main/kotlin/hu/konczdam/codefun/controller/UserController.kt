package hu.konczdam.codefun.controller;

import hu.konczdam.codefun.converter.toDto
import hu.konczdam.codefun.dataacces.PageRequest
import hu.konczdam.codefun.dataacces.PasswordUpdateRequestDto
import hu.konczdam.codefun.dataacces.UserDto
import hu.konczdam.codefun.getUserIdFromPrincipal
import hu.konczdam.codefun.model.User
import hu.konczdam.codefun.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.*
import java.lang.IllegalArgumentException
import javax.validation.Valid

@RestController
@RequestMapping("/users")
@PreAuthorize("hasRole('USER')")
class UserController {

    @Autowired
    private lateinit var userService: UserService

    @GetMapping
    fun getUsers(
        @RequestParam page: String,
        @RequestParam size: String,
        @RequestParam sortProperty: String,
        @RequestParam sortDirection: String?,
        @RequestParam name: String?,
        principal: UsernamePasswordAuthenticationToken
    ): ResponseEntity<Page<UserDto>> {
        val pageOfUsers = userService.getPageOfUsersExcludingCaller(
                PageRequest(
                    page = page.toInt(),
                    size = size.toInt(),
                    sortProperty = sortProperty,
                    sortDirection = if  (sortDirection != null) sortDirection else "asc"
                ),
                name,
                getUserIdFromPrincipal(principal)
        )
        return ResponseEntity.ok(pageOfUsers.map(User::toDto))
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: String): ResponseEntity<UserDto> {
        val dto = userService.getUserById(id.toLong()).toDto()
        return ResponseEntity.ok(dto)
    }

    @PostMapping("/changePassword")
    fun changePassword(
            @RequestBody @Valid passwordUpdateRequestDto: PasswordUpdateRequestDto,
            principal: UsernamePasswordAuthenticationToken
    ): ResponseEntity<*> {
        val userId = getUserIdFromPrincipal(principal)
        try {
            userService.changePasswordForUser(userId, passwordUpdateRequestDto)
            return ResponseEntity.ok("Password changed")
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().body(e.message)
        }
    }
}
