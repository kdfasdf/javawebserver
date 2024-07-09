package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequest;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

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
    /*  33번째 줄로 대체
            String line = HttpRequestUtils.getUrl(br.readLine());
            int index = line.indexOf("?");              //url 유저정보 파라미터를 분리하기 위해 ?의 인덱스를 저장한다
  */
            String requestPath;
            String defaultPath="/index.html";        //redirect 할 defaultPath로 index.html
            String params;
            boolean cookie=false;
            requestPath=hr.getPath();
            /*
            if (index==-1){                //indexOf는 해당 문자에 대한 index를 찾을 수 없으면 -1을 반환한다
                requestPath=line;
                params="";
            }
            else {
                requestPath = line.substring(0, index);    //? 앞부분 추출 즉 /user/create
                params = line.substring(index + 1);
            }*/
            DataOutputStream dos = new DataOutputStream(out);
            if (Files.exists( Paths.get("./webapp"+requestPath))){ //요청한 웹페이지에 해당하는 파일이 있다면
                byte[] body = Files.readAllBytes(new File("./webapp"+requestPath).toPath());
                if(hr.getHeader("Cookie")!=null) {
                    String[] temp = hr.getHeader("Cookie").split("=");
                    String[] temp2 = temp[1].split(" ");
                    log.debug("coookieheadercheck{}",temp2[0]);
                    cookie = Boolean.parseBoolean(temp2[0]);
                }
                /*
                while(!line.equals("")){
                    line=br.readLine();
                    if(line.contains("Cookie")){
                        cookie=Boolean.parseBoolean(HttpRequestUtils.parseCookies(line.split(":")[1]).get("logined"));
                        log.debug("{} \n{} Cookie:{}",line,HttpRequestUtils.parseCookies(line.split(":")[1]).get("logined"),cookie);
                    }
                }*/
                //

                urlFunction(requestPath,dos,hr,body,cookie);    //웹 url에 따라 다른 기능을 수행하기 위함
            }
            else{                                         //없는 경우 기본 페이지인 index.html 페이지를 응답으로 보낸다
                log.debug("defaultPath{}",defaultPath);
                byte[] body = Files.readAllBytes(new File("./webapp"+defaultPath).toPath());
                //post 요청 body 읽기;
                int contentLength=0;            //로그인 쿠키 확인
                /*while(!line.equals("")){
                    line=br.readLine();
                    if(line.contains("Content-Length")){
                        contentLength = getContentLength(line);
                    }
                }*/
                //post 요청 body 읽기
                urlFunction(requestPath, dos, hr, body,cookie);
            }
        } catch(Exception e){
            e.printStackTrace();

        }
    }
    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Accept: text/css,*/*;q=0.1");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");// 웹은 헤더와 body를 구분할 때 \r\n\r\n로 구분함
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302LoginHeader(DataOutputStream dos, int lengthOfBodyContent, User loginUser) {
        try {
            if (loginUser != null)
            {
                dos.writeBytes("HTTP/1.1 302 OK \r\n");
                dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
                dos.writeBytes("location: http://localhost:8080/index.html"+"\r\n");
                dos.writeBytes("Accept: text/css,*/*;q=0.1\r\n");
                dos.writeBytes("Set-cookie: logined=true MaxAge=1800, HttpOnly;\r\n");
                dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
                dos.writeBytes("\r\n");// 웹은 헤더와 body를 구분할 때 \r\n\r\n로 구분함//
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void response302LoginFail(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("location: http://localhost:8080/login_failed.html"+"\r\n");
            dos.writeBytes("Set-cookie: logined=false\r\n");
            dos.writeBytes("Accept: text/css,*/*;q=0.1");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");// 웹은 헤더와 body를 구분할 때 \r\n\r\n로 구분함//


        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("Http/1.1 302 found \r\n");
            dos.writeBytes("location: http://localhost:8080/index.html"+"\r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Accept: text/css,*/*;q=0.1");
            dos.writeBytes("Content-Length: "+lengthOfBodyContent+"\r\n");
            dos.writeBytes("\r\n");
        }catch(IOException e){
            log.error(e.getMessage());
        }
    }

    private void response302Relogin(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("Http/1.1 302 found \r\n");
            dos.writeBytes("location: http://localhost:8080/user/login.html"+"\r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Accept: text/css,*/*;q=0.1");
            dos.writeBytes("Content-Length: "+lengthOfBodyContent+"\r\n");
            dos.writeBytes("\r\n");
        }catch(IOException e){
            log.error(e.getMessage());
        }
    }
    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    //private void urlFunction(String requestPath, DataOutputStream dos, String params, byte[] body,boolean cookie){
    private void urlFunction(String requestPath, DataOutputStream dos, HttpRequest hr, byte[] body,boolean cookie){
        if("/user/create".equals(requestPath)) {
//리펙토링 필요한 부분이라 생각 /user/create 뿐만 아닌 다른 없는 페이지에 대해서도 수행할 수 있어야함
            try{//올바르게 입력되었으면
              //  String[] acc = new String[4];
                DataBase.addUser(new User(hr.getParameter("userId"),hr.getParameter("password"),hr.getParameter("name"),hr.getParameter("email")));
                log.debug("new account|id: {} pw: {} name: {} email: {}",hr.getParameter("userId"),hr.getParameter("password"),hr.getParameter("name"),hr.getParameter("email"));
                response302Header(dos,body.length);
                responseBody(dos, body);
            }catch(Exception e){
                e.printStackTrace();
                response302Header(dos,body.length);
                responseBody(dos, body);
            }
        }
        else if("/user/login".equals(requestPath)){
            //String[] acc = new String[4];
            User loginUser = DataBase.findUserById(hr.getParameter("userId"));
            try {
                log.debug("check");
                log.debug("id: {}", loginUser.getUserId());
                response302LoginHeader(dos,body.length,loginUser);
                responseBody(dos,body);
            }
            catch(Exception e){
                e.printStackTrace();
                try {
                    byte[] loginfail=Files.readAllBytes(new File("./webapp"+"/user/login_failed.html").toPath());
                    response302LoginFail(dos,loginfail.length);
                    responseBody(dos,loginfail);
                } catch(Exception ie)
                {
                    ie.printStackTrace();
                }
            }
        }
        else if("/user/list.html".equals(requestPath)){
            if(cookie==false){
                try{
                    body = Files.readAllBytes(new File("./webapp/user/login.html").toPath());

                    response302Relogin(dos,body.length);
                    responseBody(dos, body);
                }catch(Exception re){
                    re.printStackTrace();
                }
            }
            else{
                Collection<User> users = DataBase.findAll();
                StringBuilder sb = new StringBuilder();
                sb.append("<table border = '1'>");
                for(User user :users){
                    sb.append("<tr>");
                    sb.append("<td>"+user.getUserId()+"</td>");
                    sb.append("<td>"+user.getName()+"</td>");
                    sb.append("<td>"+user.getEmail()+"</td>");
                    sb.append("</tr>");
                }
                sb.append("</table>");
                try{
                    byte[] userlist = sb.toString().getBytes();
                    response200Header(dos,userlist.length);
                    responseBody(dos,userlist);}catch(Exception a){a.printStackTrace();}{
                }
            }
        }
        else{
            response200Header(dos,body.length);
            responseBody(dos, body);
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
}