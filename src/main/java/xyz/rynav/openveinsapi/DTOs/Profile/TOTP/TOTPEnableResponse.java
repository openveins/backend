package xyz.rynav.openveinsapi.DTOs.Profile.TOTP;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TOTPEnableResponse {
    private String status;
    private boolean success;
    private String message;
    private Map<String, String> data;
}
