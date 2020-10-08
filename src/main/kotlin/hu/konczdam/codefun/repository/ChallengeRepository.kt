package hu.konczdam.codefun.repository

import hu.konczdam.codefun.model.Challenge
import org.springframework.data.jpa.repository.JpaRepository

interface ChallengeRepository: JpaRepository<Challenge, Long> {
}