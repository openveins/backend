package xyz.rynav.openveinsapi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import xyz.rynav.openveinsapi.DTOs.ApiResponse;
import xyz.rynav.openveinsapi.DTOs.Locations.LocationCreate;
import xyz.rynav.openveinsapi.models.Location;
import xyz.rynav.openveinsapi.repositories.LocationRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    public ResponseEntity<ApiResponse<?>> getLocations(){
        Location[] locations = locationRepository.findAll().toArray(new Location[0]);
        return ResponseEntity.ok(ApiResponse.ok("success", locations));
    }

    public ResponseEntity<ApiResponse<?>> createLocation(LocationCreate location) {
        Location location1 = locationRepository.findByName(location.getName()).orElse(null);

        if(location1 != null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("A location with given name already exists"));

        Location newLocation = Location.builder()
                .name(location.getName())
                .description(location.getDescription())
                .build();

        locationRepository.save(newLocation);

        return ResponseEntity.ok(ApiResponse.ok("Successfully created location!", newLocation));
    }

    public ResponseEntity<ApiResponse<?>> deleteLocation(String locationId) {
        Optional<Location> location = locationRepository.findById(locationId);

        if(location.isPresent()) {
            locationRepository.deleteById(locationId);
            return ResponseEntity.ok(ApiResponse.ok("Successfully deleted location!", null));
        }

        return ResponseEntity.ok(ApiResponse.fail("An error occurred."));
    }

    public ResponseEntity<ApiResponse<Location>> getLocation(String UUID){
        Optional<Location> location = locationRepository.findById(UUID);
        if(location.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("No location found"));
        }

        return ResponseEntity.ok(ApiResponse.ok("Successfully getting location!", location.get()));
    }

    public ResponseEntity<ApiResponse<Location>> updateLocation(LocationCreate location, String locationId) {
        Optional<Location> location1 =  locationRepository.findById(locationId);
        if(location1.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("No location found"));
        }

        location1.get().setName(location.getName());
        location1.get().setDescription(location.getDescription());
        try {
            locationRepository.save(location1.get());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("An error occurred"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Successfully updated location!", location1.get()));

    }
}
