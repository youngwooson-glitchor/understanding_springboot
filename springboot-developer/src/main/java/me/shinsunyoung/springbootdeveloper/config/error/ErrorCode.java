package me.shinsunyoung.springbootdeveloper.config.error;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public enum ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "E1", " Invalid Input Value"), METHOD_NOT_ALLOWED(
            HttpStatus.METHOD_NOT_ALLOWED, "E2", " Method Not Allowed"), INTERNAL_SERVER_ERROR(
                    HttpStatus.INTERNAL_SERVER_ERROR, "E3", "Server Error"), NOT_FOUND(
                            HttpStatus.NOT_FOUND, "E4", " Not Found"), ARTICLE_NOT_FOUND(
                                    HttpStatus.NOT_FOUND, "A1", " Article Not Found");

    private final String message;
    private final String code;
    private final HttpStatus status;

    ErrorCode(final HttpStatus status, final String code, final String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

}
