package server.handlers;

import java.net.*;

import common.net.MessageObject;
import common.net.MessageType;
import server.Server;
import server.Service.AuthService;
import server.Service.ChatRoutingService;
import server.Service.SingleConversationService;

import java.io.*;

public class ClientHandler implements Runnable {
  private Socket socket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private String username;
  private int userId;
  private  SingleConversationService singleConversationService = new SingleConversationService();
  public ClientHandler(Socket socket) {
    this.socket = socket;
  }
  public ObjectOutputStream getOutputStream() {
    return out;
  } 
  public ObjectInputStream getInputStream() {
    return in;
  }

  public void sendStatusOnline() throws IOException {
    MessageObject response = singleConversationService.sendSingleConversationInfo(userId);
    out.writeObject(response);
    out.flush();
  }

  @Override
  public void run() {
    try {
      out = new ObjectOutputStream(socket.getOutputStream());
      out.flush();  // Quan trọng: gửi stream header
      in = new ObjectInputStream(socket.getInputStream());

      while (true) {
        MessageObject request = (MessageObject) in.readObject();
        if (request == null) break;
        if (request.getType() == MessageType.LOGIN_REQUEST) {
          AuthService aService = new AuthService();
          MessageObject response = aService.authenticate(request);
          //System.out.println(request.getUsername()); //test
          out.writeObject(response);
          if (response.isSuccess()) {
            this.username = request.getUsername();
            this.userId = response.getUserId();
            //Add user
            Server.onlineUsers.put(username,this);
            System.out.println(username + " online");
            singleConversationService.sendStatusOnline(userId);
          }
        }
        if (request.getType() == MessageType.REGISTER_REQUEST) {
          AuthService aService = new AuthService();
          MessageObject response = aService.checkConditionForRegisterAndAdd(request);
          //System.out.println("Register from port: " + socket.getPort()); //test
          out.writeObject(response);
          out.flush();
        }
        if (request.getType() == MessageType.CREATE_SINGLE_CONVERSATION_REQUEST) {
          ChatRoutingService chatRoutingService = new ChatRoutingService();
          MessageObject response = chatRoutingService.createConversation(request);
          out.writeObject(response);
          out.flush();
        }
        if (request.getType() == MessageType.SEND_SINGLE_CHAT_MESSAGE_REQUEST) {
          ChatRoutingService chatRoutingService = new ChatRoutingService();
          chatRoutingService.handleReceiveAndSendSingleChatMessage(request);
        }
        if (request.getType() == MessageType.GET_LIST_SINGLE_USER_REQUEST) {
          SingleConversationService singleConversationService = new SingleConversationService();
          System.out.println(request.getUserId());
          MessageObject response = singleConversationService.sendSingleConversationInfo(request.getUserId());
          out.writeObject(response);
          out.flush();
        }
        
      }

    } catch (java.io.EOFException | java.net.SocketException e) {
      // client disconnected normally
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } finally {

      if (username != null) {
            Server.onlineUsers.remove(username);
            System.out.println(username + " offline");
            singleConversationService.sendStatusOnline(userId);
      }
      try {
        if (in != null) in.close();
      } catch (IOException e) {}
      try {
        if (out != null) out.close();
      } catch (IOException e) {}
      try {
        if (socket != null && !socket.isClosed()) socket.close();
      } catch (IOException e) {}
      
    }
  }
  
}
