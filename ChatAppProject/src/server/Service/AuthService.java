package server.Service;

import common.net.MessageObject;
import common.net.MessageType;
import server.dao.UserDao;

public class AuthService {
  private UserDao userDao = new UserDao();
  public MessageObject authenticate(MessageObject request) {
        String username = request.getUsername();
        String password = request.getPassword();

        // Gọi DAO để check DB
        boolean resCheck = userDao.validateLogin(username, password);

        MessageObject response = new MessageObject(MessageType.LOGIN_RESPONSE);
        
        if (resCheck == true) {
            System.out.println("Here true");
            response.setSuccess(true);
            response.setMessage("Login successful");
            response.setType(MessageType.LOGIN_RESPONSE);
            response.setSuccess(true);
        } else {
            System.out.println("Here false");
            response.setSuccess(false);
            response.setMessage("fail to login");
            response.setType(MessageType.LOGIN_RESPONSE);
            response.setSuccess(false);
        }
        
        return response;
    }
}
