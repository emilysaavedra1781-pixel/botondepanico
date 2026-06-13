package botondepanico.service;

import botondepanico.model.Emergencia;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OperadorAlertaWebSocketService extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    public void broadcastNuevaEmergencia(Emergencia emergencia) {
        String payload = String.format(
            "{\"tipo\":\"NUEVA_EMERGENCIA\",\"id\":%d,\"emergencia\":\"%s\",\"distrito\":\"%s\"}",
            emergencia.getId(),
            limpiar(emergencia.getTipoEmergencia()),
            limpiar(emergencia.getDistrito())
        );
        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) continue;
            try {
                session.sendMessage(new TextMessage(payload));
            } catch (IOException ignored) {
            }
        }
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.replace("\"", "'");
    }
}
