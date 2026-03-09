package xyz.rynav.openveinsapi.DTOs.Profile.TOTP;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TOTPVerifyRequest {
    private String code;
}
