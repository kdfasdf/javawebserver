package util;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class HttpRequestTest{
    private HttpMethod method;
    private String testDirectory = "./src/test/resources/";
    @Test
    public void requestGet() throws Exception{
        InputStream in = new FileInputStream(new File(testDirectory+"Http_GET.txt"));
        HttpRequest request = new HttpRequest(in);
        assertEquals(method.GET,request.getMethod());
        assertEquals("/user/create",request.getPath());
        assertEquals("localhost:8080",request.getHeader("Host"));
        assertEquals("keep-alive",request.getHeader("Connection"));
        assertEquals("testcode",request.getParameter("userID"));
        assertEquals("logined=true",request.getHeader("Cookie"));
    }
    @Test
    public void request_POST() throws Exception{
        InputStream in = new FileInputStream(new File(testDirectory+"Http_POST.txt"));
        HttpRequest request = new HttpRequest(in);
        assertEquals(method.POST,request.getMethod());
        assertEquals("/user/create",request.getPath());
        assertEquals("localhost:8080",request.getHeader("Host"));
        assertEquals("46",request.getHeader("Content-Length"));
        assertEquals("application/x-www-form-urlencoded",request.getHeader("Content-Type"));
        assertEquals("keep-alive",request.getHeader("Connection"));
        assertEquals("name",request.getParameter("name"));
    }


}