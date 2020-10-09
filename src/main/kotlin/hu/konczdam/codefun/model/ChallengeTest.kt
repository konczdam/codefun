package hu.konczdam.codefun.model

import javax.persistence.*

@Entity
data class ChallengeTest(
        val input: String,

        val expectedOutput: String,

        val displayName: String
) {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(referencedColumnName = "id")
    lateinit var challenge: Challenge

    @Id
    @GeneratedValue
    var id: Long? = null
}