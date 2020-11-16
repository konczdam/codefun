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

    fun test() {
        javaCodeRunnerService.executeCode(
                8L,
                "import java.util.Scanner;  public class Solution {      public static void main(String[] args) {         Scanner scanner = new Scanner(System.in);                  int a = scanner.nextInt();         int b = scanner.nextInt();          System.out.println(a+b);     } }",
                listOf(9L, 111L)
        )
    }

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