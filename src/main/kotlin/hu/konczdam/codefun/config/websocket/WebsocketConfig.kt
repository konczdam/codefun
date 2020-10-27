package hu.konczdam.codefun.config.websocket

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebsocketConfig : WebSocketMessageBrokerConfigurer, AbstractSecurityWebSocketMessageBrokerConfigurer() {

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000", "chrome-extension://ggnhohnkfcpcanfekomdkjffnfcjnjam")
                .withSockJS()
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.setApplicationDestinationPrefixes("/app")
        registry.enableSimpleBroker("/topic")
        registry.setUserDestinationPrefix("/user")
    }

    override fun sameOriginDisabled(): Boolean {
        return true
    }

    override fun configureInbound(messages: MessageSecurityMetadataSourceRegistry) {
//        super.configureInbound(messages)
        messages
                .simpSubscribeDestMatchers("/ws/**").hasRole("USER")
                .simpMessageDestMatchers("/ws/**").hasRole("USER")
//                .anyMessage().denyAll()
    }
}