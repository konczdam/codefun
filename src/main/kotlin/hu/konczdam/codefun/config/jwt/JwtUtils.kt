package hu.konczdam.codefun.config.jwt

import hu.konczdam.codefun.services.UserDetailsImpl
import io.jsonwebtoken.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import java.util.*


@Component
class JwtUtils(
        @Value("\${konczdam.app.jwtSecret}")
        private var jwtSecret: String,

        @Value("\${konczdam.app.jwtExpirationMs}")
        private var jwtExpirationMs: Int
) {

    companion object {
        private val logger = LoggerFactory.getLogger(JwtUtils::class.java)
    }


    fun generateJwtToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal as UserDetailsImpl

        val now = Date()
        return Jwts.builder()
                .setSubject(userPrincipal.email)
                .setId(userPrincipal.id.toString())
                .setIssuedAt(now)
                .setExpiration(Date(now.time + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact()
    }

    fun getEmailFromJwtToken(header: String): String {
        val token = if (StringUtils.hasText(header) && header.startsWith("Bearer ")) header.substring(7) else header
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).body.subject
    }

    fun getIdFromJwtToken(header: String): String {
        val token = if (StringUtils.hasText(header) && header.startsWith("Bearer ")) header.substring(7) else header
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).body.id
    }

    fun validateJwtToken(authToken: String): Boolean {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken)
            return true
        } catch (e: SignatureException) {
            logger.error("Invalid JWT signature: {}", e.message)
        } catch (e: MalformedJwtException) {
            logger.error("Invalid JWT token: {}", e.message)
        } catch (e: ExpiredJwtException) {
            logger.error("JWT token is expired: {}", e.message)
        } catch (e: UnsupportedJwtException) {
            logger.error("JWT token is unsupported: {}", e.message)
        } catch (e: IllegalArgumentException) {
            logger.error("JWT claims string is empty: {}", e.message)
        }
        return false
    }
}