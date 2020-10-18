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
        javaCodeRunnerService.executeCode(
                8L,
                "import java.util.Scanner;  public class Solution {      public static void main(String[] args) {         Scanner scanner = new Scanner(System.in);                  int a = scanner.nextInt();         int b = scanner.nextInt();          System.out.println(a+b);     } }",
                listOf(9L, 111L)
        )
    }
}