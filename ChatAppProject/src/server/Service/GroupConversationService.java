package server.Service;

import java.util.ArrayList;

import common.models.GroupConversationInfo;
import common.net.MessageObject;
import common.net.MessageType;
import server.dao.ChatDao;

public class GroupConversationService {
  private ChatDao chatDao = new ChatDao();

  public MessageObject sendGroupConversationInfo(int userId) {
    ArrayList<GroupConversationInfo> listGroupConversation = chatDao.getListGroupConversationByUserId(userId);
    MessageObject response = new MessageObject(MessageType.GET_LIST_GROUP_RESPOND);
    response.setListGroup(listGroupConversation);
    return response;
  }
}
