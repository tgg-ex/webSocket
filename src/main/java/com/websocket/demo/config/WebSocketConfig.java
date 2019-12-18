package com.websocket.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @author zyz
 * <p>
 * 初始化websocket
 */
@Configuration
public class WebSocketConfig {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        System.out.println(">>>>>>>>>>>>>>>>>启用 WebSocket");
        return new ServerEndpointExporter();
    }
}
