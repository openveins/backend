package xyz.rynav.openveinsapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.rynav.openveinsapi.models.OTPConfig;

import java.util.Optional;

@Repository
public interface OTPRepository extends JpaRepository<OTPConfig, String> {
    Optional<OTPConfig> findByUserId(String userId);

    void deleteByUserId(String userId);
}
