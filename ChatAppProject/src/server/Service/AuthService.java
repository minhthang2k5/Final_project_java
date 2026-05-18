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
            response.setSuccess(true);
            response.setMessage("Login successful");
            response.setType(MessageType.LOGIN_RESPONSE);
            response.setSuccess(true);
        } else {
            response.setSuccess(false);
            response.setMessage("Username or Password is not correct");
            response.setType(MessageType.LOGIN_RESPONSE);
            response.setSuccess(false);
        }
        
        return response;
    }
    public MessageObject checkConditionForRegisterAndAdd(MessageObject request) {
        CheckRegister checkRegister = new CheckRegister(request,userDao);
        MessageObject response = checkRegister.checkRequest();
        if (response.isSuccess()) {
            if (checkRegister.writeNewUserIntoDatabase()) {
                System.out.println("Success to write new user into database");
                return response;
            }
            else {
                System.out.println("fail to write new user into database");
                response.setSuccess(false);
                response.setMessage("Fail from server");
                return response;
            }
        }
        else {
            return response;
        }
    }
    

}

class CheckRegister {
    private String username;
    private String password;
    private String confirmPassword;
    private String email;
    private String displayName;
    private UserDao userDao;
    public CheckRegister(MessageObject request,UserDao userDao) {
        username = request.getUsername();
        password = request.getPassword();
        confirmPassword = request.getConfirmPassword();
        email = request.getEmail();
        displayName = request.getDisplayName();
        this.userDao = userDao;
    }

    public MessageObject checkRequest() {
        MessageObject response = new MessageObject(MessageType.REGISTER_RESPONSE);
        if (username == null) {
            System.out.println("Username is required");
            response.setSuccess(false);
            response.setMessage("Username is required");
            return response;
        }
        if (userDao.checkExistsUsername(username)) {
            System.out.println("Check username already exists");
            response.setSuccess(false);
            response.setMessage("Username already exists");
            return response;
        }
        if (!checkPassword().equals("successful")) {
            System.out.println("Fail to check password");
            response.setSuccess(false);
            response.setMessage(checkPassword());
            return response;
        }
        if (!password.equals(confirmPassword)) {
            System.out.println("Password and confirm password don't match together");
            response.setSuccess(false);
            response.setMessage("Password and confirm password don't match together");
            return response;
        }
        if (!checkEmail().equals("successful")) {
            System.out.println("Fail to check email");
            response.setSuccess(false);
            response.setMessage(checkEmail());
            return response;
        }
        if (displayName == null) {
            System.out.println("Display name is required");
            response.setSuccess(false);
            response.setMessage("Display name is required");
            return response;
        }
        response.setSuccess(true);
        response.setMessage("Register successfully");
        System.out.println("Register successfully");
        return response;
    }
    public boolean writeNewUserIntoDatabase() {
        return userDao.writeNewUser(username, password, displayName, email);
    }
    String checkPassword() {
        if (password == null || password.length() < 6) {
            return "Password must be at least 6 characters long";
        }

        boolean hasLetter = false;
        boolean hasDigit = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }

            if (hasLetter && hasDigit) {
                break;
            }
        }

        if (!hasLetter || !hasDigit) {
            return "Password must include at least one letter and one number";
        }

        return "successful";
    }
    String checkEmail() {
        if (email == null || email.trim().isEmpty()) {
            return "Email is required";
        }

        if (!email.contains("@") && !email.contains(".")) {
            return "Email must contain @ or .";
        }

        return "successful";
    }
}
