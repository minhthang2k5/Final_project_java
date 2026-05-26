package common.models;

import java.io.Serializable;

public class GroupConversationInfo implements Serializable {
  private static final long serialVersionUID = 1L;
  private final int conversationId;
  private final String groupName;
  private final int memberCount;

  public GroupConversationInfo(int conversationId, String groupName, int memberCount) {
    this.conversationId = conversationId;
    this.groupName = groupName;
    this.memberCount = memberCount;
  }

  public int getConversationId() {
    return conversationId;
  }

  public String getGroupName() {
    return groupName;
  }

  public int getMemberCount() {
    return memberCount;
  }
}
