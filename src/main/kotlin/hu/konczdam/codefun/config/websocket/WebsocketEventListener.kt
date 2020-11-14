package hu.konczdam.codefun.config.websocket

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectedEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent


@Component
class WebsocketEventListener {

    companion object {
        val logger = LoggerFactory.getLogger(WebsocketEventListener::class.java);
    }

    @EventListener
    fun handleWebSocketConnectListener(event: SessionConnectedEvent?) {
        logger.info("Received a new web socket connection.")
    }

    @EventListener
    fun handleWebSocketConnectionLostListener(event: SessionDisconnectEvent) {
        logger.info("User disconnected")
        logger.info(event.user.toString())
    }
}