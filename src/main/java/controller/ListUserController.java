package controller;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequest;
import util.HttpRequestUtils;
import util.HttpResponse;

import java.util.Collection;
import java.util.Map;

public class ListUserController extends AbstractController{
    private static final Logger log = LoggerFactory.getLogger(CreateUserController.class);
    @Override
    public void doGet(HttpRequest request, HttpResponse response){
        if(!isLogin(request.getHeader("Cookie"))){
            response.sendRedirect("/user/login.html");
            return;
        }
        Collection<User> users = DataBase.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("<table border='1'>");
        for(User user: users){
            sb.append("<tr>");
            sb.append("<td>"+user.getUserId()+"</td>");
            sb.append("<td>"+user.getName()+"</td>");
            sb.append("<td>"+user.getEmail()+"</td>");
            sb.append("</tr>");
        }
        response.forwardBody(sb.toString());
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
