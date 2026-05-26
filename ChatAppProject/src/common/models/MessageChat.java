package common.models;

import java.io.Serializable;

public class MessageChat implements Serializable {
  private int messageID;
  private int senderID;
  private String type;
  private String content;
  private String filePath;
  private String senderDisplayName;

  public MessageChat(int messageID, int senderID, String content) {
    this.messageID = messageID;
    this.senderID = senderID;
    this.type = "text";
    this.content = content;
    this.filePath = null;
  }

  public MessageChat(int messageID, int senderID, String content, String filePath) {
    this.messageID = messageID;
    this.senderID = senderID;
    this.type = "file";
    this.content = content;
    this.filePath = filePath;
  }

  public int getMessageID() {
    return messageID;
  }

  public void setMessageID(int messageID) {
    this.messageID = messageID;
  }

  public int getSenderID() {
    return senderID;
  }

  public void setSenderID(int senderID) {
    this.senderID = senderID;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public String getSenderDisplayName() {
    return senderDisplayName;
  }

  public void setSenderDisplayName(String senderDisplayName) {
    this.senderDisplayName = senderDisplayName;
  }
  
}
