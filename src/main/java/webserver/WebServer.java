package webserver;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {
    private static final Logger log = LoggerFactory.getLogger(WebServer.class);
    public static final int DEFAULT_PORT=8080;
    public static void main(String args[]) throws Exception{
        int port =0;
        if(args==null||args.length==0){
            port=DEFAULT_PORT;
        }
        else{
            port=Integer.parseInt(args[0]);
        }
        try(ServerSocket listenSocket = new ServerSocket(port)){
            log.info("Web Application Server started {} port",port);
            Socket connection;

            while((connection = listenSocket.accept())!=null){
                RequestHandler requestHandler=new RequestHandler(connection);
                requestHandler.start();
            }
        }

    }
}

