package webserver;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

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
            String url = HttpRequestUtils.getUrl(br.readLine());
            int index = url.indexOf("?");
            String requestPath;
            String defaultPath="/index.html";
            String params;
            if (index==-1){
             requestPath=url;
             params="";
            }
            else {
                requestPath = url.substring(0, index);
                params = url.substring(index + 1);
            }
            DataOutputStream dos = new DataOutputStream(out);
            if (Files.exists( Paths.get("./webapp"+requestPath))){
            byte[] body = Files.readAllBytes(new File("./webapp"+requestPath).toPath());
            urlFunction(requestPath,dos,params,body);
            }
            else{
                log.debug("defaultPath{}",defaultPath);
            byte[] body = Files.readAllBytes(new File("./webapp"+defaultPath).toPath());
            urlFunction(requestPath, dos, params, body);
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

    private void response302Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("Http/1.1 302 OK \r\n");
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
            if (params==null) {
                response200Header(dos, body.length);
                responseBody(dos, body);
            }
            else{
                String[] info=params.split("&");
                String[] acc = new String[4];
                for(int i=0; i<info.length;i++){
                    String[] value = info[i].split("=");
                    acc[i]=value[1];
                }
                User user = new User(acc[0],acc[1],acc[2],acc[3]);
                response302Header(dos,body.length);
                responseBody(dos, body);
            }
        }
        else{
            response200Header(dos, body.length);
            responseBody(dos, body);
        }
    }
}