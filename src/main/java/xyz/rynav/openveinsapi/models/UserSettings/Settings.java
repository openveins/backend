package xyz.rynav.openveinsapi.models.UserSettings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Settings implements Serializable {
    private boolean otpEnabled = false;
    private String timezone = "Europe/London";
}
