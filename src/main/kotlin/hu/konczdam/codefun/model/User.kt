package hu.konczdam.codefun.model

import com.fasterxml.jackson.annotation.JsonIgnore
import hu.konczdam.codefun.converter.UserConverter
import hu.konczdam.codefun.dataacces.UserDto
import org.mapstruct.factory.Mappers
import javax.persistence.*

@Entity
@Table(name = "user",
        uniqueConstraints = arrayOf(
                UniqueConstraint(columnNames = arrayOf("email"))
        ))
data class User(
        val username: String,

        val email: String,

        val password: String,

        var gamesWon: Int = 0,

        var gamesPlayed: Int = 0,

        @ElementCollection(fetch = FetchType.EAGER)
        val preferredLanguages: MutableSet<Language>,

        @JsonIgnore
        @ManyToMany
        @JoinTable(name = "tbl_friends",
                joinColumns = arrayOf(JoinColumn(name = "personId")),
                inverseJoinColumns = arrayOf(JoinColumn(name = "friendId")))
        val friends: MutableSet<User> = mutableSetOf(),

        @JsonIgnore
        @ManyToMany
        @JoinTable(name = "tbl_friends",
                joinColumns = arrayOf(JoinColumn(name = "friendId")),
                inverseJoinColumns = arrayOf(JoinColumn(name = "personId")))
        val friendOf: MutableSet<User> = mutableSetOf(),

        @ManyToMany(fetch = FetchType.EAGER)
        val roles: Set<Role> = mutableSetOf()
) {

    @Id
    @GeneratedValue
    var id: Long? = null

}