package hu.konczdam.codefun.repository

import hu.konczdam.codefun.model.Challenge
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

@Repository
@Transactional
interface ChallengeRepository: JpaRepository<Challenge, Long> {

    @Query("SELECT c from Challenge c ORDER BY function('RAND')")
    fun getRandomChallenge(): Challenge
}