package hu.konczdam.codefun.docker

import hu.konczdam.codefun.docker.CodeRunnerUtil.Companion.parseCodeRunnerOutput
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CodeResponseParserTest {

    @Test
    fun parseCodeRunnerOutput_testCompileErrorResponse() {
        val input = """
            {
               "stdout":"<LOG::-Build Output>Dependencies:<:LF:><:LF:>junit:junit:4.12<:LF:>org.projectlombok:lombok:1.16.18<:LF:>org.mockito:mockito-core:2.7.19<:LF:>org.assertj:assertj-core:3.8.0<:LF:>org.xerial:sqlite-jdbc:3.19.3<:LF:><:LF:>Tasks:<:LF:><:LF:>:compileJava FAILED<:LF:>1 actionable task: 1 executed<:LF:>\n",
               "stderr":"/home/codewarrior/src/main/java/Solution.java:1: error: cannot find symbol\nimport java.util.Scanner;  class Solution {     public static void main(String[] args) {         Scannersasasa scanner = new Scanner(System.in);  System.out.println(3);  } }\n                                                                                                 ^\n  symbol:   class Scannersasasa\n  location: class Solution\n1 error\n\nFAILURE: Build failed with an exception.\n\n* What went wrong:\nExecution failed for task ':compileJava'.\n> Compilation failed; see the compiler error output for details.\n\n*",
               "exitCode":1,
               "exitSignal":null,
               "wallTime":1186,
               "outputType":"pre"
            }
        """.trimIndent()

        val expectedOutput = "error: cannot find symbol"

        val parseCodeRunnerOutput = parseCodeRunnerOutput(input)
        Assertions.assertNotNull(parseCodeRunnerOutput.errorMessage)
        Assertions.assertTrue(parseCodeRunnerOutput.errorMessage!!.startsWith(expectedOutput))
    }

    @Test
    fun parseCodeRunnerOutput_testTimeoutResponse() {
        val input = """
            {
               "stdout":"<LOG::-Build Output>Dependencies:<:LF:><:LF:>junit:junit:4.12<:LF:>org.projectlombok:lombok:1.16.18<:LF:>org.mockito:mockito-core:2.7.19<:LF:>org.assertj:assertj-core:3.8.0<:LF:>org.xerial:sqlite-jdbc:3.19.3<:LF:><:LF:>Tasks:<:LF:><:LF:>:compileJava UP-TO-DATE<:LF:>:classes<:LF:>:compileTestJava UP-TO-DATE<:LF:>:testClasses<:LF:>:test UP-TO-DATE<:LF:><:LF:><:LF:>BUILD<:LF:> SUCCESSFUL in 0s<:LF:>3 actionable tasks: 3 up-to-date<:LF:>\n",
               "stderr":"Process was terminated. It took longer than 1000ms to complete\n",
               "status":"max_time_reached",
               "exitSignal":"SIGKILL",
               "outputType":"pre"
            }
        """.trimIndent()

        val expectedOutput = "Process was terminated. It took longer than 1000ms to complete"
        Assertions.assertEquals(expectedOutput, parseCodeRunnerOutput(input).errorMessage?.trim())

    }

    @Test
    fun parseCodeRunnerOutput_testResultParsing() {
        val input = """
            {
               "stdout":"<LOG::-Build Output>Dependencies:<:LF:><:LF:>junit:junit:4.12<:LF:>org.projectlombok:lombok:1.16.18<:LF:>org.mockito:mockito-core:2.7.19<:LF:>org.assertj:assertj-core:3.8.0<:LF:>org.xerial:sqlite-jdbc:3.19.3<:LF:><:LF:>Tasks:<:LF:><:LF:>:compileJava UP-TO-DATE<:LF:>:classes<:LF:>:compileTestJava UP-TO-DATE<:LF:>:testClasses<:LF:>:test<:LF:><:LF:><:LF:><:LF:>BUILD<:LF:> SUCCESSFUL in 1s<:LF:>3 actionable tasks: 1 executed, 2 up-to-date<:LF:>\n<DESCRIBE::>Simple(SolutionTest2)\n<PASSED::>Test Passed\n<COMPLETEDIN::>\n<DESCRIBE::>Hard(SolutionTest2)\n<FAILED::>expected:<[44]> but was:<[3]>\n<LOG::-Exception Details>org.junit.ComparisonFailure: expected:<[44]> but was:<[3]><:LF:>\tat org.junit.Assert.assertEquals(Assert.java:115)<:LF:>\tat org.junit.Assert.assertEquals(Assert.java:144)<:LF:>\tat SolutionTest2.Hard(SolutionTest2.java:1)<:LF:>\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)<:LF:>\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)<:LF:>\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)<:LF:>\tat java.lang.reflect.Method.invoke(Method.java:498)<:LF:>\tat org.junit.runners.model.FrameworkMethod${'$'}1.runReflectiveCall(FrameworkMethod.java:50)<:LF:>\tat org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)<:LF:>\tat org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)<:LF:>\tat org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)<:LF:>\tat org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)<:LF:>\tat org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)<:LF:>\tat org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)<:LF:>\tat org.junit.runners.ParentRunner${'$'}3.run(ParentRunner.java:290)<:LF:>\tat org.junit.runners.ParentRunner${'$'}1.schedule(ParentRunner.java:71)<:LF:>\tat org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)<:LF:>\tat org.junit.runners.ParentRunner.access${'$'}000(ParentRunner.java:58)<:LF:>\tat org.junit.runners.ParentRunner${'$'}2.evaluate(ParentRunner.java:268)<:LF:>\tat org.junit.runners.ParentRunner.run(ParentRunner.java:363)<:LF:>\tat org.junit.runners.Suite.runChild(Suite.java:128)<:LF:>\tat org.junit.runners.Suite.runChild(Suite.java:27)<:LF:>\tat org.junit.runners.ParentRunner${'$'}3.run(ParentRunner.java:290)<:LF:>\tat org.junit.runners.ParentRunner${'$'}1.schedule(ParentRunner.java:71)<:LF:>\tat org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)<:LF:>\tat org.junit.runners.ParentRunner.access${'$'}000(ParentRunner.java:58)<:LF:>\tat org.junit.runners.ParentRunner${'$'}2.evaluate(ParentRunner.java:268)<:LF:>\tat org.junit.runners.ParentRunner.run(ParentRunner.java:363)<:LF:>\tat org.junit.runner.JUnitCore.run(JUnitCore.java:137)<:LF:>\tat org.junit.runner.JUnitCore.run(JUnitCore.java:115)<:LF:>\tat org.junit.runner.JUnitCore.run(JUnitCore.java:105)<:LF:>\tat org.junit.runner.JUnitCore.run(JUnitCore.java:94)<:LF:>\tat Start.start(Start.java:11)<:LF:>\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)<:LF:>\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)<:LF:>\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)<:LF:>\tat java.lang.reflect.Method.invoke(Method.java:498)<:LF:>\tat org.junit.runners.model.FrameworkMethod${'$'}1.runReflectiveCall(FrameworkMethod.java:50)<:LF:>\tat org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)<:LF:>\tat org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)<:LF:>\tat org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)<:LF:>\tat org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)<:LF:>\tat org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)<:LF:>\tat org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)<:LF:>\tat org.junit.runners.ParentRunner${'$'}3.run(ParentRunner.java:290)<:LF:>\tat org.junit.runners.ParentRunner${'$'}1.schedule(ParentRunner.java:71)<:LF:>\tat org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)<:LF:>\tat org.junit.runners.ParentRunner.access${'$'}000(ParentRunner.java:58)<:LF:>\tat org.junit.runners.ParentRunner${'$'}2.evaluate(ParentRunner.java:268)<:LF:>\tat org.junit.runners.ParentRunner.run(ParentRunner.java:363)<:LF:>\tat org.gradle.api.internal.tasks.testing.junit.JUnitTestClassExecuter.runTestClass(JUnitTestClassExecuter.java:114)<:LF:>\tat org.gradle.api.internal.tasks.testing.junit.JUnitTestClassExecuter.execute(JUnitTestClassExecuter.java:57)<:LF:>\tat org.gradle.api.internal.tasks.testing.junit.JUnitTestClassProcessor.processTestClass(JUnitTestClassProcessor.java:66)<:LF:>\tat org.gradle.api.internal.tasks.testing.SuiteTestClassProcessor.processTestClass(SuiteTestClassProcessor.java:51)<:LF:>\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)<:LF:>\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)<:LF:>\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)<:LF:>\tat java.lang.reflect.Method.invoke(Method.java:498)<:LF:>\tat org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:35)<:LF:>\tat org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:24)<:LF:>\tat org.gradle.internal.dispatch.ContextClassLoaderDispatch.dispatch(ContextClassLoaderDispatch.java:32)<:LF:>\tat org.gradle.internal.dispatch.ProxyDispatchAdapter            ${"DispatchingInvocationHandler"}            .invoke(ProxyDispatchAdapter.java:93)<:LF:>\tat com.sun.proxy.            ${"Proxy1"}            .processTestClass(Unknown Source)<:LF:>\tat org.gradle.api.internal.tasks.testing.worker.TestWorker.processTestClass(TestWorker.java:109)<:LF:>\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)<:LF:>\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)<:LF:>\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)<:LF:>\tat java.lang.reflect.Method.invoke(Method.java:498)<:LF:>\tat org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:35)<:LF:>\tat org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:24)<:LF:>\tat org.gradle.internal.remote.internal.hub.MessageHubBackedObjectConnection            ${"DispatchWrapper"}            .dispatch(MessageHubBackedObjectConnection.java:146)<:LF:>\tat org.gradle.internal.remote.internal.hub.MessageHubBackedObjectConnection            ${"DispatchWrapper"}            .dispatch(MessageHubBackedObjectConnection.java:128)<:LF:>\tat org.gradle.internal.remote.internal.hub.MessageHub            ${"Handler"}            .run(MessageHub.java:404)<:LF:>\tat org.gradle.internal.concurrent.ExecutorPolicy            ${"CatchAndRecordFailures"}            .onExecute(ExecutorPolicy.java:63)<:LF:>\tat org.gradle.internal.concurrent.StoppableExecutorImpl${'$'}1.run(StoppableExecutorImpl.java:46)<:LF:>\tat java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)<:LF:>\tat java.util.concurrent.ThreadPoolExecutor            ${"Worker"}            .run(ThreadPoolExecutor.java:617)<:LF:>\tat org.gradle.internal.concurrent.ThreadFactoryImpl            ${"ManagedThreadRunnable"}            .run(ThreadFactoryImpl.java:55)<:LF:>\tat java.lang.Thread.run(Thread.java:748)<:LF:>\n<COMPLETEDIN::>\n",
               "stderr":"",
               "exitCode":0,
               "exitSignal":null,
               "wallTime":1680,
               "outputType":"pre"
            }
        """.trimIndent()

        val parseResponse = parseCodeRunnerOutput(input)

        Assertions.assertEquals(2, parseResponse.testResults.size)
        Assertions.assertEquals(1680, parseResponse.timeTaken)

        val firstTestResult = parseResponse.testResults[0]

        Assertions.assertEquals("Simple", firstTestResult.testName)
        Assertions.assertTrue(firstTestResult.passed)
        Assertions.assertNull(firstTestResult.errorMessage)

        val secondTestResult = parseResponse.testResults[1]

        Assertions.assertEquals("Hard", secondTestResult.testName)
        Assertions.assertFalse(secondTestResult.passed)
        Assertions.assertEquals("expected:<[44]> but was:<[3]>", secondTestResult.errorMessage)

    }

}