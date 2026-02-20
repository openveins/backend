package xyz.rynav.openveinsapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import xyz.rynav.openveinsapi.models.Config;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ConfigRepository extends JpaRepository<Config, String> {
    Optional<Config> findByConfigName(String configName);

}