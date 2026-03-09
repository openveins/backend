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
public class TOTPVerifyResponse {
    private boolean success;
    private String message;
    private Map<String, String> data;
}
