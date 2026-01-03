package xyz.rynav.openveinsapi.repositories;

import org.springframework.stereotype.Repository;
import xyz.rynav.openveinsapi.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
