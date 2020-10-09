package hu.konczdam.codefun.repository

import hu.konczdam.codefun.model.ChallengeTest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

@Repository
@Transactional
interface ChallengeTestRepository: JpaRepository<ChallengeTest, Long> {

    fun findByIdAndChallengeId(id: Long, challengeId: Long): ChallengeTest?
}