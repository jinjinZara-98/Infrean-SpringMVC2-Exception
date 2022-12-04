package hello.exception;

import hello.exception.filter.LogFilter;
import hello.exception.interceptor.LogInterceptor;
import hello.exception.resolver.MyHandlerExceptionResolver;
import hello.exception.resolver.UserHandlerExceptionResolver;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.util.List;

///hello 정상 요청
//WAS(/hello, dispatchType=REQUEST) -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러 -> View
///error-ex 오류 요청
//필터는 DispatchType 으로 중복 호출 제거 ( dispatchType=REQUEST )
//인터셉터는 경로 정보로 중복 호출 제거( excludePathPatterns("/error-page/**") )
//1. WAS(/error-ex, dispatchType=REQUEST) -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러
//2. WAS(여기까지 전파) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(예외발생)
//3. WAS 오류 페이지 확인
//4. WAS(/error-page/500, dispatchType=ERROR) -> 필터(x) -> 서블릿 -> 인터셉터(x) ->
//컨트롤러(/error-page/500) -> View

//스프링 빈에 등록되야 하므로 어노테이션 붙임
@Configuration
public class WebConfig implements WebMvcConfigurer {

    //인터셉터는 필터처럼 dispatchertype세팅할 수 없음, 대신 excludePathPatterns잇음
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LogInterceptor())
                .order(1)
                .addPathPatterns("/**")
                //오류 페이지 다시 요청될 때 오류 페이지 경로를 제외 경로에 넣어줌으로써
                //인터셉터에 적용되지 않게
                .excludePathPatterns("/css/**", "*.ico", "/error", "/error-page/**");//오류 페이지 경로
    }

    //configureHandlerExceptionResolvers(..) 를 사용하면 스프링이 기본으로 등록하는
    //ExceptionResolver 가 제거되므로 주의, extendHandlerExceptionResolvers 를 사용
    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add(new MyHandlerExceptionResolver());
        resolvers.add(new UserHandlerExceptionResolver());
    }

    //인터셉터를 쓰면 필터는 적용되지 않게
    //    @Bean
    public FilterRegistrationBean logFilter() {
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        //만든 LogFilter등록
        filterRegistrationBean.setFilter(new LogFilter());
        filterRegistrationBean.setOrder(1);
        filterRegistrationBean.addUrlPatterns("/*");

        //LogFilter() 이 필터는 요청과 에러 두 가지의 경우에 호출이 되고 사용된다
        //파라미터로 아무것도 넣지 않으면 요청이 DispatcherType.REQUEST 기본값으로
        //즉 클라이언트 요청 있는 경우만 필터 적용되는, 에러 페이지 호출하려는 url 요청은 필터가 적용 안됨
        filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ERROR);

        return filterRegistrationBean;
    }
}
