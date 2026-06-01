package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
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
import common.models.MessageChat;
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
  private final Map<Integer, UploadTask> pendingUploads = new HashMap<>();
  private final Map<Integer, UploadTask> activeUploads = new HashMap<>();
  private final Map<String, DownloadTask> activeDownloads = new HashMap<>();

  public interface DownloadListener {
    void onProgress(int percent);

    void onCompleted();

    void onError(String message);
  }

  private static class DownloadTask {
    private final File saveFile;
    private final String serverFileName;
    private final DownloadListener listener;
    private FileOutputStream fos;
    private long receivedBytes = 0;

    private DownloadTask(File saveFile, String serverFileName, DownloadListener listener) {
      this.saveFile = saveFile;
      this.serverFileName = serverFileName;
      this.listener = listener;
    }

    private void notifyProgress(int percent) {
      if (listener == null) {
        return;
      }
      SwingUtilities.invokeLater(() -> listener.onProgress(percent));
    }

    private void notifyCompleted() {
      if (listener == null) {
        return;
      }
      SwingUtilities.invokeLater(listener::onCompleted);
    }

    private void notifyError(String message) {
      if (listener == null) {
        return;
      }
      SwingUtilities.invokeLater(() -> listener.onError(message));
    }
  }

  public interface UploadListener {
    void onProgress(int percent);

    void onCompleted();

    void onError(String message);
  }

  private static class UploadTask {
    private final File file;
    private final int conversationId;
    private final boolean isGroupConversation;
    private final UploadListener listener;
    private final int senderId;
    private int messageId;
    private String serverFileName;

    private UploadTask(File file, int conversationId, boolean isGroupConversation, int senderId,
        UploadListener listener) {
      this.file = file;
      this.conversationId = conversationId;
      this.isGroupConversation = isGroupConversation;
      this.senderId = senderId;
      this.listener = listener;
    }

    private void notifyProgress(int percent) {
      if (listener == null) {
        return;
      }
      SwingUtilities.invokeLater(() -> listener.onProgress(percent));
    }

    private void notifyCompleted() {
      if (listener == null) {
        return;
      }
      SwingUtilities.invokeLater(listener::onCompleted);
    }

    private void notifyError(String message) {
      if (listener == null) {
        return;
      }
      SwingUtilities.invokeLater(() -> listener.onError(message));
    }
  }

  public ObjectOutputStream getOutputStream() {
    return out;
  }

  public ObjectInputStream getInputStream() {
    return in;
  }

  public int getCurrentUserId() {
    return mainFrame.getCurrentUser().getUserId();
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

  public void requestSendMessage(String msg, int conservationID) throws IOException {
    MessageObject request = new MessageObject(MessageType.SEND_SINGLE_CHAT_MESSAGE_REQUEST);
    request.setChatMsg(msg);
    request.setSenderId(mainFrame.getCurrentUser().getUserId());
    request.setConversationId(conservationID);
    writeMessage(request);
  }

  public void requestSendGroupMessage(String msg, int conversationId) throws IOException {
    MessageObject request = new MessageObject(MessageType.SEND_GROUP_CHAT_MESSAGE_REQUEST);
    request.setChatMsg(msg);
    request.setSenderId(mainFrame.getCurrentUser().getUserId());
    request.setConversationId(conversationId);
    writeMessage(request);
  }

  public void requestGetHistoryChat(int conversationId) throws IOException {
    MessageObject request = new MessageObject(MessageType.GET_HISTORY_CHAT_REQUEST);
    request.setConversationId(conversationId);
    writeMessage(request);
  }

  public void requestCreateConversation(String receiverID) throws NumberFormatException, IOException {
    authService.sendCreateConversationRequest(mainFrame.getCurrentUser().getUserId(), Integer.parseInt(receiverID));
  }

  public void requestCheckUserId(int userId) throws IOException {
    MessageObject request = new MessageObject(MessageType.CHECK_EXITS_USER_ID_REQUEST);
    request.setUserId(userId);
    writeMessage(request);
  }

  public void requestCreateGroupConversation(ArrayList<Integer> listUserID, String nameGroup) throws IOException {
    MessageObject request = new MessageObject(MessageType.CREATE_GROUP_CONVERSATION_REQUEST);
    request.setListUserID(listUserID);
    request.setNameGroup(nameGroup);
    writeMessage(request);
  }

  public void requestOnlineUsers() throws IOException {
    MessageObject request = new MessageObject(MessageType.GET_ONLINE_USERS_REQUEST);
    writeMessage(request);
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
    writeMessage(request);

    mainFrame.getCurrentUser().setUsername(null);
    mainFrame.getCurrentUser().setUserId(0);
    mainFrame.getDashBoardPanel().resetForSignOut();
    mainFrame.showLoginPanel();
  }

  public void requestUploadFile(File file, int conversationId, boolean isGroupConversation,
      UploadListener listener) throws IOException {
    if (file == null || !file.exists() || !file.isFile()) {
      if (listener != null) {
        listener.onError("Invalid file");
      }
      return;
    }
    if (conversationId <= 0) {
      if (listener != null) {
        listener.onError("Invalid conversation");
      }
      return;
    }
    if (pendingUploads.containsKey(conversationId) || hasActiveUpload(conversationId)) {
      if (listener != null) {
        listener.onError("Upload in progress");
      }
      return;
    }

    int senderId = mainFrame.getCurrentUser().getUserId();
    UploadTask task = new UploadTask(file, conversationId, isGroupConversation, senderId, listener);
    pendingUploads.put(conversationId, task);

    MessageObject request = new MessageObject(MessageType.UPLOAD_FILE_START_REQUEST);
    request.setConversationId(conversationId);
    request.setSenderId(senderId);
    request.setNameFile(file.getName());
    long length = file.length();
    int safeSize = length > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) length;
    request.setTotalSize(safeSize);
    writeMessage(request);
  }

  public void requestDownloadFile(int messageId, String serverFileName, File saveFile, DownloadListener listener) throws IOException {
    if (serverFileName == null || serverFileName.trim().isEmpty() || saveFile == null) {
      if (listener != null) {
        listener.onError("Invalid file parameters");
      }
      return;
    }
    if (activeDownloads.containsKey(serverFileName)) {
      if (listener != null) {
        listener.onError("Download in progress");
      }
      return;
    }
    DownloadTask task = new DownloadTask(saveFile, serverFileName, listener);
    activeDownloads.put(serverFileName, task);

    MessageObject request = new MessageObject(MessageType.DOWNLOAD_FILE_REQUEST);
    request.setMessageId(messageId);
    request.setNameFile(serverFileName);
    writeMessage(request);
  }

  public void requestDeleteMessage(int messageId, int conversationId) throws IOException {
    MessageObject request = new MessageObject(MessageType.DELETE_MESSAGE_REQUEST);
    request.setMessageId(messageId);
    request.setConversationId(conversationId);
    writeMessage(request);
  }

  private boolean hasActiveUpload(int conversationId) {
    for (UploadTask task : activeUploads.values()) {
      if (task.conversationId == conversationId) {
        return true;
      }
    }
    return false;
  }

  private void handleUploadStartResponse(MessageObject respond) {
    int conversationId = respond.getConversationId();
    UploadTask task = pendingUploads.remove(conversationId);
    if (task == null) {
      return;
    }
    if (!respond.isSuccess() || respond.getMessageId() <= 0 || respond.getNameFile() == null) {
      task.notifyError(respond.getMessage() == null ? "Upload rejected" : respond.getMessage());
      return;
    }

    task.messageId = respond.getMessageId();
    task.serverFileName = respond.getNameFile();
    activeUploads.put(task.messageId, task);
    Thread uploadThread = new Thread(() -> sendFileChunks(task), "upload-" + task.messageId);
    uploadThread.setDaemon(true);
    uploadThread.start();
  }

  private void handleUploadCompleteResponse(MessageObject respond) {
    int messageId = respond.getMessageId();
    UploadTask task = activeUploads.remove(messageId);
    if (task == null) {
      return;
    }
    task.notifyProgress(100);
    task.notifyCompleted();
  }

  private void sendFileChunks(UploadTask task) {
    long totalSize = task.file.length();
    if (totalSize == 0) {
      try {
        MessageObject chunk = new MessageObject(MessageType.UPLOAD_FILE_CHUNK);
        chunk.setConversationId(task.conversationId);
        chunk.setSenderId(task.senderId);
        chunk.setMessageId(task.messageId);
        chunk.setNameFile(task.serverFileName);
        chunk.setFileData(new byte[0]);
        chunk.setLast(true);
        writeMessage(chunk);
        task.notifyProgress(100);
      } catch (IOException ex) {
        activeUploads.remove(task.messageId);
        task.notifyError("Upload failed");
      }
      return;
    }

    byte[] buffer = new byte[64 * 1024];
    long sent = 0;
    try (FileInputStream fis = new FileInputStream(task.file)) {
      int read;
      while ((read = fis.read(buffer)) != -1) {
        byte[] chunkData = Arrays.copyOf(buffer, read);
        sent += read;

        MessageObject chunk = new MessageObject(MessageType.UPLOAD_FILE_CHUNK);
        chunk.setConversationId(task.conversationId);
        chunk.setSenderId(task.senderId);
        chunk.setMessageId(task.messageId);
        chunk.setNameFile(task.serverFileName);
        chunk.setFileData(chunkData);
        chunk.setLast(sent >= totalSize);
        writeMessage(chunk);

        int percent = (int) Math.min(100, (sent * 100) / totalSize);
        task.notifyProgress(percent);
      }
    } catch (IOException ex) {
      activeUploads.remove(task.messageId);
      task.notifyError("Upload failed");
    }
  }

  private void writeMessage(MessageObject request) throws IOException {
    synchronized (out) {
      out.writeObject(request);
      out.flush();
    }
  }

  @Override
  protected Void doInBackground() throws Exception {
    while (!isCancelled()) {
      MessageObject respond = (MessageObject) in.readObject();
      if (respond == null)
        break;
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
          mainFrame.getDashBoardPanel().setUserNameText(respond.getDisplayName() != null ? respond.getDisplayName() : respond.getUsername());
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
          // pendingDashBoardPanel.createChatPanel(info);
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
        // chatPanel = dashBoardPanel.createChatPanel(conversationInfo);
        // }

        if (chatPanel != null) {
          boolean isSelf = respond.getSenderId() == mainFrame.getCurrentUser().getUserId();
          String displayName = isSelf ? "You" : (conversationInfo != null ? conversationInfo.getDisplayName() : null);
          chatPanel.appendMessageBubble(respond.getMessageId(), displayName, respond.getChatMsg(), isSelf);
        }
      }

      if (respond.getType() == MessageType.SEND_GROUP_CHAT_MESSAGE_RESPONSE) {
        int conversationId = respond.getConversationId();
        DashBoardPanel dashBoardPanel = mainFrame.getDashBoardPanel();
        ChatPanel chatPanel = dashBoardPanel.getChatPanel(conversationId);
        if (chatPanel != null) {
          boolean isSelf = respond.getSenderId() == mainFrame.getCurrentUser().getUserId();
          String displayName = isSelf ? "You" : respond.getDisplayName();
          if (displayName == null || displayName.trim().isEmpty()) {
            displayName = isSelf ? "You" : "Member";
          }
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

      if (respond.getType() == MessageType.CREATE_GROUP_CONVERSATION_RESPOND) {
        if (respond.isSuccess()) {
          JOptionPane.showMessageDialog(mainFrame, "Create group successful", "Success",
              JOptionPane.INFORMATION_MESSAGE);
          try {
            GetInformationService getInformationService = new GetInformationService(out);
            getInformationService.sendGetInfoGroupConversation(mainFrame.getCurrentUser().getUserId());
          } catch (IOException e) {
            e.printStackTrace();
          }
          DashBoardPanel dashBoardPanel = mainFrame.getDashBoardPanel();
          AddGroupDialog dialog = dashBoardPanel.getAddGroupDialog();
          if (dialog != null) {
            dialog.dispose();
          }
        } else {
          String message = respond.getMessage() == null ? "Create group failed" : respond.getMessage();
          JOptionPane.showMessageDialog(mainFrame, message, "Failed", JOptionPane.ERROR_MESSAGE);
        }
      }

      if (respond.getType() == MessageType.GET_HISTORY_CHAT_RESPONSE) {
        int conversationId = respond.getConversationId();
        DashBoardPanel dashBoardPanel = mainFrame.getDashBoardPanel();
        ChatPanel chatPanel = dashBoardPanel.getChatPanel(conversationId);
        ArrayList<MessageChat> list = respond.getListMessage();
        if (chatPanel != null && list != null) {
          boolean isGroup = chatPanel.isGroupConversation();
          SingleConversationInfo conversationInfo = null;
          if (!isGroup) {
            DefaultListModel<SingleConversationInfo> model = dashBoardPanel.getSingleListModel();
            for (int i = 0; i < model.size(); i++) {
              SingleConversationInfo info = model.get(i);
              if (info.getConversationId() == conversationId) {
                conversationInfo = info;
                break;
              }
            }
          }

          // Xóa sạch chat area trước khi render lại để tránh duplicate
          chatPanel.clearChatArea();

          for (MessageChat message : list) {
            if (message == null) {
              continue;
            }
            boolean isSelf = message.getSenderID() == mainFrame.getCurrentUser().getUserId();
            String displayName = null;
            if (isSelf) {
              displayName = "You";
            } else if (!isGroup && conversationInfo != null) {
              displayName = conversationInfo.getDisplayName();
            } else if (isGroup) {
              displayName = message.getSenderDisplayName();
              if (displayName == null || displayName.trim().isEmpty()) {
                displayName = "Member";
              }
            } else {
              displayName = message.getSenderDisplayName();
            }

            String msgType = message.getType(); // "text" hoặc "file"
            if ("file".equals(msgType)) {
              String filePath = message.getFilePath();
              if (filePath != null && !filePath.trim().isEmpty()) {
                chatPanel.appendFileBubble(message.getMessageID(), displayName, filePath, isSelf);
              }
            } else {
              String text = message.getContent();
              if (text != null && !text.trim().isEmpty()) {
                chatPanel.appendMessageBubble(message.getMessageID(), displayName, text, isSelf);
              }
            }
          }
        }
      }

      if (respond.getType() == MessageType.UPLOAD_FILE_START_RESPONSE) {
        handleUploadStartResponse(respond);
      }

      if (respond.getType() == MessageType.UPLOAD_FILE_COMPLETE_RESPONSE) {
        handleUploadCompleteResponse(respond);
      }

      if (respond.getType() == MessageType.DOWNLOAD_FILE_CHUNK) {
        String nameFile = respond.getNameFile();
        DownloadTask task = activeDownloads.get(nameFile);
        if (task != null) {
          try {
            if (task.fos == null) {
              task.fos = new FileOutputStream(task.saveFile, false);
            }
            byte[] data = respond.getFileData();
            if (data != null && data.length > 0) {
              task.fos.write(data);
              task.receivedBytes += data.length;
            }
            int totalSize = respond.getTotalSize();
            if (totalSize > 0) {
              int percent = (int) Math.min(100, (task.receivedBytes * 100) / totalSize);
              task.notifyProgress(percent);
            }

            if (respond.isLast()) {
              task.fos.close();
              activeDownloads.remove(nameFile);
              task.notifyProgress(100);
              task.notifyCompleted();
            }
          } catch (IOException ex) {
            activeDownloads.remove(nameFile);
            if (task.fos != null) {
              try { task.fos.close(); } catch (IOException ignored) {}
            }
            task.notifyError("Error saving file locally");
          }
        }
      }

      if (respond.getType() == MessageType.DOWNLOAD_FILE_COMPLETE) {
        String nameFile = respond.getNameFile();
        DownloadTask task = activeDownloads.remove(nameFile);
        if (task != null) {
          if (task.fos != null) {
            try { task.fos.close(); } catch (IOException ignored) {}
          }
          if (!respond.isSuccess()) {
            task.notifyError(respond.getMessage() == null ? "Download failed" : respond.getMessage());
          }
        }
      }

      if (respond.getType() == MessageType.DELETE_MESSAGE_RESPONSE) {
        if (!respond.isSuccess()) {
          String msg = respond.getMessage();
          JOptionPane.showMessageDialog(mainFrame, 
              msg != null ? msg : "Failed to delete message", 
              "Delete Message", 
              JOptionPane.ERROR_MESSAGE);
        }
      }

    }
  }
}