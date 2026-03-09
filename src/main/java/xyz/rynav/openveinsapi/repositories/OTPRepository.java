package xyz.rynav.openveinsapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.rynav.openveinsapi.models.OTPConfig;

import java.util.Optional;

public interface OTPRepository extends JpaRepository<OTPConfig, String> {
    Optional<OTPConfig> findByUserId(String userId);

    void deleteByUserId(String userId);
}
