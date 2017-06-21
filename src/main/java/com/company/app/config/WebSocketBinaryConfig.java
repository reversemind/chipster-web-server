package com.company.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;

/**
 *
 */
@Configuration
@EnableWebSocket
public class WebSocketBinaryConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new BinaryHandler(), "/binary").withSockJS();
    }

    @Component
    public static class BinaryHandler extends BinaryWebSocketHandler {
        public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
            try {
                session.sendMessage(new BinaryMessage("123".getBytes()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}


