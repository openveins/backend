package xyz.rynav.openveinsapi.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTPConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", unique = true, nullable = false)
    private String userId;

    @Column(name = "otp_enabled")
    private boolean otpEnabled;

    @Column(name = "otp_secret")
    private String otpSecret;

    @Column(name = "otp_verified")
    private boolean otpVerified;

    @Column(name = "otp_verification_pending")
    private boolean otpVerificationPending;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate(){
        updatedAt = LocalDateTime.now();
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate(){
        updatedAt = LocalDateTime.now();
    }
}
