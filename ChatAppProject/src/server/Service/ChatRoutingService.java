package server.Service;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import common.net.MessageObject;
import common.net.MessageType;
import server.Server;
import server.dao.ChatDao;
import server.dao.UserDao;
import server.handlers.ClientHandler;

public class ChatRoutingService {
  private ChatDao chatDao = new ChatDao();
  private UserDao userDao = new UserDao();
  public MessageObject createConversation(MessageObject request) {
    MessageObject response = new MessageObject(MessageType.CREATE_SINGLE_CONVERSATION_RESPONSE);
    StringBuilder messageRes = new StringBuilder();
    boolean isSuccessful;
    int senderId = request.getSenderId();
    int receiverId = request.getReceiverId();
    isSuccessful = chatDao.createConversationSingle(senderId, receiverId, messageRes);
    if (!isSuccessful) {
      response.setMessage(messageRes.toString());
      response.setSuccess(false);
      System.out.println("From create conversation: " + messageRes);
    }
    else {
      response.setMessage("Successful");
      response.setSuccess(true);
      System.out.println("From create conversation: Successful");
    }

    return response;
  }

  public void handleReceiveAndSendSingleChatMessage(MessageObject request) {
    System.out.println("Here2");
    int senderId = request.getSenderId();
    int conversationID = request.getConversationId();
    String senderUsername = userDao.fetchUsername(senderId);
    String chatMsg = request.getChatMsg();
    MessageObject response = new MessageObject(MessageType.SEND_SINGLE_CHAT_MESSAGE_RESPONSE);
    response.setChatMsg(chatMsg);
    response.setConversationId(conversationID);
    //Gửi tin nhắn
    String[] listUsernameInConversation = chatDao.getUsernameInConversation(conversationID);
    String receiverUsername = null;
    if (senderUsername.equals(listUsernameInConversation[0])) {
      receiverUsername = listUsernameInConversation[1];
    }
    else {
      receiverUsername = listUsernameInConversation[0];
    }
    ClientHandler clientHandler = Server.onlineUsers.get(receiverUsername);
    if (clientHandler != null) {
      try {
      clientHandler.getOutputStream().writeObject(response);
      clientHandler.getOutputStream().flush();
  


      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    //Lưu database
    chatDao.writeMessage(senderId, conversationID, chatMsg);
  }

}
