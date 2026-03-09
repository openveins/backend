package xyz.rynav.openveinsapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import xyz.rynav.openveinsapi.models.Config;

import java.util.List;
import java.util.Optional;

public interface ConfigRepository extends JpaRepository<Config, String> {
    @Query("select c.config_name, c.config_value from Config c where c.config_name = ?1")
    Optional<Config> findByConfigName(String configName);

    @Query("select c.config_name, c.config_value from Config c")
    List<Config> findAllConfig();

}