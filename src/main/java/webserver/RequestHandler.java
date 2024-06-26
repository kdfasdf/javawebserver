package webserver;

import controller.Controller;
import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.*;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

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
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);
            if (request.getCookies().getCookie(HttpSessions.SESSION_ID_NAME) == null) {
                response.addHeader("Set-Cookie", HttpSessions.SESSION_ID_NAME + "=" + UUID.randomUUID());//"JSESSIONID"
            }
            Controller controller = RequestMapping.getController(request.getPath());
            if (controller ==null){
                String path = getDefaultPath(request.getPath());
                response.forward(path);
            } else{
                controller.service(request,response);
            }
        } catch(Exception e){
            log.debug("File not exits");
            e.printStackTrace();

        }
    }
    private String getDefaultPath(String path){
        if (path.equals("/")){
            return "/index.html";
        }
        return path;
    }
}