package hu.konczdam.codefun.docker.service

import hu.konczdam.codefun.docker.ParseResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class CodeExecutorManagerService {

    @Autowired
    @Qualifier("JAVA")
    private lateinit var javaCodeRunnerService: CodeRunnerService

    fun executeJavaCode(
            challengeId: Long,
            code: String,
            testIds: List<Long>
    ): ParseResponse {
        return javaCodeRunnerService.executeCode(
                challengeId, code, testIds
        )
    }
}