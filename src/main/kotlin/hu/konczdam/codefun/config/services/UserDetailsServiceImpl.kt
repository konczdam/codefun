package hu.konczdam.codefun.services

import hu.konczdam.codefun.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl: UserDetailsService {

    @Autowired
    private lateinit var userRepo: UserRepository

    override fun loadUserByUsername(email: String?): UserDetails {
        val user = userRepo.findByEmail(email!!)

        if (user == null) {
            throw Exception("User not found with email: " + email)
        }

        return UserDetailsImpl.build(user)
    }

}