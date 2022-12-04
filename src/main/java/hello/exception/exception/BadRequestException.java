package hello.exception.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 스프링 부트가 기본으로 제공하는 ExceptionResolver 는 다음과 같다.
 * HandlerExceptionResolverComposite 에 다음 순서로 등록
 *
 * 1. ExceptionHandlerExceptionResolver
 * 2. ResponseStatusExceptionResolver
 * 3. DefaultHandlerExceptionResolver 우선 순위가 가장 낮다.
 *
 * 순위가 높은 순서대로 처리하고 처리가 안되면 다음 순서 리졸버 실행하는
 *
 * ExceptionHandlerExceptionResolver
 * @ExceptionHandler 을 처리한다. API 예외 처리는 대부분 이 기능으로 해결한다.
 *
 * ResponseStatusExceptionResolver
 * HTTP 상태 코드를 지정해준다.
 * 예) @ResponseStatus(value = HttpStatus.NOT_FOUND)
 *
 * DefaultHandlerExceptionResolver
 * 스프링 내부 기본 예외를 처리한다.
 *
 * ResponseStatusExceptionResolver
 * ResponseStatusExceptionResolver 는 예외에 따라서 HTTP 상태 코드를 지정해주는 역할을 한다.
 *
 * 다음 두 가지 경우를 처리한다.
 * @ResponseStatus 가 달려있는 예외, 이 경우 개발자가 만든 예외에만 붙일 수 있다,
 * 기존 예외 클래스 수정 못하니까  ResponseStatusException 예외
 *
 * 이전에 ExceptionResolver는 메서드로 다 구현을 해야했는데 어노테이션 하나로 설정
 * BadRequestException이 터지면 ExceptionResolver가 확인, ResponseStatusExceptionResolver에 걸려
 * 에외에서 @ResponseStatus가 있는지 찾아 처리를 함, ResponseStatusExceptionResolver에 이전헤 해본 senderror있음
 * 여기서 설정한 HTTP 상태 코드 가져가 senderror() 에 세팅, 예외 핸들러도 공통으로 해주는
 * 예외에 따라 HTTP 상태 코드 지정
 *
 * ResponseStatusExceptionResolver 코드를 확인해보면
 * 결국 response.sendError(statusCode,resolvedReason) 를 호출하는 것을 확인할 수 있다.
 * sendError(400) 를 호출했기 때문에 WAS에서 다시 오류 페이지( /error )를 내부 요청
 *
 * 코드와 메시지 설정, 메시지까지 출력하려면 설정파일에서 message always해줘야함
 * reason 을 MessageSource 에서 찾는 기능도 제공한다. reason = "error.bad", 에러메시지 코드화 messages.properties파일에 있음
 *
 * @ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "잘못된 요청 오류")
 * */
@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "error.bad")
public class BadRequestException extends RuntimeException {
}
