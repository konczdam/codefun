package hu.konczdam.codefun.dataacces

import hu.konczdam.codefun.model.Language

data class UserDto(
        val username: String,
        val email: String,
        var gamesWon: Int = 0,
        var gamesPlayed: Int = 0,
        val preferredLanguages: MutableSet<Language>,
        val id: Long
)