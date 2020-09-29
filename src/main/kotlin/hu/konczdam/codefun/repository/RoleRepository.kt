package hu.konczdam.codefun.repository

import hu.konczdam.codefun.model.ERole
import hu.konczdam.codefun.model.Role
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository: JpaRepository<Role, Long> {

    fun findByName(name: ERole): Role?
}