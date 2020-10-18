package hu.konczdam.codefun.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@PreAuthorize("hasRole('USER')")
class TestController {

    @GetMapping("/test")
    @ResponseBody
    fun alma(): String {
        return "alma";
    }
}