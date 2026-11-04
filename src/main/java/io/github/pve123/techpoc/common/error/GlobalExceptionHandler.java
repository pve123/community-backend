package io.github.pve123.techpoc.common.error;

import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 모든 오류를 RFC 9457 ProblemDetail 형식으로 응답합니다.
 * 입력값 검증 실패 같은 스프링 기본 오류는 부모 클래스가 같은 형식으로 처리합니다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.errorCode();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(errorCode.status(), errorCode.message());
        problem.setProperty("code", errorCode.name());
        return ResponseEntity.status(errorCode.status()).body(problem);
    }
}
