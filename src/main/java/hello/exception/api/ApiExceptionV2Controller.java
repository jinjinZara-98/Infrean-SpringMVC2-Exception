package hello.exception.api;

import hello.exception.exception.UserException;
import hello.exception.exhandler.ErrorResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @ExceptionHandler로 사용하는, 예외 처리 컨트롤러는 ExControllerAdvice에 있음
 *
 * 지금까지 HTTP 상태 코드를 변경하고, 스프링 내부 예외의 상태코드를 변경하는 기능도 알아보았다.
 * 그런데 HandlerExceptionResolver 를 직접 사용하기는 복잡하다.
 * API 오류 응답의 경우 response 에 직접 데이터를 넣어야 해서 매우 불편하고 번거롭다. 서블릿 짜듯이
 * ModelAndView 를 반환해야 하는 것도 API에는 잘 맞지 않는다.
 * 스프링은 이 문제를 해결하기 위해 @ExceptionHandler 라는 매우 혁신적인 예외 처리 기능을 제공한다.
 * 그것이 아직 소개하지 않은 ExceptionHandlerExceptionResolver 이다.
 *
 * HTML 화면 오류 vs API 오류
 * 웹 브라우저에 HTML 화면을 제공할 때는 오류가 발생하면 BasicErrorController 를 사용하는게 편하다.
 * 이때는 단순히 5xx, 4xx 관련된 오류 화면을 보여주면 된다.
 * BasicErrorController 는 이런 메커니즘을 모두 구현해두었다.
 * 그런데 API는 각 시스템 마다 응답의 모양도 다르고, 스펙도 모두 다르다.
 * 예외 상황에 단순히 오류 화면을 보여주는 것이 아니라, 예외에 따라서 각각 다른 데이터를 출력해야 할 수도 있다.
 * 그리고 같은 예외라고 해도 어떤 컨트롤러에서 발생했는가에 따라서 다른 예외 응답을 내려주어야 할 수 있다.
 * 한마디로 매우 세밀한 제어가 필요하다.
 * 앞서 이야기했지만, 예를 들어서 상품 API와 주문 API는 오류가 발생했을 때 응답의 모양이 완전히 다를 수 있다.
 * 결국 지금까지 살펴본 BasicErrorController 를 사용하거나 HandlerExceptionResolver 를
 * 직접 구현하는 방식으로 API 예외를 다루기는 쉽지 않다.
 *
 * API 예외처리의 어려운 점
 * HandlerExceptionResolver 를 떠올려 보면 ModelAndView 를 반환해야 했다. 이것은 API 응답에는 필요하지 않다.
 * API 응답을 위해서 HttpServletResponse 에 직접 응답 데이터를 넣어주었다. 이것은 매우 불편하다.
 * 스프링 컨트롤러에 비유하면 마치 과거 서블릿을 사용하던 시절로 돌아간 것 같다.
 * 특정 컨트롤러에서만 발생하는 예외를 별도로 처리하기 어렵다.
 * 예를 들어서 회원을 처리하는 컨트롤러에서 발생하는 RuntimeException 예외와 상품을 관리하는 컨트롤러에서 발생하는 동일한
 * RuntimeException 예외를 서로 다른 방식으로 처리하고 싶다면 어떻게 해야할까?
 *
 * @ExceptionHandler
 * 스프링은 API 예외 처리 문제를 해결하기 위해 @ExceptionHandler 라는 애노테이션을 사용하는 매우
 * 편리한 예외 처리 기능을 제공하는데, 이것이 바로 ExceptionHandlerExceptionResolver 이다.
 * 스프링은 ExceptionHandlerExceptionResolver 를 기본으로 제공하고, 기본으로 제공하는
 * ExceptionResolver 중에 우선순위도 가장 높다. 실무에서 API 예외 처리는 대부분 이 기능을 사용
 *
 */
@Slf4j
@RestController
public class ApiExceptionV2Controller {

    //이 컨트롤러에서 이 예외 터지면 이 메서드가 잡음
    //이 컨트롤러 안에서만 적용, 이 컨트롤러 안에서 발생한 예외만
    //그리고 메서드 로직 수행하는데 이 컨트롤러는 @RestController 니까 json 으로 반환
    //즉 여기 컨트롤러에서 처리하고 싶은 예외 지정

    //컨트롤러에서 예외 터지면 ExceptionResolver 가 해결 시도를 함
    //첫 번째 물어보는 애가 ExceptionHandlerMethodResolver
    //ExceptionHandlerMethodResolver는 컨트롤러에 @ExceptionHandler 있는지 확인
    //@ExceptionHandler 있으면 이 메서드 호출
    //근데 정상흐름으로 바꿔서 리턴해줘서 HTTP 상태 코드가 200으로 바뀜
    //기대하는건 그게 아니다, HTTP 상태 코드를 바꿔줘야
    //그래서 @ResponseStatus 붙여서
    //얘도 똑같이 WAS 까지 예외 가는걸 잡아 정상흐름으로 반환되게 함

    //사실은 APU 뿐만 아니라 MVC 까지 다 처리 가능
    //@Controller 적고 그냥 문자열 반환하면 뷰 이름
    //근데 대부분은 API 처리할 때

    //실행 흐름
    //컨트롤러를 호출한 결과 IllegalArgumentException 예외가 컨트롤러 밖으로 던져진다.
    //예외가 발생했으로 ExceptionResolver 가 작동한다. 가장 우선순위가 높은
    //ExceptionHandlerExceptionResolver 가 실행된다.
    //ExceptionHandlerExceptionResolver 는 해당 컨트롤러에 IllegalArgumentException 을 처리할
    //수 있는 @ExceptionHandler 가 있는지 확인한다.
    //illegalExHandle() 를 실행한다. @RestController 이므로 illegalExHandle() 에도
    //@ResponseBody 가 적용된다. 따라서 HTTP 컨버터가 사용되고, 응답이 다음과 같은 JSON으로 반환된다.
    //@ResponseStatus(HttpStatus.BAD_REQUEST) 를 지정했으므로 HTTP 상태 코드 400으로 응답한다.
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResult illegalExHandle(IllegalArgumentException e) {
        log.error("[exceptionHandle] ex", e);
        return new ErrorResult("BAD", e.getMessage());
    }

    //@ExceptionHandler 에 예외를 지정하지 않으면 해당 메서드 파라미터 예외를 사용한다.
    //여기서는 UserException 을 사용한다.
    //ResponseEntity 를 사용해서 HTTP 메시지 바디에 직접 응답한다. 물론 HTTP 컨버터가 사용된다.
    //ResponseEntity 를 사용하면 HTTP 응답 코드를 프로그래밍해서 동적으로 변경할 수 있다. 앞서 살펴본
    //@ResponseStatus 는 애노테이션이므로 HTTP 응답 코드를 동적으로 변경할 수 없다.
    @ExceptionHandler
    public ResponseEntity<ErrorResult> userExHandle(UserException e) {
        log.error("[exceptionHandle] ex", e);
        ErrorResult errorResult = new ErrorResult("USER-EX", e.getMessage());
        return new ResponseEntity<>(errorResult, HttpStatus.BAD_REQUEST);
    }

    //메서드 파라미터에 넣어준 예외는 자식까지 잡아줌
    //즉 위에서 해결하지 못한 경우에는 여기서 다 잡아주는
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public ErrorResult exHandle(Exception e) {
        log.error("[exceptionHandle] ex", e);
        return new ErrorResult("EX", "내부 오류");
    }

    /**
     * 우선순위
     * 스프링의 우선순위는 항상 자세한 것이 우선권을 가진다. 예를 들어서 부모, 자식 클래스가 있고 다음과 같이
     * 예외가 처리된다.
     * @ExceptionHandler(부모예외.class)
     * public String 부모예외처리()(부모예외 e) {}
     * @ExceptionHandler(자식예외.class)
     * public String 자식예외처리()(자식예외 e) {}
     * @ExceptionHandler 에 지정한 부모 클래스는 자식 클래스까지 처리할 수 있다. 따라서 자식예외 가
     * 발생하면 부모예외처리() , 자식예외처리() 둘다 호출 대상이 된다. 그런데 둘 중 더 자세한 것이 우선권을
     * 가지므로 자식예외처리() 가 호출된다. 물론 부모예외 가 호출되면 부모예외처리() 만 호출 대상이 되므로
     * 부모예외처리() 가 호출
     *
     * 다양한 예외
     * 다음과 같이 다양한 예외를 한번에 처리할 수 있다.
     * @ExceptionHandler({AException.class, BException.class})
     * public String ex(Exception e) {
     *  log.info("exception e", e);
     * }
     *
     * 예외 생략
     * @ExceptionHandler 에 예외를 생략할 수 있다. 생략하면 메서드 파라미터의 예외가 지정된다.
     * @ExceptionHandler
     * public ResponseEntity<ErrorResult> userExHandle(UserException e) {}
     *
     */

    @GetMapping("/api2/members/{id}")
    public MemberDto getMember(@PathVariable("id") String id) {

        if (id.equals("ex")) {
            throw new RuntimeException("잘못된 사용자");
        }
        if (id.equals("bad")) {
            throw new IllegalArgumentException("잘못된 입력 값");
        }
        if (id.equals("user-ex")) {
            throw new UserException("사용자 오류");
        }

        return new MemberDto(id, "hello " + id);
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String memberId;
        private String name;
    }

}

