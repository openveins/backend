package xyz.rynav.openveinsapi.DTOs.Profile.TOTP;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class TOTPResponse {
    private String status;
    private String message;
    private String qr;

    public TOTPResponse(String status, String message, String qr) {
        this.status = status;
        this.message = message;
        this.qr = qr;
    }

    public TOTPResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
}
