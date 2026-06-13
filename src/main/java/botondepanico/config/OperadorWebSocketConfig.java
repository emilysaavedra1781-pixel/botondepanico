package botondepanico.config;

import botondepanico.service.OperadorAlertaWebSocketService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class OperadorWebSocketConfig implements WebSocketConfigurer {

    private final OperadorAlertaWebSocketService alertaService;

    public OperadorWebSocketConfig(OperadorAlertaWebSocketService alertaService) {
        this.alertaService = alertaService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(alertaService, "/ws/operador-alertas").setAllowedOrigins("*");
    }
}
