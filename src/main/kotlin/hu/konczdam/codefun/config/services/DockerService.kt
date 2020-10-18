package hu.konczdam.codefun.config.services

import org.springframework.stereotype.Service
import java.io.*
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Service
class DockerService {

    val images: MutableList<String> = mutableListOf()

    @PostConstruct
    private fun init() {
        for (i in 0..1) {
            val builder = ProcessBuilder()
//
//            builder.command("cmd.exe", "/c", "docker run -d -it codewars/java-runner")
//            val process = builder.start()
//            val streamGobbler = StreamGobbler(process.inputStream) {
//                println(it)
//            }
//            Executors.newSingleThreadExecutor().submit(streamGobbler)

//            val response = exec(cmd = "docker run -d -it codewars/java-runner", captureOutput = true)
//            println(response)
        }
    }

    fun exec(cmd: String, stdIn: String = "", captureOutput:Boolean = false, workingDir: File = File(".")): String? {
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
}