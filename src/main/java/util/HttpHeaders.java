package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpHeaders {
    private static final String COOKIE = "Cookie";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final Logger log  = LoggerFactory.getLogger(HttpHeaders.class);
    private Map<String,String> headers = new HashMap<>();

    HttpHeaders(BufferedReader br) throws IOException{
        String line;
        while(!(line=br.readLine()).equals("")) {
            log.debug("header : {}",line);
            String [] splitedHeaders = line.split(":");
            headers.put(splitedHeaders[0],splitedHeaders[1].trim());
        }
    }
    String getHeader(String name){
        return headers.get(name);
    }
    int getIntHeader(String name){
        String header = getHeader(name);
        return header == null?0:Integer.parseInt(header);
    }
    int getContentLength(){
        return getIntHeader(CONTENT_LENGTH);
    }
    HttpCookie getCookie(){
        return new HttpCookie(getHeader(COOKIE));
    }

    HttpSession getSession(){
        return HttpSessions.getSession(getCookie().getCookie("JESSIONID"));
    }
}

