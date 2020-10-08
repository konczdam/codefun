package hu.konczdam.codefun.docker.service

interface CodeRunnerService {

    fun executeCode(challengeId: Long, code: String, testIds: List<Long>)
}