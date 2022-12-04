package hello.exception.api;

import hello.exception.exception.BadRequestException;
import hello.exception.exception.UserException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** api 예외 */
@Slf4j
/** json으로 화면에 출력하기 위해 */
@RestController
public class ApiExceptionController {

    @GetMapping("/api/members/{id}")
    public MemberDto getMember(@PathVariable("id") String id) {

        /**
         * API를 요청했는데, 정상의 경우 API로 JSON 형식으로 데이터가 정상 반환된다.
         * 그런데 오류가 발생하면 우리가 미리 만들어둔 오류 페이지 HTML이 반환된다. 이것은 기대하는 바가 아니다.
         * 클라이언트는 정상 요청이든, 오류 요청이든 JSON이 반환되기를 기대한다.
         * 웹 브라우저가 아닌 이상 HTML을 직접 받아서 할 수 있는 것은 별로 없다.
         *
         * RuntimeException 예외 발생하면서 ErrorPageController 가 동작함
         * */
        //url에 ex있으면 예외가 터진거
        if (id.equals("ex")) {
            throw new RuntimeException("잘못된 사용자");
        }

        /**
         * 클라이언튜 에러 5xx
         *
         * 클라이언트가 잘못해 에러가 발생한거로 처리하려 그런데
         * 그러면 4XX 에러가 떠야하는데 에러를 던져버려서 5XX 에러가 뜸
         *
         * 스프링 MVC는 컨트롤러(핸들러) 밖으로 예외가 던져진 경우 예외를 해결하고, 동작을 새로 정의할 수 있는 방법을 제공
         * 컨트롤러 밖으로 던져진 예외를 해결하고, 동작 방식을 변경하고 싶으면 HandlerExceptionResolver 를 사용
         *
         * 줄여서 ExceptionResolver 라고 부르는데 컨트롤러에서 예외 터지면 WAS 로 가는 도중 얘가 호출되서
         * 예외를 해결하도록 함, 정상적으로 처리할 수 있도록 해주는
         * 대신 인터셉터 posthandle 은 여전히 호출 안되는
         * */
        if (id.equals("bad")) {
            throw new IllegalArgumentException("잘못된 입력 값");
        }

        //직접 만들어준 예외 발생
        //서블릿컨테이너까지 user-ex 날라갔다 예외 터져서 /error 날라오고 BasicController
        //호출되고 BasicController 에서 결과 내주는
        //BasicController 가 요청온게 json 이면 같은 url 이더라도 html 응답이 아닌 json 반환
        if (id.equals("user-ex")) {
            throw new UserException("사용자 오류");
        }

        //에러 없으면 화면에 출력
        return new MemberDto(id, "hello " + id);
    }

    //스프링부트가 제공하는 ExceptionResolver
    @GetMapping("/api/response-status-ex1")
    public String responseStatusEx1() {
        throw new BadRequestException();
    }


    /**
     * ResponseStatusException, 개발자가 정의한 예외가 아닌 기본으로 제공해주는 예외가 터질시
     * @ResponseStatus 는 개발자가 직접 변경할 수 없는 예외에는 적용할 수 없다. 개발자가 정의한 예외서만 사용가능하다는 의미
     *  (애노테이션을 직접 넣어야 하는데, 내가 코드를 수정할 수 없는 라이브러리의 예외 코드 같은 곳에는 적용할 수 없다.)
     * 추가로 애노테이션을 사용하기 때문에 조건에 따라 동적으로 변경하는 것도 어렵다.
     * 이때는 ResponseStatusException 예외를 사용하면 된다.
     */
    @GetMapping("/api/response-status-ex2")
    public String responseStatusEx2() {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "error.bad", new IllegalArgumentException());
    }


    /**
     * DefaultHandlerExceptionResolver 는 스프링 내부에서 발생하는 스프링 예외를 해결한다.
     * 대표적으로 파라미터 바인딩 시점에 타입이 맞지 않으면 내부에서 TypeMismatchException 이 발생하는데,
     * 이 경우 예외가 발생했기 때문에 그냥 두면 서블릿 컨테이너까지 오류가 올라가고, 결과적으로 500 오류가 발생
     * 그런데 파라미터 바인딩은 대부분 클라이언트가 HTTP 요청 정보를 잘못 호출해서 발생하는 문제이다.
     * HTTP 에서는 이런 경우 HTTP 상태 코드 400을 사용하도록 되어 있다.
     * DefaultHandlerExceptionResolver 는 이것을 500 오류가 아니라 HTTP 상태 코드 400 오류로 변경한다.
     * 스프링 내부 오류를 어떻게 처리할지 수 많은 내용이 정의
     *
     * 내부에서 터진걸 HTTP 스펙에 맞는 HTTP 상태 코드로 바꿔 처리
     * */
    @GetMapping("/api/default-handler-ex")
    public String defaultException(@RequestParam Integer data) {
        return "ok";
    }


    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String memberId;
        private String name;
    }
}