package hu.konczdam.codefun.repository

import hu.konczdam.codefun.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserRepository: JpaRepository<User, Long?> {

    fun findByEmail(email: String): User?

    @Query("""
        SELECT u.friends FROM User u JOIN u.friends WHERE u.id = :id
    """)
    fun findFriendsById(@Param("id") id: Long, page: Pageable): Page<User>


    @Query("""
        SELECT u FROM User u 
        WHERE (:name IS NULL OR u.username LIKE %:name%)
        AND u.id <> :callerId
        AND :callerId not in (select ff.id from u.friends ff)
        AND :callerId not in (select ff.requester.id from u.incomingFriendRequests ff)
        AND :callerId not in (select ff.receiver.id from u.outgoingFriendRequests ff)
    """)
    fun findFiltereAndSortedUsers(
            @Param("name") name: String?,
            @Param("callerId") id: Long,
            page: Pageable
    ): Page<User>
}