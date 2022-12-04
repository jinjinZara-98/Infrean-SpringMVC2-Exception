package hello.exception.filter;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

//서버 내부에서 오류 페이지를 호출한다고 해서 해당
//필터나 인터셉트가 한번 더 호출되는 것은 매우 비효율적, 경우에 따라서 중복호출되면 안되는 필터도 있을 수 있음
//결국 클라이언트로 부터 발생한 정상 요청인지, 아니면 오류 페이지를 출력하기 위한 내부 요청인지 구분할수 있어야 한다.
//서블릿은 이런 문제를 해결하기 위해 DispatcherType 이라는 추가 정보를 제공

//필터는 이런 경우를 위해서 dispatcherTypes 라는 옵션을 제공한다.
//이전 강의의 마지막에 다음 로그를 추가했다.
//log.info("dispatchType={}", request.getDispatcherType())
//그리고 출력해보면 오류 페이지에서 dispatchType=ERROR 로 나오는 것을 확인할 수 있다.

//고객이 처음 요청하면 dispatcherType=REQUEST 이다.
//이렇듯 서블릿 스펙은 실제 고객이 요청한 것인지, 서버가 내부에서 오류 페이지를 요청하는 것인지
//DispatcherType 으로 구분할 수 있는 방법을 제공

//처음에 /error-ex로 url에 호출하면 doFilter호출하면 equest.getDispatcherType()는 request가 됨
//dispatcherservlet지나서 chain.doFilter로 ServletExController클래스의 errorEx() 호출
//그럼 그 메서드의 예외가 터져 현재 doFilter메서드의 catch문으로 와서 예외잡음, 그 다음 finally문
//그 다음 was까지 올라가 메시지 찍고 다시 내려옴
//WebServerCustomizer에서 만든 오류페이지로 요청, 현재 doFilter의 request.getDispatcherType()가 에러로
//출력다하고 finallu

//filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ERROR);
//이렇게 두 가지를 모두 넣으면 클라이언트 요청은 물론이고, 오류 페이지 요청에서도 필터가 호출된다.
//아무것도 넣지 않으면 기본 값이 DispatcherType.REQUEST 이다. 즉 클라이언트의 요청이 있는 경우에만
//필터가 적용된다. 특별히 오류 페이지 경로도 필터를 적용할 것이 아니면, 기본 값을 그대로 사용하면 된다.
//물론 오류 페이지 요청 전용 필터를 적용하고 싶으면 DispatcherType.ERROR 만 지정하면 된다

//등록은 WebConfig클래스에
@Slf4j
public class LogFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("log filter init");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        String uuid = UUID.randomUUID().toString();

        try {
            log.info("REQUEST  [{}][{}][{}]", uuid, request.getDispatcherType(), requestURI);
            chain.doFilter(request, response);

        } catch (Exception e) {
            log.info("EXCEPTION {}", e.getMessage());
            throw e;

        } finally {
            log.info("RESPONSE [{}][{}][{}]", uuid, request.getDispatcherType(), requestURI);
        }

    }

    @Override
    public void destroy() {
        log.info("log filter destroy");
    }
}