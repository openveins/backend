package xyz.rynav.openveinsapi.DTOs.Configs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigPatchRequest {
    private Map<String, String> updates;

}
