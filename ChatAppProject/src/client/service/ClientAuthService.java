package client.service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import common.net.MessageObject;
import common.net.MessageType;

public class ClientAuthService {
  private final ObjectOutputStream out;
  private final ObjectInputStream in;
  public ClientAuthService(ObjectInputStream in, ObjectOutputStream out) throws IOException {
    this.out = out;
    this.in = in;
  }
  public void sendAuthentication(String username,String password) throws IOException {
    MessageObject msg = new MessageObject(MessageType.LOGIN_REQUEST);
    msg.setUsername(username);
    msg.setPassword(password);
    synchronized (out) {
      out.writeObject(msg);
      out.flush();
    }
  }
  public MessageObject receiveRespond() throws ClassNotFoundException, IOException {
    MessageObject response = (MessageObject) in.readObject();
    return response;
  }
  public void sendRegisterInformation(String username,String password,String confirmPassword, 
    String displayName,String email) throws IOException {
      MessageObject msg = new MessageObject(MessageType.REGISTER_REQUEST);
      msg.setUsername(username);
      msg.setPassword(password);
      msg.setConfirmPassword(confirmPassword);
      msg.setEmail(email);
      msg.setDisplayName(displayName);
      synchronized (out) {
        out.writeObject(msg);
        out.flush();
      }
      
  }

  public void sendCreateConversationRequest(int senderID,int receiverID) throws IOException {
    MessageObject request = new MessageObject(MessageType.CREATE_SINGLE_CONVERSATION_REQUEST);
    request.setSenderId(senderID);
    request.setReceiverId(receiverID);
    synchronized (out) {
      out.writeObject(request);
      out.flush();
    }
  }

}
