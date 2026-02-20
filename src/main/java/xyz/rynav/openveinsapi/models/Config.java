package xyz.rynav.openveinsapi.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "panelsettings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Config {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String Id;

    @Column(name = "config_name", unique = true, nullable = false)
    private String configName;

    @Column(name = "config_value")
    private String configValue;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate(){
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate(){
        updatedAt = LocalDateTime.now();
    }



}
