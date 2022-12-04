package hello.exception.resolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ExceptionResolver을 적용 전에는 prehandle을 거치고 컨트롤러에서 예외가 터지면 posthandle을 가지않고 was에 예외 전달했지만
 * ExceptionResolver을 적용하면 posthandle을 가지 않는거는 동일하지만 ExceptionResolver에서 예외를 처리해 was와 view에 정상응답을 할 수 있음
 *
 * ExceptionResolver 활용
 *
 * 예외 상태 코드 변환
 * 예외를 response.sendError(xxx) 호출로 변경해서 서블릿에서 상태 코드에 따른 오류를 처리하도록 위임
 * 이후 WAS는 서블릿 오류 페이지를 찾아서 내부 호출, 예를 들어서 스프링 부트가 기본으로 설정한 /error 가 호출됨
 *
 * 뷰 템플릿 처리
 * ModelAndView 에 값을 채워서 예외에 따른 새로운 오류 화면 뷰 렌더링 해서 고객에게 제공
 *
 * API 응답 처리
 * response.getWriter().println("hello"); 처럼 HTTP 응답 바디에 직접 데이터를 넣어주는 것도 가능하다.
 *  여기에 JSON 으로 응답하면 API 응답 처리를 할 수 있다.
 *
 * ExceptionResolver 를 사용하면 컨트롤러에서 예외가 발생해도 ExceptionResolver 에서 예외를 처리해버린다.
 * 따라서 예외가 발생해도 서블릿 컨테이너까지 예외가 전달되지 않고, 스프링 MVC에서 예외 처리는 끝이 난다.
 * 결과적으로 WAS 입장에서는 정상 처리가 된 것이다. 이렇게 예외를 이곳에서 모두 처리할 수 있다는 것이 핵심이다.
 * 서블릿 컨테이너까지 예외가 올라가면 복잡하고 지저분하게 추가 프로세스가 실행된다. 반면에
 * ExceptionResolver 를 사용하면 예외처리가 상당히 깔끔해진다.
 * 그런데 직접 ExceptionResolver 를 구현하려고 하니 상당히 복잡
 *
 * WebConfig에 등록해줘야함
 *
 * 쉽게 말해 컨트롤러에서 예외 터지면 여기서 처리해 정상적으로 동작하게 함
 * 예외 먹고 HTTP 상태 코드 설정해서
 * */
@Slf4j
public class MyHandlerExceptionResolver implements HandlerExceptionResolver {

    //Exception ex 예외가 오면 정상적인 ModelAndView를 반환
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        log.info("call resolver", ex);

        try {
            //넘어온 오류가 IllegalArgumentException이면 400으로 내보내는
            if (ex instanceof IllegalArgumentException) {

                log.info("IllegalArgumentException resolver to 400");

                //이 예외르 여기서 먹어버리고 400을 내보냄
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());


                /**
                 * 빈 ModelAndView: new ModelAndView() 처럼 빈 ModelAndView 를 반환하면 뷰를 렌더링 하지 않고, 정상 흐름으로 서블릿이 리턴된다.
                 * ModelAndView 지정: ModelAndView 에 View , Model 등의 정보를 지정해서 반환하면 뷰를 렌더링한다.
                 * null: null 을 반환하면, 다음 ExceptionResolver 를 찾아서 실행한다.
                 * 만약 처리할 수 있는 ExceptionResolver 가 없으면 예외 처리가 안되고, 기존에 발생한 예외를 서블릿 밖으로 던진다.
                 *
                 * 파라미터 없는 ModelAndView 넘기면 정상적인 흐름으로 반환되서 was까지 감, 에러인식은 400으로 알게되고
                 */
                return new ModelAndView();
            }

        } catch (IOException e) {//sendError가 IOException으로 checkedexception으로 되어있어서 잡아줘야함
            log.error("resolver ex", e);
        }

        return null;
    }
}
