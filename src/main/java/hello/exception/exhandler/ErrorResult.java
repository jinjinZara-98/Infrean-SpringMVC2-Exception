package hello.exception.exhandler;

import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * 오류가 발생하는 코드와 메시지를 json 으로 응답하기 위한
 */
@Data
@AllArgsConstructor
public class ErrorResult {
    private String code;
    private String message;
}
