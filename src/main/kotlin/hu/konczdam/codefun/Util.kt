package hu.konczdam.codefun

import hu.konczdam.codefun.services.UserDetailsImpl
import org.springframework.data.domain.Sort
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import java.lang.IllegalArgumentException

fun getDirectionValueFromString(direction: String): Sort.Direction {
    try {
        return Sort.Direction.fromString(direction)
    } catch (e: IllegalArgumentException) {
        return Sort.DEFAULT_DIRECTION
    }
}

fun getUserIdFromPrincipal(principal: UsernamePasswordAuthenticationToken): Long {
    return (principal.principal as UserDetailsImpl).id
}