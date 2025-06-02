package nbc.devmountain.common.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
    private final String error;
    private final int status;
    private final String code;
    private final String message;
}