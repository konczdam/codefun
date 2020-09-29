package hu.konczdam.codefun

import hu.konczdam.codefun.dataacces.UserCreationDto
import hu.konczdam.codefun.model.Language
import hu.konczdam.codefun.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CodefunApplication: CommandLineRunner {

	@Autowired
	private lateinit var userService: UserService

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
	}
}

fun main(args: Array<String>) {
	runApplication<CodefunApplication>(*args)
}
