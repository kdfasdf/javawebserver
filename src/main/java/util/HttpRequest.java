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
    private Map<String,String> headers = new HashMap<String,String>(); //getHeader("필드 이름")을 통해 접근 가능하도록
    private Map<String,String> params = new HashMap<String,String>(); //getParameter("인자 이름")을 통해 접근 가능하도록
    public HttpRequest(InputStream in){ //클라이언트 요청 데이터를 담고 있는 InputStream을 생성자로 받음
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));
            String line = br.readLine(); // 요청 라인 읽기(헤더 첫줄)
            if(line == null){
                return ;
            }
            processRequestLine(line); //요청 라인 처리


            while(true){
                line = br.readLine();
                if(line==null||line.equals("")){//첫번째 조건은 GET인경우 헤더를 다 읽었을 때 두번째 조건은 POST인경우(헤더 body 구분)
                    break;
                }
                log.debug("header : {}",line);
                String[] tokens = line.split(":");
                if (tokens.length==2)
                    headers.put(tokens[0].trim(),tokens[1].trim()); // 헤더, 헤더 내용을 headers에 저장
                else if(tokens.length==3)
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(tokens[1]);
                    sb.append(":");
                    sb.append(tokens[2]);
                    headers.put(tokens[0].trim(),sb.toString().trim());
                }
            }
            if(method.isPost()){
                String body = IOUtils.readData(br,Integer.parseInt(headers.get("Content-Length")));
                params = HttpRequestUtils.parseQueryString(body);
            }
        } catch(IOException e){
            e.printStackTrace();
        }

    }

    public void processRequestLine(String requestLine){
        log.debug("request line : {}",requestLine);
        String[] tokens = requestLine.split(" ");
        method = HttpMethod.valueOf(tokens[0]);     //메서드 방식
        if(method.isPost()){
            path=tokens[1]; //post인 경우 별 다른 경우 없이 파일 경로
            return ;
        }
        int index = tokens[1].indexOf("?");
        if(index==-1) { //쿼리스트링이 없으면 파일 경로
            path = tokens[1];
        }else{
            path=tokens[1].substring(0,index);//? 이전 까지가 경로
            params = HttpRequestUtils.parseQueryString(tokens[1].substring(index+1));
        }
    }
    public HttpMethod getMethod(){
        return method;
    }
    public String getPath(){
        return path;
    }
    public String getHeader(String name){
        return headers.get(name);
    }
    public String getParameter(String name){
        return params.get(name);
    }
}

