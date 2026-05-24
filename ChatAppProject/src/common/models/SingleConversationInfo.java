package common.models;

public class SingleConversationInfo {
  private final int conversationId;
  private final String displayName;
  private final int userId;
  private boolean isOnline = false;

  public SingleConversationInfo(int conversationId, String displayName, int userId) {
    this.conversationId = conversationId;
    this.displayName = displayName;
    this.userId = userId;
  }

  public int getConversationId() {
    return conversationId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public int getUserId() {
    return userId;
  }

  public boolean isOnline() {
    return this.isOnline;
  }
  public void setOnline(boolean isOnline) {
    this.isOnline = isOnline;
  }
}
