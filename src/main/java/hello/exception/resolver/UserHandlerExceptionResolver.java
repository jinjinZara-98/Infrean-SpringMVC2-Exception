package hello.exception.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.exception.exception.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//WebConfig에 추가
//사실 이런 ExceptionResolver 직접 구현할 필요없이 스프링에서 제공함
//UserException 처리하는
@Slf4j
public class UserHandlerExceptionResolver implements HandlerExceptionResolver {

    //errorResult객체를 문자로 바꿔주는
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        try {

            //넘어온 오류가 UserException이면
            //이 예외면 서블릿 컨테이너까지 가서 /error 로 다시 내려오고 그런 흐름이 안 생김
            //서블릿 컨테이너까지 가고 정상적으로 끝남
            //여기서 정상적인 ModelAndView 반환해서 WAS 로
            //컨트롤러에서 예외가 터져 서블릿 WAS 까지 가지 않고
            //중간에서 얘가 예외 잡아 처리 후 그냥 빈 ModelAndView 생성해 반환
            if (ex instanceof UserException) {
                log.info("UserException resolver to 400");


                String acceptHeader = request.getHeader("accept");
                //나갈 응답상태를 세팅
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                //acceptHeader가 json이면
                if ("application/json".equals(acceptHeader)) {

                    Map<String, Object> errorResult = new HashMap<>();

                    //예외정보는 예외 클래스와 메시지만
                    errorResult.put("ex", ex.getClass());
                    errorResult.put("message", ex.getMessage());

                    //예외 데이터를 response에 넣어줘야함
                    //에리데이터 errorResult 객체를 문자열로 바꿈
                    String result = objectMapper.writeValueAsString(errorResult);

                    //response 세팅
                    response.setContentType("application/json");
                    response.setCharacterEncoding("utf-8");
                    //변환된 데이터 json을 http응답바디에 적음
                    //ModelAndView() 로 반환해야 되서 다 세팅해야함
                    //json을 문자로 바꿈
                    response.getWriter().write(result);

                    return new ModelAndView();

                } else {//그 외의 케이스, json 이 아니면 json으로 응답안하는
                    // TEXT/HTML
                    //templates/error/500
                    return new ModelAndView("error/500");
                }
            }

        } catch (IOException e) {
            log.error("resolver ex", e);
        }

        return null;
    }
}
