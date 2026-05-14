package common.net;

import java.io.Serializable;

public class MessageObject implements Serializable {

  private static final long serialVersionUID = 1L; // Đảm bảo ID này đồng bộ giữa Client và Server để không bị lỗi đường truyền
  private MessageType type;      //Loại thông điệp (Lấy từ Enum ở trên)
  private String username;
  private String password;
  private boolean success;
  private String message;
  
  
  
  
  public MessageObject(MessageType type) {
        this.type = type;
  }

  
  public MessageType getType() {
    return type; 
  }
  public void setType(MessageType type) { 
    this.type = type; 
  }

  public String getUsername() {
    return username; 
    }
  public void setUsername(String username) {
    this.username = username; 
  }

  public String getPassword() { 
    return password; 
  }
  public void setPassword(String password) { 
    this.password = password; 
  }

  public boolean isSuccess() { 
    return success; 
  }
  public void setSuccess(boolean success) {
    this.success = success; 
  }

  public String getMessage() { 
    return message; 
  }
  public void setMessage(String message) { 
    this.message = message; 
  }

}
