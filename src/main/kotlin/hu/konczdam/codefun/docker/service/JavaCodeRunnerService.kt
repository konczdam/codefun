package hu.konczdam.codefun.docker.service

import hu.konczdam.codefun.docker.exec
import hu.konczdam.codefun.docker.parseCodeRunnerOutput
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.concurrent.Semaphore
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
@Qualifier("JAVA")
class JavaCodeRunnerService: CodeRunnerService {

    @Value("\${konczdam.app.coderunnerexecutorcount.java}")
    private var numberOfJavaExecutorDockerContainers: Int = 0

    private lateinit var semaphore: Semaphore

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

    override fun executeCode(challengeId: Long, code: String, testIds: List<Long>) {
        semaphore.acquire()
        var containerId: String
        synchronized(this) {
            containerId = runnerContainerIds.removeAt(0)
        }
        println("starting code execution")
        val cmd = "docker exec $containerId node run -l java -c \"import java.util.Scanner;  public class InputTest {      public static void main(String[] args) {         Scanner scanner = new Scanner(System.in);                  int a = scanner.nextInt();         int b = scanner.nextInt();          System.out.println(a+b);     } } \" -f \"import org.junit.Assert;         import org.junit.Test;  import java.io.*;  public class InputTestTest {      @Test     public void test() throws InterruptedException, IOException {         String input = \\\"3 5\\\";         System.setIn(new ByteArrayInputStream(input.getBytes()));           PrintStream printStream = new PrintStream(new File(\\\"aaa.txt\\\"));         PrintStream pr = System.out;         System.setOut(printStream);          InputTest.main(new String[]{});         Thread.sleep(400);         System.setOut(pr);         String finalString = new BufferedReader(new InputStreamReader(new FileInputStream(new File(\\\"aaa.txt\\\")))).readLine();         Assert.assertEquals(\\\"8\\\", finalString);     } }   \""
        val result = exec( cmd = cmd, captureOutput = true)
        println("Code Executed: $containerId")
        if (result != null) {
            val parseResult = parseCodeRunnerOutput(result)
            println(result)
        }
        synchronized(this) {
            runnerContainerIds.add(containerId)
        }
        semaphore.release()
    }

}