package hu.konczdam.codefun.services

import com.fasterxml.jackson.annotation.JsonIgnore
import hu.konczdam.codefun.model.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserDetailsImpl constructor(
     val id: Long,

     private val username: String,

     val email: String,

     @JsonIgnore
     private val password: String,

    private val authorities: Collection<GrantedAuthority>

): UserDetails {

    companion object {
        fun build(user: User): UserDetailsImpl {
            val authorities = user.roles.map {
                SimpleGrantedAuthority(it.name.name)
            }

            return UserDetailsImpl(
                    id = user.id!!,
                    username = user.username,
                    email = user.email,
                    password = user.password,
                    authorities = authorities
            )
        }
    }

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getPassword() = password

    override fun getUsername() = username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}