package controller;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequest;
import util.HttpResponse;
import util.HttpSession;

public class LoginController extends AbstractController{
    private static final Logger log = LoggerFactory.getLogger(CreateUserController.class);
    @Override
    public void doPost(HttpRequest request, HttpResponse response){
        User user = DataBase.findUserById(
                request.getParameter("userId")
        );
        if(user != null){
            if(user.login(request.getParameter("password"))){
                HttpSession session = request.getSession();
                session.setAttribute("user",user);
                log.debug("login success");
                response.sendRedirect("/index.html");
            }else{
                response.sendRedirect("/user/login_failed.html");
            }
        }else{
            response.sendRedirect("/user/login_failed.html");
        }
    }
}
