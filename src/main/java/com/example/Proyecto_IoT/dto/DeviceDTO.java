package com.example.Proyecto_IoT.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;

//las etiquetas de lombok no pescaron
@JsonIgnoreProperties(ignoreUnknown = true)// Ignora propiedades desconocidas en la respuesta JSON
public class DeviceDTO implements Serializable {
    private IdWrapper id;
    private String name;
    private String type;
    private Object additionalInfo; // Permite enviar info adicional como un ObjectNode

    public DeviceDTO() {}

    public DeviceDTO(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public IdWrapper getId() {
        return id;
    }

    public void setId(IdWrapper id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Object additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    // Clase interna para mapear el campo id de ThingsBoard
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IdWrapper implements Serializable {
        private String id;

        public IdWrapper() {}

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
