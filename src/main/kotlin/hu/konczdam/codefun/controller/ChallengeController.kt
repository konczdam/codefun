package hu.konczdam.codefun.controller

import hu.konczdam.codefun.dataacces.PageRequest
import hu.konczdam.codefun.model.Challenge
import hu.konczdam.codefun.service.ChallengeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/challenges")
@PreAuthorize("hasRole('ADMIN')")
class ChallengeController {

    @Autowired
    private lateinit var challengeService: ChallengeService

    @GetMapping
    fun getChallenges(
            @RequestParam page: String,
            @RequestParam size: String,
            @RequestParam sortProperty: String,
            @RequestParam sortDirection: String?
    ): ResponseEntity<Page<Challenge>> {
        val pageOfChallenges = challengeService.getPageOfChallenges(PageRequest(
                page = page.toInt(),
                size = size.toInt(),
                sortProperty = sortProperty,
                sortDirection = if  (sortDirection != null) sortDirection else "asc"
        ))
        return ResponseEntity.ok(pageOfChallenges)
    }

    @GetMapping("/{challengeId}")
    fun getSingleChallenge(
            @PathVariable challengeId: String
    ): ResponseEntity<Challenge> {
        val challenge = challengeService.findById(challengeId.toLong())
        if (challenge == null) {
            return ResponseEntity.notFound().build()
        }
        return ResponseEntity.ok(challenge)
    }

    @PostMapping
    fun addChallenge(
            @RequestBody challenge: Challenge
    ): ResponseEntity<Challenge> {
        val savedChallenge: Challenge = challengeService.addChallenge(challenge)
        return ResponseEntity
                .created(URI("/challenges/${savedChallenge.id}"))
                .body(savedChallenge)
    }

//    @PatchMapping("/{challengeId}")
//    fun modifyChallenge(
//            @PathVariable challengeId: String
//    ): ResponseEntity<Challenge> {
//
//        return ResponseEntity.
//    }

//    @DeleteMapping("/challengeId")
//    fun deleteChallenge(
//            @PathVariable challengeId: String
//    ): ResponseEntity<Challenge> {

//    }
}