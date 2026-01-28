package haennihaesseo.sandoll.global.response;

import haennihaesseo.sandoll.global.base.BaseErrorStatus;
import haennihaesseo.sandoll.global.base.BaseSuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private final boolean success;
    private int status;
    private String message;
    private String code;
    private T data;
    private final OffsetDateTime timestamp;   // 응답 시각

    private static <T> ResponseEntity<ApiResponse<T>> wrap(BaseSuccessStatus successStatus) {
        ApiResponse<T> body = ApiResponse.<T>builder()
                .success(successStatus.getHttpStatus().is2xxSuccessful())
                .status(successStatus.getHttpStatus().value())
                .message(successStatus.getMessage())
                .code(successStatus.getCode())
                .data(null)
                .timestamp(OffsetDateTime.now())
                .build();
        return new ResponseEntity<>(body, successStatus.getHttpStatus());
    }


    private static <T> ResponseEntity<ApiResponse<T>> wrap(BaseSuccessStatus successStatus,
        T data) {
        ApiResponse<T> body = ApiResponse.<T>builder()
            .success(successStatus.getHttpStatus().is2xxSuccessful())
            .status(successStatus.getHttpStatus().value())
            .message(successStatus.getMessage())
            .code(successStatus.getCode())
            .data(data)
            .timestamp(OffsetDateTime.now())
            .build();
        return new ResponseEntity<>(body, successStatus.getHttpStatus());
    }
    // ===================== 성공 응답 ==========================

    // 데이터 X 성공 응답
    public static <T> ResponseEntity<ApiResponse<T>> success(BaseSuccessStatus successStatus) {
        return wrap(successStatus);
    }

    // 데이터 O 성공 응답
    public static <T> ResponseEntity<ApiResponse<T>> success(BaseSuccessStatus successStatus, T data) {
        return wrap(successStatus, data);
    }

    // ====================== 실패 응답 =========================

    public static ResponseEntity<ApiResponse<Void>> fail(BaseErrorStatus baseErrorStatus) {
        return error(baseErrorStatus);
    }

    public static ResponseEntity<ApiResponse<Void>> fail(BaseErrorStatus baseErrorStatus, String message) {
        return error(baseErrorStatus, message);
    }

    private static <T> ResponseEntity<ApiResponse<T>> error(BaseErrorStatus errorStatus, String message) {
        ApiResponse<T> body = ApiResponse.<T>builder()
            .success(false)
            .status(errorStatus.getHttpStatus().value())
            .message(message)
            .code(errorStatus.getCode())
            .data(null)
//            .timestamp(OffsetDateTime.now())
            .build();
        return new ResponseEntity<>(body, errorStatus.getHttpStatus());
    }

    private static <T> ResponseEntity<ApiResponse<T>> error(BaseErrorStatus errorStatus) {
        ApiResponse<T> body = ApiResponse.<T>builder()
            .success(false)
            .status(errorStatus.getHttpStatus().value())
            .message(errorStatus.getMessage())
            .code(errorStatus.getCode())
            .data(null)
//            .timestamp(OffsetDateTime.now())
            .build();
        return new ResponseEntity<>(body, errorStatus.getHttpStatus());
    }
}
