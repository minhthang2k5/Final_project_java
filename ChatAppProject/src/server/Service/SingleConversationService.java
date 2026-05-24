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
    ArrayList<SingleConversationInfo> listSingleConversation = chatDao.getListSingleConversationById(userId);
    for (SingleConversationInfo user : listSingleConversation) {
      String username = userDao.fetchUsername(user.getUserId());
      if (username != null && Server.onlineUsers.containsKey(username)) {
        user.setOnline(true);
      }
    }
    MessageObject response = new MessageObject(MessageType.GET_LIST_SINGLE_USER_RESPOND);
    response.setListSingleUser(listSingleConversation);
    return response;
  }
}
