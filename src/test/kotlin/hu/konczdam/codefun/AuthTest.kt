package hu.konczdam.codefun

import hu.konczdam.codefun.dataacces.UserCreationDto
import hu.konczdam.codefun.model.Language
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@Transactional
@ActiveProfiles("test")
class AuthTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `user can register`() {

        val userCreationDto = UserCreationDto(
                email = "example@gmail.com",
                password = "almakörte",
                username = "konczdam",
                roles = mutableSetOf("user"),
                preferredLanguages = mutableSetOf(Language.JAVA, Language.JAVASCRIPT)
        )

        val entity = restTemplate.postForEntity(
                "/auth/register",
                userCreationDto,
                String::class.java
        )
        println("aa")

    }
}