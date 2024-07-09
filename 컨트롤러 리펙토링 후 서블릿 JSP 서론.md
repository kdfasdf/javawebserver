앞에서 구현한 웹 서버는 크게 세 가지 문제점을 가지고 있다. 이 중 앞의 두 가지 문제점을 해결하기 위해 자바 진영에서 표준으로 정한 것이 서블릿 컨테이너와 서블릿/JSP이다.<br>
먼저 서블릿은 앞에서 구현한 웹 서버의 Controller,HttpRequest,HttpResponse를 추상화해 인터페이스로 정의해 놓은 표준이다. 즉 Http의 클라이언트 요청과 응답에 대한 표준을 정해놓은 것이 서블릿이라 생각하면 된다. 서블릿 컨테이너는
이 서블릿 표준에 대한 구현을 담당하고 있으며 앞에서 구현한 웹 서버가 서블릿 컨테이너 역할과 같다고 생각하면 된다<br>
앞에서 구현한 Http 웹 서버는 서버를 시작하는 시점에 Controller의 인터페이스를 생성하고, 요청 URL과 생성한 Controller 인스턴스를 연결시켜 놓는다. 클라이언트에서 요청이 오면 요청 URL에 해당하는 Controller를 찾아 Controller에 실질적인 작업을 위임했다.
서블릿 컨테이너와 서블릿의 동작 방식도 이와 똑같다. 서블릿 컨테이너는 서버가 시작할때 서블릿 인스턴스를 생성해, 요청 URL과 서블릿 인스턴스를 연결해 놓는다. 클라이언트에서 요청이 오면 요청 URL에 해당하는 서블릿을 찾아 서블릿에 모든 작업을 위임한다.
<br>
hello world를 출력하는 간단한 서블릿 예제를 통해 앞서 구현한 Http 웹서버와 비교해보자
- 서블릿 예제
```
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@WebServlet("/hello")
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class HelloWorldServlet extends HttpServlet{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        PrintWriter out = resp.getWriter();
        out.print("Hello World!");
    }
}
```

서블릿 코드와 이전에 구현한 Controller 인터페이스 구현체와 비슷한 것을 알 수 있다. 서블릿은 앞에서 구현했던 Controller와 같은 역할을 하며 똑같은 방식으로 동작한다. 
doGet() 메서드의 인자로 전달하는 HttpServletRequest, HttpServletResponse는 앞에서 구현한
HttpRequest,HttpResponse와 같다. 더 정확히 비교하면 Controller 인터페이스는 서블릿의 Servlet 인터페이스, AvstreacController는 HttpServlet과 같다
서블릿 컨테이너는 서버를 시작할 때 클래스 패스(이클립스)에 있는 클래스 중 HttpServlet을 상속하는 클래스를 찾은 후 @WebServlet 애노테이션의 값을 읽어 요청 URL과 서블릿을 연결하는 Map을 생성한다.
즉, 리펙토링 당시 구현한 RequestMapping의 Map에 서블릿을 추가하고 , 요청 URL에 대한 서블릿을 차장 서비스하는 역할을 서블릿 컨테이너가 담당한다.<br>
즉, 서블릿 컨테이너의 중요한 역할 중의 하나는 서블릿 클래스의 인스턴스 생성, 요청 URL과 서블릿 인스턴스 매핑, 클라이언트 요청에 해당하는 서블릿을 찾은 후 서블릿에 작업을 위임하는 역할을 한다.
이외에도 서블릿 컨테이너는 서블릿과 관련한 초기화와 소멸 작업도 담당한다. Servlet 인터페이스 소스코드를 보면 좀 더 명확히 이해할 수 있다.

```
package javax.servlet;
import java.io.IOException;

public interface Servlet{
    public void init(ServletConfig config) throws ServletException;
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException;
    public void destroy();
    public ServletConfig getServletConfig();
    public String getServletInfo();
}
```

서블릿 컨테이너가 시작하고 종료할 떄의 과정을 단계적으로 살펴보자

- 서블릿 컨테이너 시작
- 클래스패스에 있는 Servlet 인터페이스를 구현하는 서블릿 클래스를 찾음
- @WebServlet 설정을 통해 요청 URL과 서블릿 매핑
- 서블릿 인스턴스 생성
- init()메서드를 호출해 초기화

서블릿 컨테이너는 위 과정으로 서블릿 초기화를 완료한 후 클라이언트 요청이 있을 떄까지 대기상태로 있다가 클라이언트 요청이 있을 경우 요청 URL에 해당하는 서블릿을 찾아 service() 메서드를 호출한다.<br>

서비스를 하다 서블릿 컨테이너를 종료하면 서블릿 컨테이너가 관리하고 있는 모든 서블릿의 destroy() 메서드를 호출해 소멸 작업을 진행한다.<br>
이와 샅이 서블릿 생성, 초기화, 서비스, 소멸 과정을 거치는 전체 과정을 서블릿의 생명주기라 한다. 따라서 서블릿 컨테이너는 서블릿의 생명주기를 관리한다고 이야기 한다. 물론 이외에도 멀티쓰레딩 지원, 설정 파일을 활용한 보안관리, JSP 지원등의 작업을 지원함으로써 개발자가 중요한 비즈니스 로직 구현에 집중할 수 있도록 한다
<br>
앞으로 자바 진영에서 웹 애플리케이션을 개발하면서 컨테이너라는 용어를 접할 기회가 있다. 각 컨테이너마다 다른 기능을 지원할 수 있지만 기본적으로 생명주기를 관리하는 기능을 제공한다. 예를 들어 지금은 거의 사용되지 않는
EJB 컨테이너는 EJB에 대한 생명주기 관리, 스프링 프레임워크에 포함되어 있는 빈 컨테이너는 빈에 대한 생명 주기를 관리하는 기능을 제공한다고 생각할 수 있다.
컨테이너가 관리하는 객체의 인스턴스는 개발자가 직접 생성하는 인스턴스가 아니다. 개발자가직접 인스턴스를 생성한다면 개발자가 원하는 메서드를 호출해 초기화나 소멸과 같은 작업을 진행하면 된다. 하지만
컨테이너에 의해 인스턴스가 관리되기 떄문에 초기화, 소멸과 같은 작업을 위한 메서드를 인터페이스 규약으로 만들어 놓고 확장할 수 있도록 지원하는 것이다.<br>
서블리셍서 알아야 할 중요한 부분 중의 하나는 서블릿 컨테이너가 생성한느 서블릿 인스턴스의 개수이다. 서블릿 컨테이너는 멀티스레드로 동작한다. 즉, 도ㅇ시에 여러 명의 클라이언트가 접속할 수 있도록 지원한다. 그렇다면 서블릿 인스턴스는
몇 개나 생성될까? 새로운 스레드가 생성될 떄마다 새로운 서블릿 인스턴스를 생성할까? 이에 대한 해답은 HTTP 웹 서버 실습에서 구현한 RequestMapping의 Map을 보면 된다. RequestMapping의 Map은 static 키워드로 구현되어 있어 서버가 시작할 때 한 번 초기화 되면 더 이상 초기화하지 않고 계속해서
재사용한다. 서블릿도 같다 서블릿도 서블릿컨테이너가 시작할 때 한번 생성되면 모든 스레드가 같은 인스턴스를 재사용한다. 멀티스레드가 인스턴스 하나를 공유하면서 발생하는 문제와 이에 대한 해결 방법은 다음에 알아보자
 


