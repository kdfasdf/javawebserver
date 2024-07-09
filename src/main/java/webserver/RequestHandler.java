package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequest;
import util.HttpRequestUtils;
import util.HttpResponse;
import util.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class RequestHandler extends Thread{
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private Socket connection;
    public RequestHandler(Socket connectionSocket){
        this.connection=connectionSocket;
    }

    @Override
    public void run(){
        log.debug("New Client Connect! Connected IP {}, PORT : {}",connection.getInetAddress(),connection.getPort());
        try(InputStream in = connection.getInputStream();
            OutputStream out = connection.getOutputStream())
        {
            InputStreamReader isr = new InputStreamReader(in,"UTF-8");
            BufferedReader br = new BufferedReader(isr);
            HttpRequest hr = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);
            String requestPath;
            String defaultPath="/index.html";        //redirect 할 defaultPath로 index.html
            String params;
            boolean cookie=false;
            requestPath=hr.getPath();
            log.debug("request path :{} ",requestPath);
            DataOutputStream dos = new DataOutputStream(out);
            if (Files.exists( Paths.get("./webapp"+requestPath))){ //요청한 웹페이지에 해당하는 파일이 있다면
                byte[] body = Files.readAllBytes(new File("./webapp"+requestPath).toPath());
                if ("/user/create".equals(requestPath))
                {
                    DataBase.addUser(new User(hr.getParameter("userId"),
                            hr.getParameter("password"),
                            hr.getParameter("name"),
                            hr.getParameter("email")));
                    log.debug("new account|id: {} pw: {} name: {} email: {}",
                            hr.getParameter("userId"),
                            hr.getParameter("password"),
                            hr.getParameter("name"),
                            hr.getParameter("email"));
                    response.sendRedirect("/index.html");
                }
                else if ("/user/login".equals(requestPath)){
                    User user = DataBase.findUserById(
                            hr.getParameter("userId")
                    );
                if(user != null){
                    if(user.login(hr.getParameter("password"))){
                        response.addHeader("Set-Cookie","logined=true");
                        response.sendRedirect("/index.html");
                    }else{
                        response.sendRedirect("/user/login_failed.html");
                    }
                }else{
                    response.sendRedirect("/user/login_failed.html");
                }
                }else if("/user/list".equals(requestPath)){
                    if(!isLogin(hr.getHeader("Cookie"))){
                        response.sendRedirect("/user/login.html");
                        return;
                    }

                }else{                                         //없는 경우 기본 페이지인 index.html 페이지를 응답으로 보낸다
                    response.forward(requestPath);
                }
            }
            else{                                         //없는 경우 기본 페이지인 index.html 페이지를 응답으로 보낸다
                log.debug("defaultPath{}",defaultPath);
                byte[] body = Files.readAllBytes(new File("./webapp"+defaultPath).toPath());
                response.sendRedirect(defaultPath);
                response.forward(defaultPath);
            }
        } catch(Exception e){
            log.debug("File not exits");
            e.printStackTrace();

        }
    }

    private String[] parseParam(String params){
        String[] acc = new String[4];
        String[] info=params.split("&");
        for(int i=0; i<info.length;i++){
            String[] value = info[i].split("=");
            acc[i]=value[1];
        }
        return acc;
    }
    private int getContentLength(String line){
        String[] header = line.split(":");
        return Integer.parseInt(header[1].trim());
    }
    private boolean isLogin(String cookieValue){
        Map<String,String> cookies = HttpRequestUtils.parseCookies(cookieValue);
        String value = cookies.get("logined");
        if(value == null){
            return false;
        }
        return Boolean.parseBoolean(value);
    }
}