package server.Service;

import java.util.ArrayList;

import common.models.SingleConversationInfo;
import common.net.MessageObject;
import common.net.MessageType;
import server.Server;
import server.dao.ChatDao;
import server.dao.UserDao;

public class SingleConversationService {
  private ChatDao chatDao = new ChatDao();
  private UserDao userDao = new UserDao();
  public MessageObject sendSingleConversationInfo(int userId) {
    ArrayList<SingleConversationInfo> listSingleConversation = null;
    listSingleConversation = chatDao.getListSingleConversationById(userId);
    for (SingleConversationInfo user : listSingleConversation) {
      String username = userDao.fetchUsername(user.getUserId());
      if (Server.onlineUsers.containsKey(username)) {
        user.setOnline(true);
      }
    }
    MessageObject response = new MessageObject(MessageType.SEND_SINGLE_CHAT_MESSAGE_REQUEST);
    response.setListSingleUser(listSingleConversation);
    return response;
  }
}
