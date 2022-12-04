package hello.exception.servlet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//스프링 부트가 제공하는 기본 오류 메커니즘을 사용하도록 WebServerCustomizer에 있는
//@Component 를 주석 처리하자.
//이제 오류가 발생했을 때 오류 페이지로 /error 를 기본 요청한다. 스프링 부트가 자동 등록한
//BasicErrorController 는 이 경로를 기본으로 받는다.
//개발자는 오류 페이지만 등록
//BasicErrorController 는 기본적인 로직이 모두 개발되어 있다.
//개발자는 오류 페이지 화면만 BasicErrorController 가 제공하는 룰과 우선순위에 따라서 등록하면
//된다. 정적 HTML이면 정적 리소스, 뷰 템플릿을 사용해서 동적으로 오류 화면을 만들고 싶으면 뷰 템플릿
//경로에 오류 페이지 파일을 만들어서 넣어두기만 하면 된다.
//뷰 선택 우선순위

//BasicErrorController 의 처리 순서

//1. 뷰 템플릿
//resources/templates/error/500.html
//resources/templates/error/5xx.html

//2. 정적 리소스( static , public )
//resources/static/error/400.html
//resources/static/error/404.html
//resources/static/error/4xx.html

//3. 적용 대상이 없을 때 뷰 이름( error )
//resources/templates/error.html
//해당 경로 위치에 HTTP 상태 코드 이름의 뷰 파일을 넣어두면 된다.
//뷰 템플릿이 정적 리소스보다 우선순위가 높고, 404, 500처럼 구체적인 것이 5xx처럼 덜 구체적인 것 보다
//우선순위가 높다.
//5xx, 4xx 라고 하면 500대, 400대 오류를 처리해준다

//ServletExController  WebServerCustomizer  ErrorPageController  해당하는 뷰
//이 순서대로 동작

//WAS(여기까지 전파) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(예외발생)
//WAS `/error-page/500` 다시 요청 -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러(/error-page/500) -> View
//중요한 점은 웹 브라우저(클라이언트)는 서버 내부에서 이런 일이 일어나는지 전혀 모른다는 점이다. 오직
//서버 내부에서 오류 페이지를 찾기 위해 추가적인 호출을 한다. 컨트롤러 2번 호출

//WAS는 오류 페이지를 단순히 다시 요청만 하는 것이 아니라, 오류 정보를 request 의 attribute 에 추가해서 넘겨준다.
//필요하면 오류 페이지에서 이렇게 전달된 오류 정보를 사용할 수 있다

//뷰 템플릿, 타임리프로 수정가능, 오류페이지를 동적으로 수정하면서 만드는
@Slf4j
@Controller
public class ServletExController {

    //예외 터진건 무조건 500으로
    @GetMapping("/error-ex")
    public void errorEx() {
        throw new RuntimeException("예외 발생!");
    }

    //서블릿은 다음 2가지 방식으로 예외 처리를 지원한다.
    //Exception (예외) 500
    //개발자가 직접 에러 만드는
    //response.sendError(HTTP 상태 코드, 오류 메시지

    //실제로 예외가 발생하는건 아님
    //WAS(sendError 호출 기록 확인) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러
    //response.sendError() 를 호출하면 response 내부에는 오류가 발생했다는 상태를 저장해둔다.
    //그리고 서블릿 컨테이너는 고객에게 응답 전에 response 에 sendError() 가 호출되었는지 확인한다.
    //그리고 호출되었다면 설정한 오류 코드에 맞추어 기본 오류 페이지를 보여준다.
    //실행해보면 다음처럼 서블릿 컨테이너가 기본으로 제공하는 오류 화면을 볼 수 있다.
    @GetMapping("/error-404")
    public void error404(HttpServletResponse response) throws IOException {
        response.sendError(404, "404 오류!");
    }

    @GetMapping("/error-400")
    public void error400(HttpServletResponse response) throws IOException {
        response.sendError(400, "400 오류!");
    }

    @GetMapping("/error-500")
    public void error500(HttpServletResponse response) throws IOException {
        response.sendError(500);
    }
}
