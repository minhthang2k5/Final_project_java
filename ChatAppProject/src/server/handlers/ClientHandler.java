package server.handlers;

import java.net.*;

import common.net.MessageObject;
import common.net.MessageType;
import server.Server;
import server.Service.AuthService;
import server.Service.ChatRoutingService;
import server.Service.GroupConversationService;
import server.Service.SingleConversationService;
import server.dao.ChatDao;
import server.dao.UserDao;

import java.io.*;

public class ClientHandler implements Runnable {
  private Socket socket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private String username;
  private int userId;
  private SingleConversationService singleConversationService = new SingleConversationService();
  private GroupConversationService groupConversationService = new GroupConversationService();
  public ClientHandler(Socket socket) {
    this.socket = socket;
  }
  public ObjectOutputStream getOutputStream() {
    return out;
  } 
  public ObjectInputStream getInputStream() {
    return in;
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
          if (response.isSuccess()) {
            //Send cho người đối diện
            singleConversationService.sendStatusOnline(userId);
            //Send cho bản thân mình 
            SingleConversationService singleConversationService = new SingleConversationService();
            MessageObject response2 = singleConversationService.sendSingleConversationInfo(userId);
            out.writeObject(response2);
            out.flush();
          }
        }
        if (request.getType() == MessageType.GET_LIST_SINGLE_USER_REQUEST) {
          SingleConversationService singleConversationService = new SingleConversationService();
          System.out.println(request.getUserId());
          MessageObject response = singleConversationService.sendSingleConversationInfo(request.getUserId());
          out.writeObject(response);
          out.flush();
        }
        if (request.getType() == MessageType.GET_LIST_GROUP_REQUEST) {
          MessageObject response = groupConversationService.sendGroupConversationInfo(request.getUserId());
          out.writeObject(response);
          out.flush();
        }
        if (request.getType() == MessageType.SEND_SINGLE_CHAT_MESSAGE_REQUEST) {
          ChatRoutingService chatRoutingService = new ChatRoutingService();
          MessageObject response = chatRoutingService.handleReceiveSingleChatMessage(request);
          //Gửi cho chính mình
          sendChatMessage(response);
          //Gửi cho người trong cuộc trò chuyện nếu online
          UserDao userDao = new UserDao();
          ChatDao chatDao = new ChatDao();
          int otherUserId = chatDao.getTheOtherUserInSingleConversation(request.getConversationId(), userId);
          if (otherUserId > 0) {
            String otherUsername = userDao.fetchUsername(otherUserId);
            if (otherUsername != null && Server.onlineUsers.containsKey(otherUsername)) {
              Server.onlineUsers.get(otherUsername).sendChatMessage(response);
            }
          }
        }
        if (request.getType() == MessageType.SEND_GROUP_CHAT_MESSAGE_REQUEST) {
          ChatRoutingService chatRoutingService = new ChatRoutingService();
          MessageObject response = chatRoutingService.handleReceiveGroupChatMessage(request);
          //Gửi cho chính mình
          sendChatMessage(response);
          //Gửi cho các thành viên trong group nếu online
          ChatDao chatDao = new ChatDao();
          String[] listUsername = chatDao.getListUserInGroupConversation(request.getConversationId());
          if (listUsername != null) {
            for (String name : listUsername) {
              if (name == null || name.trim().isEmpty()) {
                continue;
              }
              ClientHandler handler = Server.onlineUsers.get(name);
              if (handler != null && handler != this) {
                handler.sendChatMessage(response);
              }
            }
          }
        }
        if (request.getType() == MessageType.SIGNOUT_REQUEST) {
          if (this.username != null) {
            Server.onlineUsers.remove(this.username);
            System.out.println(this.username + " offline");
            singleConversationService.sendStatusOnline(this.userId);
          }
          this.username = null;
          this.userId = 0;
        }
        if (request.getType() == MessageType.CHECK_EXITS_USER_ID_REQUEST) {
          AuthService authService = new AuthService();
          MessageObject response = authService.checkExistsUserId(request);
          out.writeObject(response);
          out.flush();
        }
        if (request.getType() == MessageType.CREATE_GROUP_CONVERSATION_REQUEST) {
          MessageObject response = groupConversationService.handleCreateGroupConversation(request);
          out.writeObject(response);
          out.flush();
          if (response.isSuccess()) {
            ChatDao chatDao = new ChatDao();
            UserDao userDao = new UserDao();
            String[] listUsername = chatDao.getListUserInGroupConversation(response.getConversationId());
            if (listUsername != null) {
              for (String name : listUsername) {
                if (name == null || name.trim().isEmpty()) {
                  continue;
                }
                ClientHandler handler = Server.onlineUsers.get(name);
                if (handler != null) {
                  int targetUserId = userDao.fetchUserID(name);
                  if (targetUserId > 0) {
                    handler.sendGroupConversationInfo(targetUserId);
                  }
                }
              }
            }
          }
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
  
  public void sendStatusOnline() throws IOException {
    MessageObject response = singleConversationService.sendSingleConversationInfo(userId);
    out.writeObject(response);
    out.flush();
  }

  public void sendChatMessage(MessageObject response) throws IOException {
    out.writeObject(response);
    out.flush();
  }  

  public void sendGroupConversationInfo(int userId) throws IOException {
    MessageObject response = groupConversationService.sendGroupConversationInfo(userId);
    out.writeObject(response);
    out.flush();
  }
}
