package xyz.rynav.openveinsapi.DTOs.Locations;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationCreate {
    private String name;
    private String description = "";
}
