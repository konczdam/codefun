package hu.konczdam.codefun.dataacces

data class TestCaseExecuteDTO (
        val challengeId: String,
        val code: String,
        val testIds: List<String>,
        val submitted: Boolean
)