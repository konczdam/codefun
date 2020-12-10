package hu.konczdam.codefun.model

import javax.persistence.*

@Entity
data class Challenge(

        val title: String,

        val description: String,

        @OneToMany(cascade = arrayOf(CascadeType.ALL), fetch = FetchType.EAGER, mappedBy = "challenge")
        val challengeTests: MutableList<ChallengeTest> = mutableListOf()
) {

    @Id
    @GeneratedValue
    var id: Long? = null
}