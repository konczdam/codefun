package hu.konczdam.codefun.model

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
data class FriendRequest(

        @ManyToOne
        val requester: User,

        @ManyToOne
        val receiver: User
) {

    @Id
    @GeneratedValue
    var id: Long? = null

    override fun toString(): String {
        return """
            requesterId: ${requester.id},
            receiverId: ${requester.id},
            id: $id
        """.trimIndent()
    }
}