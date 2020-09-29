package hu.konczdam.codefun.repository

import hu.konczdam.codefun.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

@Repository
@Transactional
interface UserRepository: JpaRepository<User, Long?> {

    fun findByEmail(email: String): User?
}