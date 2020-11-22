package hu.konczdam.codefun.repository

import hu.konczdam.codefun.model.FriendRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FriendRequestRepository: JpaRepository<FriendRequest, Long?> {
}