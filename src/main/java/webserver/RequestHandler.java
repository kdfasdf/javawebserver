package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

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
            String line = HttpRequestUtils.getUrl(br.readLine());
            int index = line.indexOf("?");              //url 유저정보 파라미터를 분리하기 위해 ?의 인덱스를 저장한다
            String requestPath;
            String defaultPath="/index.html";        //redirect 할 defaultPath로 index.html
            String params;
            if (index==-1){                //indexOf는 해당 문자에 대한 index를 찾을 수 없으면 -1을 반환한다
                requestPath=line;
                params="";
            }
            else {
                requestPath = line.substring(0, index);    //? 앞부분 추출 즉 /user/create
                params = line.substring(index + 1);
            }
            DataOutputStream dos = new DataOutputStream(out);
            if (Files.exists( Paths.get("./webapp"+requestPath))){ //요청한 웹페이지에 해당하는 파일이 있다면
                byte[] body = Files.readAllBytes(new File("./webapp"+requestPath).toPath());
                urlFunction(requestPath,dos,params,body);    //웹 url에 따라 다른 기능을 수행하기 위함
            }
            else{                                         //없는 경우 기본 페이지인 index.html 페이지를 응답으로 보낸다
                log.debug("defaultPath{}",defaultPath);
                byte[] body = Files.readAllBytes(new File("./webapp"+defaultPath).toPath());
                //post 요청 body 읽기;
                int contentLength=0;
                while(!line.equals("")){
                    line=br.readLine();
                    if(line.contains("Content-Length")){
                        contentLength = getContentLength(line);
                    }
                }
                String user = IOUtils.readData(br,contentLength);
                //post 요청 body 읽기
                urlFunction(requestPath, dos, user, body);
            }
        } catch(Exception e){
            e.printStackTrace();

        }
    }
    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
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
                dos.writeBytes("Cookie: logined=true\r\n");
                dos.writeBytes("Set-cookie: logined=true\r\n");
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
                dos.writeBytes("Cookie: logined=true\r\n");
                dos.writeBytes("Set-cookie: logined=false\r\n");
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

    private void urlFunction(String requestPath, DataOutputStream dos, String params, byte[] body){
        if("/user/create".equals(requestPath)) {
//리펙토링 필요한 부분이라 생각 /user/create 뿐만 아닌 다른 없는 페이지에 대해서도 수행할 수 있어야함
            try{//올바르게 입력되었으면
                String[] acc = new String[4];
                acc=parseParam(params);
                DataBase.addUser(new User(acc[0],acc[1],acc[2],acc[3]));
                log.debug("new account|id: {} pw: {} name: {} email: {}",acc[0],acc[1],acc[2],acc[3]);
                response302Header(dos,body.length);
                responseBody(dos, body);
            }catch(Exception e){
                e.printStackTrace();
                response302Header(dos,body.length);
                responseBody(dos, body);
            }
        }
        else if("/user/login".equals(requestPath)){
            String[] acc = new String[4];
            acc=parseParam(params);
            User loginUser = DataBase.findUserById(acc[0]);
            try {
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