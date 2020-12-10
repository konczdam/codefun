package hu.konczdam.codefun.repository

import hu.konczdam.codefun.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository: JpaRepository<User, Long?> {

    fun findByEmail(email: String): User?

    /**
     * Returns users which are not friends, and doesn't have a friend request
     * associated with the caller
     */
    @Query("""
        SELECT u FROM User u 
        WHERE (:name IS NULL OR u.username LIKE %:name%)
        AND u.id <> :callerId
        AND :callerId not in (select ff.id from u.friends ff)
        AND :callerId not in (select ff.requester.id from u.incomingFriendRequests ff)
        AND :callerId not in (select ff.receiver.id from u.outgoingFriendRequests ff)
    """)
    fun findNonFriendsUsers(
            @Param("name") name: String?,
            @Param("callerId") id: Long,
            page: Pageable
    ): Page<User>


    @Query("""
        SELECT u FROM User u 
        WHERE (:name IS NULL OR u.username LIKE %:name%)
        AND u.id <> :callerId
        AND :callerId not in (select ff.id from u.friends ff)
        AND :callerId in (select ff.requester.id from u.incomingFriendRequests ff)
        AND :callerId not in (select ff.receiver.id from u.outgoingFriendRequests ff)
    """)
    fun findUsersFriendRequestSentTo(
            @Param("name") name: String?,
            @Param("callerId") id: Long,
            page: Pageable
    ): Page<User>


    @Query("""
        SELECT u FROM User u 
        WHERE (:name IS NULL OR u.username LIKE %:name%)
        AND u.id <> :callerId
        AND :callerId not in (select ff.id from u.friends ff)
        AND :callerId not in (select ff.requester.id from u.incomingFriendRequests ff)
        AND :callerId in (select ff.receiver.id from u.outgoingFriendRequests ff)
    """)
    fun findUsersThatSentFriendRequests(
            @Param("name") name: String?,
            @Param("callerId") id: Long,
            page: Pageable
    ): Page<User>

    @Query("""
        SELECT u FROM User u 
        WHERE (:name IS NULL OR u.username LIKE %:name%)
        AND u.id <> :callerId
        AND :callerId in (select ff.id from u.friends ff)
        AND :callerId not in (select ff.requester.id from u.incomingFriendRequests ff)
        AND :callerId not in (select ff.receiver.id from u.outgoingFriendRequests ff)
    """)
    fun findFriends(
            @Param("name") name: String?,
            @Param("callerId") id: Long,
            page: Pageable
    ): Page<User>

    override fun findById(id: Long): Optional<User>
}