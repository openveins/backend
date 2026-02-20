package xyz.rynav.openveinsapi.exceptions.DTO;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class ValidationErrorResponse extends ErrorResponse {
    private Map<String, String> errors;
    public ValidationErrorResponse(int status, String message, Map<String, String> errors, LocalDateTime timestamp) {
        super(status, message, timestamp);
        this.errors = errors;
    }
}
