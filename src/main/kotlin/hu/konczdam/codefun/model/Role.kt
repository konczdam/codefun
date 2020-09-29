package hu.konczdam.codefun.model

import javax.persistence.*

@Entity
data class Role(

        @Enumerated(EnumType.STRING)
        val name: ERole
) {
    @Id
    @GeneratedValue
    var id: Long? = null
}