package botondepanico.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarAlerta(String destinatario, String nombreCiudadano,
                          String celular, String tipoEmergencia,
                          String latitud, String longitud) {

    String googleMapsLink = "https://www.google.com/maps?q=" + latitud + "," + longitud;

    SimpleMailMessage mensaje = new SimpleMailMessage();
    mensaje.setTo(destinatario);
    mensaje.setSubject("🆘 ALERTA DE EMERGENCIA - " + tipoEmergencia);
    mensaje.setText(
        "ALERTA DE EMERGENCIA\n\n" +
        "Tipo de emergencia: " + tipoEmergencia + "\n" +
        "Ciudadano: " + nombreCiudadano + "\n" +
        "Celular: " + celular + "\n" +
        "Ubicación GPS: " + googleMapsLink + "\n\n" +
        "Por favor responda de inmediato."
    );
    mailSender.send(mensaje);
}
}