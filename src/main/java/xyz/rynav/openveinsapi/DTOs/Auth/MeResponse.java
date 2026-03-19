package xyz.rynav.openveinsapi.DTOs.Auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.rynav.openveinsapi.models.UserSettings.Settings;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeResponse {

    private String id;
    private String username;
    private String email;
    private Settings settings;
}
