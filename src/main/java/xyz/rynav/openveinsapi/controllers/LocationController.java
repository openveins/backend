package xyz.rynav.openveinsapi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.rynav.openveinsapi.DTOs.Locations.LocationCreate;
import xyz.rynav.openveinsapi.interceptors.auth.AuthRequired;
import xyz.rynav.openveinsapi.services.LocationService;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping()
    @AuthRequired
    public ResponseEntity<?> getLocations() {
        return locationService.getLocations();
    }

    @PostMapping()
    @AuthRequired
    public ResponseEntity<?> createLocation(@RequestBody LocationCreate location) {
        return locationService.createLocation(location);
    }

    @DeleteMapping("{locationId}")
    @AuthRequired
    public ResponseEntity<?> deleteLocation(@PathVariable("locationId") String locationId) {
        return locationService.deleteLocation(locationId);
    }

    @GetMapping("{locationId}")
    @AuthRequired
    public ResponseEntity<?> getLocation(@PathVariable("locationId") String locationId) {
        return locationService.getLocation(locationId);
    }

    @PatchMapping("{locationId}")
    @AuthRequired
    public ResponseEntity<?> updateLocation(@RequestBody LocationCreate location, @PathVariable("locationId") String locationId) {
        return locationService.updateLocation(location, locationId);
    }
}
