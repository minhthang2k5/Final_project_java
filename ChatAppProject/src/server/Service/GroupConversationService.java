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
  public MessageObject handleCreateGroupConversation(MessageObject request) {
    ArrayList<Integer> listUserID = request.getListUserID();
    String nameGroup = request.getNameGroup();
    int conversationId = chatDao.createGroupConversation(listUserID, nameGroup);
    MessageObject response = new MessageObject(MessageType.CREATE_GROUP_CONVERSATION_RESPOND);
    if (conversationId > 0) {
      response.setSuccess(true);
      response.setConversationId(conversationId);
    }
    else {
      response.setSuccess(false);
      response.setMessage("Fail from server");
    }
    return response;
  }

  

}
