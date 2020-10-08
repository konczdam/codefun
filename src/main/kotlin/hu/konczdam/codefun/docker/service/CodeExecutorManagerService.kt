package hu.konczdam.codefun.docker.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class CodeExecutorManagerService {

    @Autowired
    @Qualifier("JAVA")
    private lateinit var javaCodeRunnerService: CodeRunnerService

    fun test() {
        javaCodeRunnerService.executeCode(123L, "aasdasd", listOf())
    }
}