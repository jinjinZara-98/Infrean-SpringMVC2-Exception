package hello.exception.servlet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

//이쁜 오류 페이지를 보여주기 위한 컨트롤러
//WebServerCustomizer를 @component로 등록해줘야 동작
@Slf4j
@Controller
public class ErrorPageController {

    //was가 예외를 받으면 requestgetattribute로 밑에 정보들을 담아 다시 보냄. 출력해 볼 수 있음
    //RequestDispatcher 상수로 정의되어 있음
    public static final String ERROR_EXCEPTION = "javax.servlet.error.exception";
    public static final String ERROR_EXCEPTION_TYPE = "javax.servlet.error.exception_type";
    public static final String ERROR_MESSAGE = "javax.servlet.error.message";
    public static final String ERROR_REQUEST_URI = "javax.servlet.error.request_uri";
    public static final String ERROR_SERVLET_NAME = "javax.servlet.error.servlet_name";
    public static final String ERROR_STATUS_CODE = "javax.servlet.error.status_code";

    @RequestMapping("/error-page/404")
    public String errorPage404(HttpServletRequest request, HttpServletResponse response) {
        log.info("errorPage 404");
        printErrorInfo(request);
        return "error-page/404";
    }

    @RequestMapping("/error-page/500")
    public String errorPage500(HttpServletRequest request, HttpServletResponse response) {
        log.info("errorPage 500");
        printErrorInfo(request);
        return "error-page/500";
    }

    /**
     * api예외를 처리하기 위해, ApiExceptionController /api/members/{id} 요청 받고 예외 터져서
     * WebServerCustomizer /error-page/500 요청하면 여기서 받음
     *
     * produces = MediaType는 클라이언트가 보내는 accept타입에 따라 어떤걸 호출하는지
     * 위와 매핑되는 url이 같아도 이게 우선
     *
     * ApiExceptionController에서 ex나오면 런타임오류를 던져 WebServerCustomizer가 받아 이걸 실행
     * 이때 WebServerCustomizer @Component 활성화
     *
     * 쌉중요!!! 근데 사실 BasicErrorController 에는 이 로직도 들어있음
     * 즉 요청 헤더 accept 가 json 이면 같은 url 이더라도 html 파일을 반환하는게 아닌 json 형식으로 반환
     * 스프링부트는  BasicErrorController가 제공하는 기본정보들 활용해 API 만듬
     * */
    @RequestMapping(value = "/error-page/500", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> errorPage500Api(
            HttpServletRequest request, HttpServletResponse response) {

        log.info("API errorPage 500");

        //result에 값 넣으면 json으로 변환됨
        //HashMap은 순서 보장안해서 출력순서 바뀔 수 있음
        Map<String, Object> result = new HashMap<>();
        //위에 정의해놓은 코드 넣는
        Exception ex = (Exception) request.getAttribute(ERROR_EXCEPTION);
        result.put("status", request.getAttribute(ERROR_STATUS_CODE));
        /** 에러 메시지, ApiExceptionController 에서 잘못된 사용자 라는 메시지 넣어줌 */
        result.put("message", ex.getMessage());

        //오류상태코드
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        return new ResponseEntity<>(result, HttpStatus.valueOf(statusCode));
    }
    //결과
    //{
    //  "message": "잘못된 사용자",
    //  "status": 500
    //}
    //HTTP Header에 Accept 가 application/json 이 아니면, 기존 오류 응답인 HTML 응답이 출력되는 것을 확인

    private void printErrorInfo(HttpServletRequest request) {
        log.info("ERROR_EXCEPTION: {}", request.getAttribute(ERROR_EXCEPTION));
        log.info("ERROR_EXCEPTION_TYPE: {}", request.getAttribute(ERROR_EXCEPTION_TYPE));
        log.info("ERROR_MESSAGE: {}", request.getAttribute(ERROR_MESSAGE));
        log.info("ERROR_REQUEST_URI: {}", request.getAttribute(ERROR_REQUEST_URI));
        log.info("ERROR_SERVLET_NAME: {}", request.getAttribute(ERROR_SERVLET_NAME));
        log.info("ERROR_STATUS_CODE: {}", request.getAttribute(ERROR_STATUS_CODE));
        log.info("dispatchType={}", request.getDispatcherType());
    }
}