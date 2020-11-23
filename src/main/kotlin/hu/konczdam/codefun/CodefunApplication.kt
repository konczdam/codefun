package hu.konczdam.codefun

import hu.konczdam.codefun.model.ChallengeTest
import hu.konczdam.codefun.repository.ChallengeRepository
import hu.konczdam.codefun.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CodefunApplication: CommandLineRunner {

	@Autowired
	private lateinit var userService: UserService

	@Autowired
	private lateinit var challengeRepository: ChallengeRepository

	override fun run(vararg args: String?) {
//		val userCreationDto = UserCreationDto(
//			email = "konczdam98@gmail.com",
//			password = "almakörte",
//			username = "konczdam",
//			roles = mutableSetOf("admin", "user"),
//			preferredLanguages = mutableSetOf(Language.JAVA, Language.JAVASCRIPT)
//		)
//
//		userService.createUser(userCreationDto)
//		return



//		val challenge = Challenge(
//				title="Add two numbers",
//				description = "Your program gets two integers from the static input, print out the sum of them",
//				availableLanguages = mutableSetOf(Language.JAVA, Language.JAVASCRIPT)
//		)
//
//		val element = ChallengeTest(
//				input = "1 \r\n 2\r\n",
//				expectedOutput = "3",
//				displayName = "simple"
//		)
//		challenge.challengeTests.add(
//				element
//		)
//		element.challenge = challenge
//
//		challengeRepository.save(challenge)

//		val challenge = challengeRepository.getRandomChallenge()
//		val element = ChallengeTest(
//				input = "5 3",
//				expectedOutput = "8",
//
//				displayName = "hard"
//		)
//		element.challenge = challenge
//		challenge.challengeTests.add(element)
//		challengeRepository.save(challenge)

	}

}

fun main(args: Array<String>) {
	runApplication<CodefunApplication>(*args)
}
