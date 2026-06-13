package botondepanico.model;

public enum TipoEmergencia {
    ROBO_ASALTO("Robo / Asalto"),
    VIOLENCIA_FAMILIAR("Violencia familiar"),
    EMERGENCIA_MEDICA("Emergencia medica"),
    INCENDIO("Incendio"),
    ACCIDENTE_TRANSITO("Accidente de transito"),
    OTRO("Otra situacion de riesgo");

    private final String etiqueta;

    TipoEmergencia(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
