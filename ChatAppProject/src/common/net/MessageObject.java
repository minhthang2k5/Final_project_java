package common.net;

import java.io.Serializable;
import java.util.ArrayList;

import common.models.GroupConversationInfo;
import common.models.MessageChat;
import common.models.SingleConversationInfo;

public class MessageObject implements Serializable {

  private static final long serialVersionUID = 1L; // Đảm bảo ID này đồng bộ giữa Client và Server để không bị lỗi đường truyền
  private MessageType type;      //Loại thông điệp (Lấy từ Enum ở trên)
  private String username;
  private String password;
  private int userId;
  private String confirmPassword;
  private String email;
  private String displayName;
  private boolean success;
  private String message;
  private int senderId;
  private int receiverId;
  private int conversationId;
  private String chatMsg;
  private ArrayList<SingleConversationInfo> listSingleUser;
  private ArrayList<GroupConversationInfo> listGroup;
  private int messageID;
  private ArrayList<Integer> listUserID;
  private String nameGroup;
  private ArrayList<MessageChat> listMessage;
  
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

  public String getConfirmPassword() {
    return confirmPassword;
  }
  public void setConfirmPassword(String confirmPassword) {
    this.confirmPassword = confirmPassword;
  }

  public String getEmail() {
    return email;
  }
  public void setEmail(String email) {
    this.email = email;
  }

  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
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
  public int getSenderId() {
    return this.senderId;
  }
  public void setSenderId(int senderId) {
    this.senderId = senderId;
  }
  public int getReceiverId() {
    return this.receiverId;
  }
  public void setReceiverId(int receiverId) {
    this.receiverId = receiverId;
  }
  public int getUserId() {
    return userId;
  }
  public void setUserId(int userId) {
    this.userId = userId;
  }
  public void setConversationId(int conversationId) {
    this.conversationId = conversationId;
  }
  public int getConversationId() {
    return this.conversationId;
  }
  public void setChatMsg(String chatMsg) {
    this.chatMsg = chatMsg;
  }
  public String getChatMsg() {
    return this.chatMsg;
  }

  public void setListSingleUser(ArrayList<SingleConversationInfo> listSingleUser) {
    this.listSingleUser = listSingleUser;
  }
  public ArrayList<SingleConversationInfo> getListSingleUser() {
    return this.listSingleUser;
  }

  public void setListGroup(ArrayList<GroupConversationInfo> listGroup) {
    this.listGroup = listGroup;
  }

  public ArrayList<GroupConversationInfo> getListGroup() {
    return this.listGroup;
  }


  public void setMessageId(int messageID) {
    this.messageID = messageID;
  }

  public int getMessageId() {
    return messageID;
  }

  public ArrayList<Integer> getListUserID() {
    return listUserID;
  }

  public void setListUserID(ArrayList<Integer> listUserID) {
    this.listUserID = listUserID;
  }
  
  public void setNameGroup(String nameGroup) {
    this.nameGroup = nameGroup;
  }
  public String getNameGroup() {
    return this.nameGroup;
  }


  public void setListMessage(ArrayList<MessageChat> listMessage) {
    this.listMessage = listMessage;
  }
  public ArrayList<MessageChat> getListMessage() {
    return this.listMessage;
  }
} 
