package hello.exception.exhandler.advice;

import hello.exception.exception.UserException;
import hello.exception.exhandler.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * @ExceptionHandler 를 사용해서 예외를 깔끔하게 처리할 수 있게 되었지만,
 * 정상 코드와 예외 처리코드가 하나의 컨트롤러에 섞여 있다. @ControllerAdvice 또는 @RestControllerAdvice 를 사용하면 분리가능
 * 컨트롤러에 있는것들을 여기서 모아서 처리, 마치 AOP를 컨트롤러에 적용하는거처럼
 *
 * 즉 컨트롤러에서 예외 발생하면 처리하는걸 한 곳에 모아둠
 * 그전에 @ExceptionHandler 쓰면 그 컨트롤러에서 예외 발생한거만 처리 할 수 있어
 * 매번 컨트롤러 클래스 만들 때마다 @ExceptionHandler 붙여 같은 코드 썻어야했음
 *
 * AOP 랑 비슷슷
 * */
@Slf4j
/**
 * @ControllerAdvice
 * @ControllerAdvice 는 대상으로 지정한 여러 컨트롤러에 @ExceptionHandler , @InitBinder 기능을부여해주는 역할을 한다.
 * @ControllerAdvice 에 대상을 지정하지 않으면 모든 컨트롤러에 적용된다. (글로벌 적용)
 *
 * @RestControllerAdvice 는 @ControllerAdvice 와 같고, @ResponseBody 가 추가되어 있다.
 * @Controller , @RestController 의 차이와 같다
 *
 * 대상 컨트롤러 지정 방법,
 *
 * 특정 어노테이션에 있는 컨트롤러
 * @ControllerAdvice(annotations = RestController.class)
 * public class ExampleAdvice1 {}
 *
 * 이 패키지와 하위 패키지 포함
 * @ControllerAdvice("org.example.controllers")
 * public class ExampleAdvice2 {}
 *
 * 직접 컨트롤러 지정
 * @ControllerAdvice(assignableTypes = {ControllerInterface.class,AbstractController.class})
 * public class ExampleAdvice3 {}
 *
 * @ExceptionHandler 와 @ControllerAdvice 를 조합하면 예외를 깔끔하게 해결
 */
//@RestControllerAdvice(basePackages = "hello.exception.api")
public class ExControllerAdvice {

    /**
     * 우선순위
     * 스프링의 우선순위는 항상 자세한 것이 우선권을 가진다. 예를 들어서 부모, 자식 클래스가 있고 다음과 같이 예외가 처리된다.
     * @ExceptionHandler(부모예외.class)
     * public String 부모예외처리()(부모예외 e) {}
     * @ExceptionHandler(자식예외.class)
     * public String 자식예외처리()(자식예외 e) {}
     * @ExceptionHandler 에 지정한 부모 클래스는 자식 클래스까지 처리할 수 있다.
     *  따라서 자식예외가 발생하면 부모예외처리() , 자식예외처리() 둘다 호출 대상이 된다.
     *  그런데 둘 중 더 자세한 것이 우선권을 가지므로 자식예외처리() 가 호출된다.
     *  물론 부모예외 가 호출되면 부모예외처리() 만 호출 대상이 되므로 부모예외처리() 가 호출된다.
     *
     * 다양한 예외
     * 다음과 같이 다양한 예외를 한번에 처리할 수 있다.
     * @ExceptionHandler({AException.class, BException.class})
     * public String ex(Exception e) {
     *  log.info("exception e", e);
     * }
     *
     * 실행 흐름
     * 컨트롤러를 호출한 결과 IllegalArgumentException 예외가 컨트롤러 밖으로 던져진다.
     * 예외가 발생했으로 ExceptionResolver 가 작동한다. 가장 우선순위가 높은
     * ExceptionHandlerExceptionResolver 가 실행된다.
     * ExceptionHandlerExceptionResolver 는 해당 컨트롤러에 IllegalArgumentException 을 처리할
     * 수 있는 @ExceptionHandler 가 있는지 확인한다.
     * illegalExHandle() 를 실행한다. @RestController 이므로 illegalExHandle() 에도
     * @ResponseBody 가 적용된다. 따라서 HTTP 컨버터가 사용되고, 응답이 다음과 같은 JSON으로 반환된다.
     * @ResponseStatus(HttpStatus.BAD_REQUEST) 를 지정했으므로 HTTP 상태 코드 400으로 응답한다.
     *
     * @ExceptionHandler는 이 컨트롤러에서만 적용되는
     *
     * 해당 컨트롤러에서 처리하고 싶은 예외를 지정해주면 된다.
     * 해당 컨트롤러에서 예외가 발생하면 이 메서드가 호출된다. 참고로 지정한 예외 또는 그 예외의 자식 클래스는 모두 잡을 수 있다
     * 때문에 파라미터가 Exception인  exHandler가 첫 번째 두 번째가 처리못한걸 다 처리해줌, 실수로 놓치거나 공통으로 처리하는 것들들
     *
     * 이 컨트롤러엣허 IllegalArgumentException예외 발생하면 이 메서드가 잡음
     * @RestController이기 때문에 제이슨으로 반환해줌, 정상흐름으로 바꿔서 반환, 때문에 코드가 200으로
     * 때문에 @ResponseStatus(HttpStatus.BAD_REQUEST)로 응답코드까지 바꿔줌
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResult illegalExHandler(IllegalArgumentException e) {
        log.error("[exceptionHandler] ex", e);

        return new ErrorResult("BAD", e.getMessage());
    }

    /**
     * @ExceptionHandler 에 예외를 지정하지 않으면 해당 메서드 파라미터 예외를 사용
     * (UserException e)는 @ExceptionHandler(UserException.class)을 대신하는
     */
    @ExceptionHandler
    public ResponseEntity<ErrorResult> userExHandler(UserException e) {
        log.error("[exceptionHandler] ex", e);

        ErrorResult errorResult = new ErrorResult("USER-EX", e.getMessage());

        return new ResponseEntity(errorResult, HttpStatus.BAD_REQUEST);
    }


    /**
     * throw new RuntimeException("잘못된 사용자") 이 코드가 실행되면서, 컨트롤러 밖으로
     * RuntimeException 이 던져진다.
     * RuntimeException 은 Exception 의 자식 클래스이다. 따라서 이 메서드가 호출된다.
     * @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) 로 HTTP 상태 코드를 500으로 응답한다.
     *
     * 200으로 응답코드 보내지않게
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public ErrorResult exHandler(Exception e) {
        log.error("[exceptionHandler] ex", e);
        return new ErrorResult("EX", "내부 오류");
    }

}