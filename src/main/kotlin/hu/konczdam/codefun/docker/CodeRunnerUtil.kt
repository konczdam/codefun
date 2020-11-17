package hu.konczdam.codefun.docker

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.aspectj.apache.bcel.classfile.Code
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

data class ParseResponse(
        val errorMessage: String? = null,
        val testResults: MutableList<TestResult> = mutableListOf(),
        val timeTaken: Int = -1
)

data class TestResult(
        val testName: String,
        val passed: Boolean,
        val errorMessage: String?
)


data class codeExecutorResponse constructor(
        @JsonProperty("stdout") val stdout: String,
        @JsonProperty("stderr") val stderr: String,
        @JsonProperty("exitCode") val exitCode: Int? = null,
        @JsonProperty("wallTime") val wallTime: Int? = null,
        @JsonProperty("outputType") val outputType: String,
        @JsonProperty("exitSignal") val exitSignal: String? = null,
        @JsonProperty("status") val status: String? = null
)

class CodeRunnerUtil {

    companion object {
        private val objectMapper = ObjectMapper()

        private val logger = LoggerFactory.getLogger(Code::class.java)

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

        fun parseCodeRunnerOutput(output: String): ParseResponse {

            // create object from json response
            val codeExecutorResponse = objectMapper.readValue(output, codeExecutorResponse::class.java)

            codeExecutorResponse.status?.let {
                if (it == "max_time_reached") {
                    return ParseResponse(errorMessage = codeExecutorResponse.stderr)
                }
            }

            // at this point the exitCode must not be null
            if (codeExecutorResponse.exitCode == null) {
                logger.error("unexpected null value in codeExecutorResponse.exitCode! {0}", codeExecutorResponse)
                throw Exception("unexpected null value in codeExecutorResponse.exitCode")
            }

            if (codeExecutorResponse.exitCode == 1) {
                // compile error
                codeExecutorResponse.stderr.let {
                    val indexOfError = it.indexOf("error")
                    val errorMessage = it.substring(indexOfError)
                    return ParseResponse(errorMessage)
                }
            }

            // code executed successfully, output can be parsed
            val stdout = codeExecutorResponse.stdout.lines()

            // at this point the wallTime must not be null
            if (codeExecutorResponse.wallTime == null) {
                logger.error("unexpected null value in codeExecutorResponse.wallTime! {0}", codeExecutorResponse)
                throw Exception("unexpected null value in codeExecutorResponse.wallTime")
            }

            val response = ParseResponse(timeTaken = codeExecutorResponse.wallTime)

            stdout.forEachIndexed { index, s ->
                if (s.startsWith("<DESCRIBE::>")) {
                    val testName = s.substring(
                            startIndex = 1 + s.indexOfFirst { it.equals('>') },
                            endIndex = s.indexOfFirst { it.equals('(') }
                    )
                    val nextLine = stdout[index + 1]
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
    }
}


