package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import client.gui.ChatPanel;
import client.gui.AddGroupDialog;
import client.gui.DashBoardPanel;
import client.gui.LoginPanel;
import client.gui.MainFrame;
import client.gui.RegisterPanel;
import client.service.ClientAuthService;
import client.service.GetInformationService;
import common.models.GroupConversationInfo;
import common.models.SingleConversationInfo;
import common.net.MessageObject;
import common.net.MessageType;

public class ServerHandler extends SwingWorker<Void, MessageObject> {
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private MainFrame mainFrame;
  private ClientAuthService authService;
  private LoginPanel pendingLoginPanel;
  private RegisterPanel pendingRegisterPanel;
  private DashBoardPanel pendingDashBoardPanel;

  public ObjectOutputStream getOutputStream() {
    return out;
  } 
  public ObjectInputStream getInputStream() {
    return in;
  }
  public ServerHandler(ObjectInputStream in, ObjectOutputStream out, MainFrame mainFrame,
      ClientAuthService authService) throws IOException {
    this.out = out;
    this.in = in;
    this.mainFrame = mainFrame;
    this.authService = authService;
    this.pendingDashBoardPanel = mainFrame.getDashBoardPanel();
  }

  public void requestLogin(String username, String password, LoginPanel loginPanel) throws IOException {
    this.pendingLoginPanel = loginPanel;
    authService.sendAuthentication(username, password);
  }

  public void requestRegister(String username, String password, String confirmPassword,
      String displayName, String email, RegisterPanel registerPanel) throws IOException {
    this.pendingRegisterPanel = registerPanel;
    authService.sendRegisterInformation(username, password, confirmPassword, displayName, email);
  }

  public void requestSendMessage(String msg,int conservationID) throws IOException {
    MessageObject request = new MessageObject(MessageType.SEND_SINGLE_CHAT_MESSAGE_REQUEST);
		request.setChatMsg(msg);
		request.setSenderId(mainFrame.getCurrentUser().getUserId());
    request.setConversationId(conservationID);
    out.writeObject(request);
    out.flush();
  }

  public void requestCreateConversation(String receiverID) throws NumberFormatException, IOException {
    authService.sendCreateConversationRequest(mainFrame.getCurrentUser().getUserId(), Integer.parseInt(receiverID));
  }

  public void requestCheckUserId(int userId) throws IOException {
    MessageObject request = new MessageObject(MessageType.CHECK_EXITS_USER_ID_REQUEST);
    request.setUserId(userId);
    out.writeObject(request);
    out.flush();
  }

  public void requestSignOut() throws IOException {
    String username = mainFrame.getCurrentUser().getUsername();
    int userId = mainFrame.getCurrentUser().getUserId();
    if (username == null || username.trim().isEmpty() || userId <= 0) {
      mainFrame.showLoginPanel();
      return;
    }

    MessageObject request = new MessageObject(MessageType.SIGNOUT_REQUEST);
    request.setUsername(username);
    request.setUserId(userId);
    out.writeObject(request);
    out.flush();

    mainFrame.getCurrentUser().setUsername(null);
    mainFrame.getCurrentUser().setUserId(0);
    mainFrame.getDashBoardPanel().resetForSignOut();
    mainFrame.showLoginPanel();
  }
  @Override
  protected Void doInBackground() throws Exception {
    while (!isCancelled()) {
      MessageObject respond = (MessageObject) in.readObject();
      if (respond == null) break;
      publish(respond);
    }
    return null;
  }
  @Override
  protected void process(List<MessageObject> chunks) {
    for (MessageObject respond : chunks) {
      if (respond.getType() == MessageType.LOGIN_RESPONSE && pendingLoginPanel != null) {
        if (respond.isSuccess()) {
          pendingLoginPanel.clearInput();
          mainFrame.showDashBoardPanel();
          mainFrame.getDashBoardPanel().setUserNameText(respond.getUsername());
          mainFrame.getDashBoardPanel().setUserIdText("#" + String.valueOf(respond.getUserId()));
          mainFrame.getCurrentUser().setUserId(respond.getUserId());
          mainFrame.getCurrentUser().setUsername(respond.getUsername());
          try {
            GetInformationService getInformationService = new GetInformationService(out);
            getInformationService.sendGetInfoSingleConversation(mainFrame.getCurrentUser().getUserId());
            getInformationService.sendGetInfoGroupConversation(mainFrame.getCurrentUser().getUserId());
          } catch (IOException e) {
            e.printStackTrace();
          }
          
        } else if (respond != null) {
          System.out.println("Fail to login");
          pendingLoginPanel.setStatusMessage(respond.getMessage(), true);
        } else {
          pendingLoginPanel.setStatusMessage("Login failed", true);
        }
        pendingLoginPanel.getSignInButton().setEnabled(true);
        pendingLoginPanel = null;
        continue;
      }
      if (respond.getType() == MessageType.REGISTER_RESPONSE && pendingRegisterPanel != null) {
        if (respond.isSuccess()) {
          System.out.println("Successfully register");
          pendingRegisterPanel.setStatusMessage("Successful");
        } else if (respond != null) {
          System.out.println("Fail to sign up");
          pendingRegisterPanel.setStatusMessage(respond.getMessage());
        } else {
          pendingRegisterPanel.setStatusMessage("Register failed");
        }
        pendingRegisterPanel.getSignUpButton().setEnabled(true);
        pendingRegisterPanel = null;
        continue;
      }
      if (respond.getType() == MessageType.GET_LIST_SINGLE_USER_RESPOND) {
        ArrayList<SingleConversationInfo> list = respond.getListSingleUser();
        DashBoardPanel dashBoardPanel = mainFrame.getDashBoardPanel();
        DefaultListModel<SingleConversationInfo> model = dashBoardPanel.getSingleListModel();
        model.clear();
        for (SingleConversationInfo info : list) {
            model.addElement(info);
            //pendingDashBoardPanel.createChatPanel(info);
        }
      }
      if (respond.getType() == MessageType.GET_LIST_GROUP_RESPOND) {
        ArrayList<GroupConversationInfo> list = respond.getListGroup();
        DashBoardPanel dashBoardPanel = mainFrame.getDashBoardPanel();
        DefaultListModel<GroupConversationInfo> model = dashBoardPanel.getGroupListModel();
        model.clear();
        if (list != null) {
          for (GroupConversationInfo info : list) {
            model.addElement(info);
          }
        }
      }
      if (respond.getType() == MessageType.CREATE_SINGLE_CONVERSATION_RESPONSE) {
        if (respond.isSuccess()) {
          JOptionPane.showMessageDialog(mainFrame, "Create conversation successful", "Success",
              JOptionPane.INFORMATION_MESSAGE);
        } else {
          String message = respond.getMessage() == null ? "Create conversation failed" : respond.getMessage();
          JOptionPane.showMessageDialog(mainFrame, message, "Failed", JOptionPane.ERROR_MESSAGE);
        }
      }
      if (respond.getType() == MessageType.SEND_SINGLE_CHAT_MESSAGE_RESPONSE) {
        int conversationId = respond.getConversationId();
        DashBoardPanel dashBoardPanel = mainFrame.getDashBoardPanel();
        ChatPanel chatPanel = dashBoardPanel.getChatPanel(conversationId);

        SingleConversationInfo conversationInfo = null;
        DefaultListModel<SingleConversationInfo> model = dashBoardPanel.getSingleListModel();
        for (int i = 0; i < model.size(); i++) {
          SingleConversationInfo info = model.get(i);
          if (info.getConversationId() == conversationId) {
            conversationInfo = info;
            break;
          }
        }

        // if (chatPanel == null && conversationInfo != null) {
        //   chatPanel = dashBoardPanel.createChatPanel(conversationInfo);
        // }

        if (chatPanel != null) {
          boolean isSelf = respond.getSenderId() == mainFrame.getCurrentUser().getUserId();
          String displayName = isSelf ? "You" : (conversationInfo != null ? conversationInfo.getDisplayName() : null);
          chatPanel.appendMessageBubble(respond.getMessageId(), displayName, respond.getChatMsg(), isSelf);
        }
      }

      if (respond.getType() == MessageType.CHECK_EXITS_USER_ID_RESPOND) {
        DashBoardPanel dashBoardPanel = mainFrame.getDashBoardPanel();
        AddGroupDialog dialog = dashBoardPanel.getAddGroupDialog();
        if (dialog != null) {
          dialog.handleCheckUserIdResult(respond.getUserId(), respond.isSuccess());
        }
      }
    
    
    }
  }
} 