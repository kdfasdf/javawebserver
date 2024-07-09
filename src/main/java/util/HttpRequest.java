package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
/*
* 클라이언트 요청 데이터에서 요청 라인을 읽고, 헤더를 읽는 로직을 HttpRequest 클래스를 추가해 구현한다
* HttpRequest의 역할은 클라이언트 요청 데이터를 읽은 후 각 데이터를 사용하기 좋은 형태로 분리하는 역할만 한다.
* 이렇게 분리한 데이터를 사용하는 부분은 RequestHandler가 가지도록 한다
* 즉 데이터를 파싱하는 작업과 사용하는 부분을 분리하는 것이다
* */


public class HttpRequest{
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
    private String path;
    private HttpMethod method;
    private RequestLine requestLine;

    private HttpHeaders headers;

    private RequestParams requestParams = new RequestParams();
    public HttpRequest(InputStream in){ //클라이언트 요청 데이터를 담고 있는 InputStream을 생성자로 받음
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            requestLine = new RequestLine(createRequestLine(br));
            requestParams.addQueryString(requestLine.getQueryString());
            headers = new HttpHeaders(br);
            requestParams.addBody(IOUtils.readData(br, headers.getContentLength()));
        } catch(IOException e){
            log.error(e.getMessage());
        }
    }
    private String createRequestLine(BufferedReader br) throws IOException{
        String line = br.readLine();
        if(line==null){
            throw new IllegalStateException();
        }
        return line;
    }
    public HttpMethod getMethod(){
        return method;
    }
    public String getPath(){
        return path;
    }
    public String getHeader(String name){
        return headers.getHeader(name);
    }
    public String getParameter(String name){
        return requestParams.getParameter(name);
    }
}

