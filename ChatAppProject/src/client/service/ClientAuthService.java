package client.service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import common.net.MessageObject;
import common.net.MessageType;

public class ClientAuthService {
  private final ObjectOutputStream out;
  private final ObjectInputStream in;
  public ClientAuthService(Socket socket) throws IOException {
    this.out = new ObjectOutputStream(socket.getOutputStream());
    this.in = new ObjectInputStream(socket.getInputStream());
  }
  public void sendAuthentication(String username,String password) throws IOException {
    MessageObject msg = new MessageObject(MessageType.LOGIN_REQUEST);
    msg.setUsername(username);
    msg.setPassword(password);
    out.writeObject(msg);
    out.flush();
  }
  public MessageObject receiveRespondFromLogin() throws ClassNotFoundException, IOException {
    MessageObject response = (MessageObject) in.readObject();
    return response;
  }

}
