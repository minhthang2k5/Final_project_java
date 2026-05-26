package client.service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import common.net.MessageObject;
import common.net.MessageType;

public class GetInformationService {
  private final ObjectOutputStream out;
  public GetInformationService( ObjectOutputStream out) throws IOException {
    this.out = out;
  }
  public void sendGetInfoSingleConversation(int userID) throws IOException {
    MessageObject request = new MessageObject(MessageType.GET_LIST_SINGLE_USER_REQUEST);
    request.setUserId(userID);
    out.writeObject(request);
    out.flush();
  }

  public void sendGetInfoGroupConversation(int userID) throws IOException {
    MessageObject request = new MessageObject(MessageType.GET_LIST_GROUP_REQUEST);
    request.setUserId(userID);
    out.writeObject(request);
    out.flush();
  }


}
