package hello.exception;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 스프링 부트가 제공하는 BasicErrorController 는 HTML 페이지를 제공하는 경우에는 매우 편리하다.
 * 4xx, 5xx 등등 모두 잘 처리해준다. 그런데 API 오류 처리는 다른 차원의 이야기이다. API 마다, 각각의
 * 컨트롤러나 예외마다 서로 다른 응답 결과를 출력해야 할 수도 있다. 예를 들어서 회원과 관련된 API에서
 * 예외가 발생할 때 응답과, 상품과 관련된 API에서 발생하는 예외에 따라 그 결과가 달라질 수 있다.
 * 결과적으로 매우 세밀하고 복잡하다. 따라서 이 방법은 HTML 화면을 처리할 때 사용하고, API는 오류
 * 처리는 뒤에서 설명할 @ExceptionHandler 를 사용
 *
 * API 의 경우 좀 다르다. 안드로이드나 ios 에서 앱으로 서버를 호출, 기업들간의 시스템 통신 또는 마이크로서비스의 서버 시스템들끼리
 * 고객에게 보여줄 화면이 아니라 정확한 데이터 줘야, 각 오류 상황에 맞는 오류 응답 스펙 정하고 JSON 으로 데이터 내려줘야
 */
@SpringBootApplication
public class ExceptionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExceptionApplication.class, args);
	}

}
