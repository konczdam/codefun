package hu.konczdam.codefun.service

import hu.konczdam.codefun.dataacces.PageRequest
import hu.konczdam.codefun.getDirectionValueFromString
import hu.konczdam.codefun.model.Challenge
import hu.konczdam.codefun.repository.ChallengeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.domain.PageRequest as SpringPageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.IllegalArgumentException

@Service
class ChallengeService {

    @Autowired
    private lateinit var challengeRepository: ChallengeRepository

    @Transactional(readOnly =  true)
    fun findById(id: Long): Challenge? {
        return challengeRepository.findById(id).get()
    }

    @Transactional(readOnly =  true)
    fun getRandomChallenge(): Challenge {
        return challengeRepository.getRandomChallenge(org.springframework.data.domain.PageRequest.of(0,1))[0]
    }

    @Transactional(readOnly =  true)
    fun getPageOfChallenges(pageRequest: PageRequest): Page<Challenge> {
        val sortDirection = getDirectionValueFromString(pageRequest.sortDirection)
        val springPageRequest = SpringPageRequest.of(
                pageRequest.page,
                pageRequest.size.let { if (it > 0 ) it else Int.MAX_VALUE },
                Sort.by(sortDirection, pageRequest.sortProperty)
        )
        return challengeRepository.findAll(springPageRequest)

    }

    @Transactional
    fun addChallenge(challenge: Challenge): Challenge {
        challenge.challengeTests.forEach { it.challenge = challenge }
        return challengeRepository.save(challenge)
    }

    @Transactional
    fun deleteChallenge(id: Long) {
        challengeRepository.deleteById(id)
    }

    @Transactional
    fun modifyChallenge(challengeId: Long, challenge: Challenge): Challenge {
        val challengeFromDB = challengeRepository.findByIdOrNull(challengeId)
        if (challengeFromDB == null) {
            throw Exception("challenge not found in the database!")
        }

        challenge.challengeTests.forEach { it.challenge = challenge }
        return challengeRepository.save(challenge)
    }
}