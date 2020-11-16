package hu.konczdam.codefun.docker.service

import hu.konczdam.codefun.docker.ParseResponse
import hu.konczdam.codefun.docker.exec
import hu.konczdam.codefun.docker.parseCodeRunnerOutput
import hu.konczdam.codefun.model.ChallengeTest
import hu.konczdam.codefun.service.ChallengeService
import hu.konczdam.codefun.service.ChallengeTestService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
@Qualifier("JAVA")
class JavaCodeRunnerService : CodeRunnerService {

    @Value("\${konczdam.app.coderunnerexecutorcount.java}")
    private var numberOfJavaExecutorDockerContainers: Int = 0

    @Autowired
    private lateinit var challengeService: ChallengeService

    @Autowired
    private lateinit var challengeTestService: ChallengeTestService

    private lateinit var semaphore: Semaphore

    private val atomicInteger = AtomicInteger()

    val runnerContainerIds = mutableListOf<String>()

    @PostConstruct
    private fun initializeJavaExecutor() {

        repeat(numberOfJavaExecutorDockerContainers) {
            val response = exec(cmd = "docker run -d -it codewars/java-runner", captureOutput = true)
            if (response != null)
                runnerContainerIds.add(response)
        }

        semaphore = Semaphore(numberOfJavaExecutorDockerContainers, true)
    }

    @PreDestroy
    private fun stopDockerContainers() {
        for (id in runnerContainerIds) {
            println("stopping container with id: $id")
            exec(cmd = "docker stop $id", captureOutput = false)
        }
    }

    override fun executeCode(
            challengeId: Long,
            code: String,
            testIds: List<Long>
    ): ParseResponse {
        var cmd = createCommand(challengeId, code, testIds)
        semaphore.acquire()
        var containerId: String
        synchronized(this) {
            containerId = runnerContainerIds.removeAt(0)
        }
        println("starting code execution")
        cmd = cmd.replace("{{containerId}}", containerId)
        val result = exec(cmd = cmd, captureOutput = true)
        println("Code Executed: $containerId")
        lateinit var parseResult: ParseResponse
        if (result != null) {
            parseResult = parseCodeRunnerOutput(result)
            println(result)
        }
        synchronized(this) {
            runnerContainerIds.add(containerId)
        }
        semaphore.release()
        return parseResult
    }

    private fun createCommand(challengeId: Long, code: String, testIds: List<Long>): String {
        val challange = challengeService.findById(challengeId)
        if (challange == null) {
            throw Exception("Challenge not found in database")
        }

        val tests = mutableListOf<ChallengeTest>()

        testIds.forEach {
            val challengeTest = challengeTestService.findByIdAndChallengeId(it, challengeId)
            if (challengeTest == null) {
                throw Exception("ChallengeTest not found in database!")
            }
            tests.add(challengeTest)
        }

        val escapedCode = prepareCode(code).replace("\"", "\\\"")
        var result = "docker exec {{containerId}} node run -l java -c \"$escapedCode\" -f "

        var testCode = "\"import org.junit.Assert;         import org.junit.Test;  import java.io.*;  public class SolutionTest${atomicInteger.incrementAndGet()} {  "

        tests.forEach {
            val testCase = "@Test     public void ${it.displayName}() throws InterruptedException, IOException {         String input = \\\"${it.input}\\\";         System.setIn(new ByteArrayInputStream(input.getBytes()));           PrintStream printStream = new PrintStream(new File(\\\"aaa.txt\\\"));         PrintStream pr = System.out;         System.setOut(printStream);          Solution.main(new String[]{});          System.setOut(pr);         String finalString = new BufferedReader(new InputStreamReader(new FileInputStream(new File(\\\"aaa.txt\\\")))).readLine();         Assert.assertEquals(\\\"${it.expectedOutput}\\\", finalString);     }"
            testCode += testCase
        }
        result = result + testCode + "}\""
        return result
    }

    private fun prepareCode(code: String): String {
        return code.split("\n")
                .asSequence()
                .map { it.trim() }
                .filter { !it.startsWith("//") }
                .map {
                    if (it.contains("//")) {
                        it.substring(0, it.indexOf("//"))
                    } else {
                        it
                    }
                }
                .joinToString(separator = " ") { it }

    }

}