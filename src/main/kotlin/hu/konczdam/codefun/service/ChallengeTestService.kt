package hu.konczdam.codefun.service

import hu.konczdam.codefun.model.ChallengeTest
import hu.konczdam.codefun.repository.ChallengeTestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ChallengeTestService {

    @Autowired
    private lateinit var challengeTestRepository: ChallengeTestRepository

    fun findById(id: Long): ChallengeTest? {
        return challengeTestRepository.findById(id).orElse(null)
    }
}