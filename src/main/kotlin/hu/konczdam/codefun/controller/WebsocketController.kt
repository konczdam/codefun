package hu.konczdam.codefun.controller

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

@Controller
class WebsocketController {


    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    fun greeting(helloMessage: HelloMessage): HelloMessage  {
        return HelloMessage(message = "Hello, ${helloMessage.message}")
    }

    class HelloMessage constructor(
            val message: String
    )
}