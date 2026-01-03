package xyz.rynav.openveinsapi.DTOs.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "An email address is required")
    @Email(message = "An email must be of a valid format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 10, max = 64, message = "A password must be between 10 and 64 characters")
    private String password;

    @NotBlank(message = "Captcha token is required")
    private String captchaToken;
}