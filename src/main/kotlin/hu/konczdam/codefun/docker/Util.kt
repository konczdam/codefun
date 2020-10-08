package hu.konczdam.codefun.docker

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

fun exec(cmd: String, stdIn: String = "", captureOutput: Boolean = false, workingDir: File = File(".")): String? {
    try {
        val process = ProcessBuilder(*cmd.split("\\s".toRegex()).toTypedArray())
                .directory(workingDir)
                .redirectOutput(if (captureOutput) ProcessBuilder.Redirect.PIPE else ProcessBuilder.Redirect.INHERIT)
                .redirectError(if (captureOutput) ProcessBuilder.Redirect.PIPE else ProcessBuilder.Redirect.INHERIT)
                .start().apply {
                    if (stdIn != "") {
                        outputStream.bufferedWriter().apply {
                            write(stdIn)
                            flush()
                            close()
                        }
                    }
                    waitFor(60, TimeUnit.SECONDS)
                }
        if (captureOutput) {
            return process.inputStream.bufferedReader().readText()
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return null
}

data class ParseResponse(
        val errorMessage: String? = null,
        val testResults: MutableList<TestResult> = mutableListOf()
)

data class TestResult(
        val testName: String,
        val passed: Boolean,
        val errorMessage: String?
)

fun parseCodeRunnerOutput(output: String): ParseResponse {
    var lines = output.lines()

    if (lines[0].contains("compileJava FAILED")) {
        lines = lines.filterIndexed { index, s -> index > 0 }

        return ParseResponse(
                errorMessage = lines.joinToString { it -> it }
        )
    }

    if (lines[1].startsWith("Process was terminated. It took longer than")) {
        return ParseResponse(
                errorMessage = lines[1]
        )
    }

    val response = ParseResponse()
    lines.forEachIndexed { index, s ->
        if (s.startsWith("<DESCRIBE::>")) {
            val testName = s.substring(
                    startIndex = 1 + s.indexOfFirst { it.equals('>') },
                    endIndex = s.indexOfFirst { it.equals('(') }
            )
            val nextLine = lines[index + 1]
            val passed = nextLine.startsWith("<PASSED::>")
            var errorMessage: String? = null
            if (!passed) {
                errorMessage = nextLine.substring(1 + nextLine.indexOfFirst { it.equals('>') })
            }
            response.testResults.add(
                    TestResult(
                            testName = testName,
                            passed = passed,
                            errorMessage = errorMessage
                    )
            )
        }
    }

    return response
}