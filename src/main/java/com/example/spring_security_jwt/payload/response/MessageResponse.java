package com.example.spring_security_jwt.payload.response;

public class MessageResponse {

    private String message;

    public MessageResponse() {
    }

    public MessageResponse(String message) {
        this.message = message;
    }

    // ESTE MÉTODO (Getter y Setter) ES IMPRESCINDIBLE PARA QUE SPRING / JACKSON GENEREN EL JSON
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
