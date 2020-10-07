package hu.konczdam.codefun.docker

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CodeResponseParserTest {

    @Test
    fun parseCodeRunnerOutput_testCompileErrorResponse() {
        val input = """<LOG::-Build Output>Dependencies:<:LF:><:LF:>junit:junit:4.12<:LF:>org.projectlombok:lombok:1.16.18<:LF:>org.mockito:mockito-core:2.7.19<:LF:>org.assertj:assertj-core:3.8.0<:LF:>org.xerial:sqlite-jdbc:3.19.3<:LF:><:LF:>Tasks:<:LF:><:LF:>:compileJava FAILED<:LF:>1 actionable task: 1 executed<:LF:>
/home/codewarrior/src/main/java/HelloWorld.java:1: error: cannot find symbol
class HelloWorld {public static void main(String[] args) {System.out.println(55 + new Dataaa());}}
                                                                                      ^
  symbol:   class Dataaa
  location: class HelloWorld
1 error

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':compileJava'.
> Compilation failed; see the compiler error output for details.

*"""

        val expectedOutput = input.lines().filterIndexed { index, s -> index > 0}.joinToString { it }

        Assertions.assertEquals(expectedOutput, parseCodeRunnerOutput(input).errorMessage)
    }

    @Test
    fun parseCodeRunnerOutput_testTimeoutResponse() {
        val input = """<LOG::-Build Output>Dependencies:<:LF:><:LF:>junit:junit:4.12<:LF:>org.projectlombok:lombok:1.16.18<:LF:>org.mockito:mockito-core:2.7.19<:LF:>org.assertj:assertj-core:3.8.0<:LF:>org.xerial:sqlite-jdbc:3.19.3<:LF:><:LF:>Tasks:<:LF:><:LF:>:compileJava
Process was terminated. It took longer than 1000ms to complete"""

        val expectedOutput = "Process was terminated. It took longer than 1000ms to complete"
        Assertions.assertEquals(expectedOutput, parseCodeRunnerOutput(input).errorMessage)

    }

    @Test
    fun parseCodeRunnerOutput_testResultParsing() {
        val input = """<LOG::-Build Output>Dependencies:<:LF:><:LF:>junit:junit:4.12<:LF:>org.projectlombok:lombok:1.16.18<:LF:>org.mockito:mockito-core:2.7.19<:LF:>org.assertj:assertj-core:3.8.0<:LF:>org.xerial:sqlite-jdbc:3.19.3<:LF:><:LF:>Tasks:<:LF:><:LF:>:compileJava<:LF:>:classes<:LF:>:compileTestJava<:LF:>:testClasses<:LF:>:test<:LF:><:LF:><:LF:><:LF:>BUILD<:LF:> SUCCESSFUL in 2s<:LF:>3 actionable tasks: 3 executed<:LF:>
<DESCRIBE::>test3(MyTests)
<PASSED::>Test Passed
<COMPLETEDIN::>
<DESCRIBE::>multiplicationOfZeroIntegersShouldReturnZero(MyTests)
<PASSED::>Test Passed
<COMPLETEDIN::>
<DESCRIBE::>almaTest(MyTests)
<FAILED::>expected:<1> but was:<12>
<LOG::-Exception Details>java.lang.AssertionError: expected:<1> but was:<12><:LF:>      at org.junit.Assert.fail(Assert.java:88)<:LF:>  at org.junit.Assert.failNotEquals(Assert.java:834)<:LF:>        at org.junit.Assert.assertEquals(Assert.java:645)<:LF:> at org.junit.Assert.assertEquals(Assert.java:631)<:LF:> at MyTests.almaTest(MyTests.java:1)<:LF:> at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)<:LF:>    at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)<:LF:>  at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)<:LF:>  at java.lang.reflect.Method.invoke(Method.java:498)<:LF:>       at org.junit.runners.model.FrameworkMethod${'$'}1.runReflectiveCall(FrameworkMethod.java:50)<:LF:>     at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)<:LF:>    at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)<:LF:>     at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)<:LF:>        at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)<:LF:>  at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)<:LF:>      at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)<:LF:>      at org.junit.runners.ParentRunner${'$'}3.run(ParentRunner.java:290)<:LF:>      at org.junit.runners.ParentRunner${'$'}1.schedule(ParentRunner.java:71)<:LF:>        at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)<:LF:>      at org.junit.runners.ParentRunner.access${'$'}000(ParentRunner.java:58)<:LF:>        at org.junit.runners.ParentRunner${'$'}2.evaluate(ParentRunner.java:268)<:LF:>       at org.junit.runners.ParentRunner.run(ParentRunner.java:363)<:LF:>        at org.junit.runners.Suite.runChild(Suite.java:128)<:LF:>       at org.junit.runners.Suite.runChild(Suite.java:27)<:LF:>        at org.junit.runners.ParentRunner${'$'}3.run(ParentRunner.java:290)<:LF:>    at org.junit.runners.ParentRunner${'$'}1.schedule(ParentRunner.java:71)<:LF:>        at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)<:LF:>        at org.junit.runners.ParentRunner.access${'$'}000(ParentRunner.java:58)<:LF:>        at org.junit.runners.ParentRunner${'$'}2.evaluate(ParentRunner.java:268)<:LF:>       at org.junit.runners.ParentRunner.run(ParentRunner.java:363)<:LF:>      at org.junit.runner.JUnitCore.run(JUnitCore.java:137)<:LF:>       at org.junit.runner.JUnitCore.run(JUnitCore.java:115)<:LF:>     at org.junit.runner.JUnitCore.run(JUnitCore.java:105)<:LF:>     at org.junit.runner.JUnitCore.run(JUnitCore.java:94)<:LF:>      at Start.start(Start.java:11)<:LF:>     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)<:LF:>    at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)<:LF:>    at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)<:LF:>  at java.lang.reflect.Method.invoke(Method.java:498)<:LF:>       at org.junit.runners.model.FrameworkMethod${'$'}1.runReflectiveCall(FrameworkMethod.java:50)<:LF:>   at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)<:LF:>      at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)<:LF:>     at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)<:LF:>      at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)<:LF:>    at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)<:LF:>      at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)<:LF:>      at org.junit.runners.ParentRunner${'$'}3.run(ParentRunner.java:290)<:LF:>    at org.junit.runners.ParentRunner${'$'}1.schedule(ParentRunner.java:71)<:LF:> at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)<:LF:>       at org.junit.runners.ParentRunner.access${'$'}000(ParentRunner.java:58)<:LF:>        at org.junit.runners.ParentRunner${'$'}2.evaluate(ParentRunner.java:268)<:LF:>       at org.junit.runners.ParentRunner.run(ParentRunner.java:363)<:LF:>      at org.gradle.api.internal.tasks.testing.junit.JUnitTestClassExecuter.runTestClass(JUnitTestClassExecuter.java:114)<:LF:> at org.gradle.api.internal.tasks.testing.junit.JUnitTestClassExecuter.execute(JUnitTestClassExecuter.java:57)<:LF:>     at org.gradle.api.internal.tasks.testing.junit.JUnitTestClassProcessor.processTestClass(JUnitTestClassProcessor.java:66)<:LF:>  at org.gradle.api.internal.tasks.testing.SuiteTestClassProcessor.processTestClass(SuiteTestClassProcessor.java:51)<:LF:>  at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)<:LF:>    at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)<:LF:>  at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)<:LF:>    at java.lang.reflect.Method.invoke(Method.java:498)<:LF:>       at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:35)<:LF:>   at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:24)<:LF:>   at org.gradle.internal.dispatch.ContextClassLoaderDispatch.dispatch(ContextClassLoaderDispatch.java:32)<:LF:>     at org.gradle.internal.dispatch.ProxyDispatchAdapter${"$"}DispatchingInvocationHandler.invoke(ProxyDispatchAdapter.java:93)<:LF:>    at com.sun.proxy.${"$"}Proxy1.processTestClass(Unknown Source)<:LF:> at org.gradle.api.internal.tasks.testing.worker.TestWorker.processTestClass(TestWorker.java:109)<:LF:>  at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)<:LF:>      at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)<:LF:>  at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)<:LF:>  at java.lang.reflect.Method.invoke(Method.java:498)<:LF:>       at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:35)<:LF:>     at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:24)<:LF:>   at org.gradle.internal.remote.internal.hub.MessageHubBackedObjectConnection${"$"}DispatchWrapper.dispatch(MessageHubBackedObjectConnection.java:146)<:LF:>   at org.gradle.internal.remote.internal.hub.MessageHubBackedObjectConnection${"$"}DispatchWrapper.dispatch(MessageHubBackedObjectConnection.java:128)<:LF:>     at org.gradle.internal.remote.internal.hub.MessageHub${"$"}Handler.run(MessageHub.java:404)<:LF:>    at org.gradle.internal.concurrent.ExecutorPolicy\${"$"}CatchAndRecordFailures.onExecute(ExecutorPolicy.java:63)<:LF:> at org.gradle.internal.concurrent.StoppableExecutorImpl${'$'}1.run(StoppableExecutorImpl.java:46)<:LF:>        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)<:LF:>        at java.util.concurrent.ThreadPoolExecutor${"$"}Worker.run(ThreadPoolExecutor.java:617)<:LF:>        at org.gradle.internal.concurrent.ThreadFactoryImpl${"$"}ManagedThreadRunnable.run(ThreadFactoryImpl.java:55)<:LF:>    at java.lang.Thread.run(Thread.java:748)<:LF:>
<COMPLETEDIN::>"""

        val parseResponse = parseCodeRunnerOutput(input)

        Assertions.assertEquals(3, parseResponse.testResults.size)

        val firstTestResult = parseResponse.testResults[0]

        Assertions.assertEquals("test3", firstTestResult.testName)
        Assertions.assertTrue(firstTestResult.passed)
        Assertions.assertNull(firstTestResult.errorMessage)

        val secondTestResult = parseResponse.testResults[1]

        Assertions.assertEquals("multiplicationOfZeroIntegersShouldReturnZero", secondTestResult.testName)
        Assertions.assertTrue(secondTestResult.passed)
        Assertions.assertNull(secondTestResult.errorMessage)

        val thirdTestResult = parseResponse.testResults[2]

        Assertions.assertEquals("almaTest", thirdTestResult.testName)
        Assertions.assertFalse(thirdTestResult.passed)
        Assertions.assertEquals("expected:<1> but was:<12>", thirdTestResult.errorMessage)

    }

}