package xyz.rynav.openveinsapi.DTOs.Configs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicAuthConfig {
    private boolean turnstileEnabled;
    private String turnstileSiteKey;
    private boolean signupEnabled;
    private String message;
}
