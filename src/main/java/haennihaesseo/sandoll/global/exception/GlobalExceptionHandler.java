package haennihaesseo.sandoll.global.exception;

import haennihaesseo.sandoll.global.response.ApiResponse;
import haennihaesseo.sandoll.global.status.ErrorStatus;
import jakarta.persistence.EntityNotFoundException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * GlobalException 처리
     */
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(GlobalException e) {
        log.warn(">>>>>>>>GlobalException: {}", e.getErrorStatus().getMessage());
        return ApiResponse.fail(e.getErrorStatus());
    }

    /**
     * 엔티티 미존재(404) 처리
     * Optional 가져올때 orElseThrow(EntityNotFoundException::new) 이용
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFoundException(EntityNotFoundException ex) {
        return ApiResponse.fail(
                ErrorStatus.NOT_FOUND, ex.getMessage() != null ? ex.getMessage() : "리소스를 찾을 수 없습니다."
        );
    }

    /**
     * 데이터 무결성 위반(409) 처리
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ApiResponse.fail(
                ErrorStatus.CONFLICT
        );
    }

    /**
     * 비즈니스 로직 일반 실패(400) 처리
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        return ApiResponse.fail(
                ErrorStatus.BAD_REQUEST
        );
    }

    /**
     * MethodArgumentNotValidException 처리 (RequestBody로 들어온 필드들의 유효성 검증에 실패한 경우)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String combinedErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining(", "));
        log.error("Validation error: {}", combinedErrors);
        return ApiResponse.fail(ErrorStatus.BAD_REQUEST, combinedErrors);
    }

    /**
     * MaxUploadSizeExceededException 처리 (업로드 파일 크기 초과)
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.error("Max upload size exceeded: {}", ex.getMessage());
        log.error("Stack trace: ", ex);
        return ApiResponse.fail(ErrorStatus.PAYLOAD_TOO_LARGE, "파일 크기가 너무 큽니다.");
    }

    /**
     * 마지막 안정망(500) 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        ex.printStackTrace();

        return ApiResponse.fail(
                ErrorStatus.INTERNAL_SERVER_ERROR
        );
    }
}
