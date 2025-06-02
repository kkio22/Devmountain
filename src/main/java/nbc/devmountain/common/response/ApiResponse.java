package nbc.devmountain.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private final boolean success;
    private final String message;
    private final int status;
    private final T data;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.of(true, "성공", 200, data);
    }

    public static ApiResponse<Void> fail(String message, int status) {
        return ApiResponse.of(false, message, status, null);
    }

    public static <T> ApiResponse<T> of(boolean success, String message, int status, T data) {
        return new ApiResponse<>(success, message, status, data);
    }
}