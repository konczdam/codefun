package hu.konczdam.codefun.service

import hu.konczdam.codefun.dataacces.PageRequest
import hu.konczdam.codefun.model.Challenge
import hu.konczdam.codefun.repository.ChallengeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.domain.PageRequest as SpringPageRquest
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException

@Service
class ChallengeService {

    @Autowired
    private lateinit var challengeRepository: ChallengeRepository

    fun findById(id: Long): Challenge? {
        return challengeRepository.findById(id).get()
    }

    fun getRandomChallenge(): Challenge {
        return challengeRepository.getRandomChallenge(org.springframework.data.domain.PageRequest.of(0,1))[0]
    }

    fun getPageOfChallenges(pageRequest: PageRequest): Page<Challenge> {
        val sortDirection = getDirectionValueFromString(pageRequest.sortDirection)
        val springPageRequest = SpringPageRquest.of(
                pageRequest.page,
                pageRequest.size.let { if (it > 0 ) it else Int.MAX_VALUE },
                Sort.by(sortDirection, pageRequest.sortProperty)
        )
        return challengeRepository.findAll(springPageRequest)

    }

    private fun getDirectionValueFromString(direction: String): Sort.Direction {
        try {
            return Sort.Direction.fromString(direction)
        } catch (e: IllegalArgumentException) {
            return Sort.DEFAULT_DIRECTION
        }
    }

    fun addChallenge(challenge: Challenge): Challenge {
        challenge.challengeTests.forEach { it.challenge = challenge }
        return challengeRepository.save(challenge)
    }

    fun deleteChallenge(id: Long) {
        challengeRepository.deleteById(id)
    }

    fun modifyChallenge(challengeId: Long, challenge: Challenge): Challenge {
        val challengeFromDB = challengeRepository.findByIdOrNull(challengeId)
        if (challengeFromDB == null) {
            throw Exception("challenge not found in the database!")
        }

        challenge.challengeTests.forEach { it.challenge = challenge }
        return challengeRepository.save(challenge)
    }
}