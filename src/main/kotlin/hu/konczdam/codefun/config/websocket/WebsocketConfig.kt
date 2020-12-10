package hu.konczdam.codefun.config.websocket

import hu.konczdam.codefun.config.jwt.JwtUtils
import hu.konczdam.codefun.services.UserDetailsServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
class WebsocketConfig : AbstractSecurityWebSocketMessageBrokerConfigurer() {

    @Autowired
    private lateinit var userDetailsService: UserDetailsServiceImpl

    @Autowired
    private lateinit var jwtUtils: JwtUtils

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

    override fun sameOriginDisabled() = true


    override fun configureInbound(messages: MessageSecurityMetadataSourceRegistry) {
        super.configureInbound(messages)
        messages
                .simpSubscribeDestMatchers("/app/**").hasRole("USER")
                .simpMessageDestMatchers("/topic/**").hasRole("USER")
    }


    override fun customizeClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(MyChannelInterceptor())
    }

    inner class MyChannelInterceptor: ChannelInterceptor {
        override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
            val accessor: StompHeaderAccessor =
                    MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)!!
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                val jwtToken = accessor.getNativeHeader("Authorization")?.get(0)!!
                val email = jwtUtils.getEmailFromJwtToken(jwtToken)
                val userDetails = userDetailsService.loadUserByUsername(email)
                val authentication = UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.authorities
                )
                accessor.setUser(authentication)
            }
            return message
        }
    }

}

