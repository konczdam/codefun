package hu.konczdam.codefun.model

import javax.persistence.*

@Entity
data class Challenge(

        val title: String,

        val description: String,

        @ElementCollection(fetch = FetchType.EAGER)
        val availableLanguages: MutableSet<Language>,
        
        @OneToMany(cascade = arrayOf(CascadeType.ALL), fetch = FetchType.EAGER)
        val challengeTests: MutableList<ChallengeTest> = mutableListOf()
) {

    @Id
    @GeneratedValue
    var id: Long? = null
}