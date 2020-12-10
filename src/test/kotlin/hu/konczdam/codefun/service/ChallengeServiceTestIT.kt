package hu.konczdam.codefun.service

import hu.konczdam.codefun.dataacces.PageRequest
import hu.konczdam.codefun.model.Challenge
import hu.konczdam.codefun.model.ChallengeTest
import hu.konczdam.codefun.repository.ChallengeRepository
import hu.konczdam.codefun.repository.ChallengeTestRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
class ChallengeServiceTestIT {

    @Autowired
    private lateinit var challengeService: ChallengeService

    @Autowired
    private lateinit var challengeRepository: ChallengeRepository

    @Autowired
    private lateinit var challengeTestRepository: ChallengeTestRepository


    @Test
    fun addChallenge_test() {
        var challenge = createChallenge("aadsweqwe")
        challenge = challengeService.addChallenge(challenge)
        val challengeFromDb = challengeRepository.findById(challenge.id!!)
        Assertions.assertEquals(challengeFromDb.get().id, challenge.id)
        challengeRepository.delete(challenge)
    }

    @Test
    fun getPageOfChallenges_test() {
        val challenge1 = challengeService.addChallenge(createChallenge("a"))
        val challenge2 = challengeService.addChallenge(createChallenge("b"))
        val challenge3 = challengeService.addChallenge(createChallenge("c"))

        val pageRequest = PageRequest(
                page = 0,
                size = 2,
                sortProperty = "title"
        )

        val page = challengeService.getPageOfChallenges(pageRequest)

        Assertions.assertEquals(2, page.totalPages)
        Assertions.assertEquals(3, page.totalElements)

        val challenges = page.content
        Assertions.assertEquals(challenge1.id, challenges[0].id)
        Assertions.assertEquals(challenge2.id, challenges[1].id)

        challengeRepository.delete(challenge1)
        challengeRepository.delete(challenge2)
        challengeRepository.delete(challenge3)
    }

    @Test
    fun getPageOfChallenges_reverseOrder_test() {
        val challenge1 = challengeService.addChallenge(createChallenge("a"))
        val challenge2 = challengeService.addChallenge(createChallenge("b"))
        val challenge3 = challengeService.addChallenge(createChallenge("c"))

        val pageRequest = PageRequest(
                page = 0,
                size = 2,
                sortProperty = "title",
                sortDirection = "desc"
        )

        val page = challengeService.getPageOfChallenges(pageRequest)

        Assertions.assertEquals(2, page.totalPages)
        Assertions.assertEquals(3, page.totalElements)

        val challenges = page.content
        Assertions.assertEquals(challenge3.id, challenges[0].id)
        Assertions.assertEquals(challenge2.id, challenges[1].id)

        challengeRepository.delete(challenge1)
        challengeRepository.delete(challenge2)
        challengeRepository.delete(challenge3)
    }


    @Test
    fun modifyChallenge_test() {
        var challenge = challengeService.addChallenge(createChallenge("a"))

        val newChallengeTest = ChallengeTest(
                input = "b",
                displayName = "b",
                expectedOutput = "b"
        )
        challenge.challengeTests.add(newChallengeTest)

        challenge = challengeService.modifyChallenge(challenge.id!!, challenge)
        Assertions.assertEquals(3, challenge.challengeTests.size)
        Assertions.assertEquals("b", challenge.challengeTests[2].input)

        challengeRepository.delete(challenge)
    }


    private fun createChallenge(seed: String): Challenge {
        val challenge = Challenge(
                title = seed + "title",
                description = seed + "description"
        )
        challenge.challengeTests.add(
                ChallengeTest(
                        input = seed + "input1",
                        expectedOutput = seed + "output1",
                        displayName = seed + "displayName1"
                )
        )
        challenge.challengeTests.add(
                ChallengeTest(
                        input = seed + "input2",
                        expectedOutput = seed + "output2",
                        displayName = seed + "displayName2"
                )
        )
        return challenge
    }
}