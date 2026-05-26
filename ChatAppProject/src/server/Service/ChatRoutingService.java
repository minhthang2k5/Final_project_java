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



  public MessageObject handleReceiveSingleChatMessage(MessageObject request) {
    int senderID = request.getSenderId();
    int conversationID = request.getConversationId();
    String chatMSG = request.getChatMsg();
    int messageID = chatDao.writeMessage(senderID, conversationID, chatMSG);
    MessageObject response = new MessageObject(MessageType.SEND_SINGLE_CHAT_MESSAGE_RESPONSE);
    response.setChatMsg(chatMSG);
    response.setSenderId(senderID);
    response.setConversationId(conversationID);
    response.setMessageId(messageID);
    return response;
  }

  public MessageObject handleReceiveGroupChatMessage(MessageObject request) {
    int senderID = request.getSenderId();
    int conversationID = request.getConversationId();
    String chatMSG = request.getChatMsg();
    int messageID = chatDao.writeMessage(senderID, conversationID, chatMSG);
    MessageObject response = new MessageObject(MessageType.SEND_GROUP_CHAT_MESSAGE_RESPONSE);
    response.setChatMsg(chatMSG);
    response.setSenderId(senderID);
    response.setConversationId(conversationID);
    response.setMessageId(messageID);
    String displayName = userDao.fetchDisplayName(senderID);
    if (displayName == null || displayName.trim().isEmpty()) {
      displayName = userDao.fetchUsername(senderID);
    }
    response.setDisplayName(displayName);
    return response;
  }

  public MessageObject handleGetHistoryChat(MessageObject request) {
    int conversationID = request.getConversationId();
    MessageObject response = new MessageObject(MessageType.GET_HISTORY_CHAT_RESPONSE);
    response.setConversationId(conversationID);
    response.setListMessage(chatDao.getListMessageInConversation(conversationID));
    return response;
  }

  
}
