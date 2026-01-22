package haennihaesseo.sandoll.global.base;

import org.springframework.http.HttpStatus;

public interface BaseSuccessStatus {
    HttpStatus getHttpStatus();
    String getCode();
    String getMessage();
}