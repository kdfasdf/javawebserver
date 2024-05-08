package controller;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequest;
import util.HttpResponse;

public class LoginController implements Controller{
    private static final Logger log = LoggerFactory.getLogger(CreateUserController.class);
    @Override
    public void service(HttpRequest request, HttpResponse response){
        User user = DataBase.findUserById(
                request.getParameter("userId")
        );
        if(user != null){
            if(user.login(request.getParameter("password"))){
                response.addHeader("Set-Cookie","logined=true");
                response.sendRedirect("/index.html");
            }else{
                response.sendRedirect("/user/login_failed.html");
            }
        }else{
            response.sendRedirect("/user/login_failed.html");
        }
    }
}
