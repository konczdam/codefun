package hu.konczdam.codefun.config.websocket

import hu.konczdam.codefun.service.RoomService
import hu.konczdam.codefun.services.UserDetailsImpl
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectedEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent


@Component
class WebsocketEventListener {

    companion object {
        private val logger = LoggerFactory.getLogger(WebsocketEventListener::class.java);
    }

    @Autowired
    private lateinit var roomService: RoomService

    @EventListener
    fun handleWebSocketConnectListener(event: SessionConnectedEvent?) {
        logger.info("Received a new web socket connection.")
    }

    @EventListener
    fun handleWebSocketConnectionLostListener(event: SessionDisconnectEvent) {
        val userId = ((event.user as UsernamePasswordAuthenticationToken).principal as UserDetailsImpl).id
        logger.info("User disconnected with id: $userId")
        roomService.removeUserFromGame(userId)
    }
}