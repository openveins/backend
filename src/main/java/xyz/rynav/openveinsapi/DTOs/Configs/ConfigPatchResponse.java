package xyz.rynav.openveinsapi.DTOs.Configs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.rynav.openveinsapi.models.Config;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfigPatchResponse {
    private List<Config> updated;
    private List<String> warnings;
    private String message;
}
