package xyz.rynav.openveinsapi.exceptions.DTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class ValidationErrorResponse extends ErrorResponse {
    private Map<String, String> errors;
    public ValidationErrorResponse(Boolean success, String message, Map<String, String> errors, String timestamp) {
        super(success, message, timestamp);
        this.errors = errors;
    }
}
