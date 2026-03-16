package xyz.rynav.openveinsapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.rynav.openveinsapi.models.UserSettings.UserSettings;

import java.util.Optional;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, String> {
    Optional<UserSettings> findByUserId(String userId);
}
