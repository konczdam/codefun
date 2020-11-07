package hu.konczdam.codefun.service

import hu.konczdam.codefun.model.Challenge
import hu.konczdam.codefun.repository.ChallengeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ChallengeService {

    @Autowired
    private lateinit var challengeRepository: ChallengeRepository

    fun findById(id: Long): Challenge? {
        return challengeRepository.findById(id).get()
    }

    fun getRandomChallenge(): Challenge {
        return challengeRepository.getRandomChallenge()
    }
}