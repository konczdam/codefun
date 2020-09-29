package hu.konczdam.codefun.config

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestFilter: Filter {
    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
        val request = req as HttpServletRequest
        val response = res as HttpServletResponse

        response.setHeader("Access-control-Allow-Origin", "*")
        response.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS, DELETE")
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with, x-auth-token")
        response.setHeader("Access-Control-Max-Age", "3600")
        response.setHeader("Access-Control-Allow-Credentials", "true")

        if (!request.method.equals("OPTIONS", ignoreCase = true)) {
            try {
                chain.doFilter(req, res)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        } else {
            response.setHeader("Access-Control-Allowed-Methods", "POST, GET, DELETE")
            response.setHeader("Access-Control-Max-Age", "3600")
            response.setHeader("Access-Control-Allow-Headers", "authorization, content-type,x-auth-token, " +
                    "access-control-request-headers, access-control-request-method, accept, origin, authorization, x-requested-with")
            response.status = HttpServletResponse.SC_OK
        }
    }
}