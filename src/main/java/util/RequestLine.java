package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class RequestLine {
    private static final Logger log = LoggerFactory.getLogger(RequestLine.class);
    private HttpMethod method;
    private String path;
    private String queryString;
    public RequestLine(String requestLine){
        log.debug("request line : {}",requestLine);
        //request line : GET /index.html HTTP/1.1
        String[] tokens = requestLine.split(" ");
        method = HttpMethod.valueOf(tokens[0]);
        if(method.isPost())
        {
            path=tokens[1];
            return ;
        }
        int index = tokens[1].indexOf("?");
        if(index==-1)
            path=tokens[1];
        else{
            path=tokens[1].substring(0,index);
            queryString=tokens[1].substring(index+1);
        }
    }
    public HttpMethod getMethod(){
        return method;
    }
    public String getPath(){
        return path;
    }
    public String getQueryString(){
        return queryString;
    }
}
