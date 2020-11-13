package hu.konczdam.codefun.controller

import hu.konczdam.codefun.repository.ChallengeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
//@PreAuthorize("hasRole('USER')")
class TestController {

    @Autowired
    private lateinit var challengeRepository: ChallengeRepository

    @GetMapping("/test")
    @ResponseBody
    fun alma(): String {
        return "alma";
    }

    @GetMapping("/test2")
    fun asd(){
        val randomChallange = challengeRepository.getRandomChallenge()
        println("alma")
    }
}