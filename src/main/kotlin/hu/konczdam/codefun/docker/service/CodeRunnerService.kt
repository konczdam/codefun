package hu.konczdam.codefun.docker.service

import hu.konczdam.codefun.docker.ParseResponse

interface CodeRunnerService {

    fun executeCode(challengeId: Long, code: String, testIds: List<Long>): ParseResponse
}