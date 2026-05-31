package server.handlers;

import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import common.net.MessageObject;
import common.net.MessageType;
import server.Server;
import server.Service.AuthService;
import server.Service.ChatRoutingService;
import server.Service.FileService;
import server.Service.GroupConversationService;
import server.Service.SingleConversationService;
import server.dao.ChatDao;
import server.dao.UserDao;

import java.io.*;

public class ClientHandler implements Runnable {
  private Socket socket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private String username;
  private int userId;
  private SingleConversationService singleConversationService = new SingleConversationService();
  private GroupConversationService groupConversationService = new GroupConversationService();
  // Map lưu trữ các FileOutputStream đang mở, key = messageId
  private final java.util.Map<Integer, FileOutputStream> fileStreams = new java.util.HashMap<>();

  // Metadata cho Server GUI
  private final Server server; // null nếu dùng constructor cũ
  private final String clientIP;
  private final int clientPort;
  private final String connectedTime;

  public ClientHandler(Socket socket) {
    this(socket, null);
  }

  public ClientHandler(Socket socket, Server server) {
    this.socket = socket;
    this.server = server;
    this.clientIP = socket.getInetAddress().getHostAddress();
    this.clientPort = socket.getPort();
    this.connectedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
  }

  // Getters cho GUI
  public String getClientIP() { return clientIP; }
  public int getClientPort() { return clientPort; }
  public String getConnectedTime() { return connectedTime; }
  public String getUsername() { return username; }
  public int getUserId() { return userId; }

  public ObjectOutputStream getOutputStream() {
    return out;
  }

  public ObjectInputStream getInputStream() {
    return in;
  }

  @Override
  public void run() {
    try {
      out = new ObjectOutputStream(socket.getOutputStream());
      out.flush(); // Quan trọng: gửi stream header
      in = new ObjectInputStream(socket.getInputStream());

      while (true) {
        MessageObject request = (MessageObject) in.readObject();
        if (request == null)
          break;
        if (request.getType() == MessageType.LOGIN_REQUEST) {
          AuthService aService = new AuthService();
          MessageObject response = aService.authenticate(request);
          // System.out.println(request.getUsername()); //test
          out.writeObject(response);
          if (response.isSuccess()) {
            this.username = request.getUsername();
            this.userId = response.getUserId();
            // Add user
            Server.onlineUsers.put(username, this);
            if (server != null) {
              server.log(username + " logged in (" + clientIP + ":" + clientPort + ")");
              server.notifyClientUpdated(this);
            } else {
              System.out.println(username + " online");
            }
            singleConversationService.sendStatusOnline(userId);
          }
        }
        if (request.getType() == MessageType.REGISTER_REQUEST) {
          AuthService aService = new AuthService();
          MessageObject response = aService.checkConditionForRegisterAndAdd(request);
          // System.out.println("Register from port: " + socket.getPort()); //test
          out.writeObject(response);
          out.flush();
        }
        if (request.getType() == MessageType.CREATE_SINGLE_CONVERSATION_REQUEST) {
          ChatRoutingService chatRoutingService = new ChatRoutingService();
          MessageObject response = chatRoutingService.createConversation(request);
          out.writeObject(response);
          out.flush();
          if (response.isSuccess()) {
            // Send cho người đối diện
            singleConversationService.sendStatusOnline(userId);
            // Send cho bản thân mình
            SingleConversationService singleConversationService = new SingleConversationService();
            MessageObject response2 = singleConversationService.sendSingleConversationInfo(userId);
            out.writeObject(response2);
            out.flush();
          }
        }
        if (request.getType() == MessageType.GET_LIST_SINGLE_USER_REQUEST) {
          SingleConversationService singleConversationService = new SingleConversationService();
          System.out.println(request.getUserId());
          MessageObject response = singleConversationService.sendSingleConversationInfo(request.getUserId());
          out.writeObject(response);
          out.flush();
        }
        if (request.getType() == MessageType.GET_LIST_GROUP_REQUEST) {
          MessageObject response = groupConversationService.sendGroupConversationInfo(request.getUserId());
          out.writeObject(response);
          out.flush();
        }
        if (request.getType() == MessageType.SEND_SINGLE_CHAT_MESSAGE_REQUEST) {
          ChatRoutingService chatRoutingService = new ChatRoutingService();
          MessageObject response = chatRoutingService.handleReceiveSingleChatMessage(request);
          // Gửi cho chính mình
          sendChatMessage(response);
          // Gửi cho người trong cuộc trò chuyện nếu online
          UserDao userDao = new UserDao();
          ChatDao chatDao = new ChatDao();
          int otherUserId = chatDao.getTheOtherUserInSingleConversation(request.getConversationId(), userId);
          if (otherUserId > 0) {
            String otherUsername = userDao.fetchUsername(otherUserId);
            if (otherUsername != null && Server.onlineUsers.containsKey(otherUsername)) {
              Server.onlineUsers.get(otherUsername).sendChatMessage(response);
            }
          }
        }
        if (request.getType() == MessageType.SEND_GROUP_CHAT_MESSAGE_REQUEST) {
          ChatRoutingService chatRoutingService = new ChatRoutingService();
          MessageObject response = chatRoutingService.handleReceiveGroupChatMessage(request);
          // Gửi cho chính mình
          sendChatMessage(response);
          // Gửi cho các thành viên trong group nếu online
          ChatDao chatDao = new ChatDao();
          String[] listUsername = chatDao.getListUserInGroupConversation(request.getConversationId());
          if (listUsername != null) {
            for (String name : listUsername) {
              if (name == null || name.trim().isEmpty()) {
                continue;
              }
              ClientHandler handler = Server.onlineUsers.get(name);
              if (handler != null && handler != this) {
                handler.sendChatMessage(response);
              }
            }
          }
        }
        if (request.getType() == MessageType.SIGNOUT_REQUEST) {
          if (this.username != null) {
            Server.onlineUsers.remove(this.username);
            if (server != null) {
              server.log(this.username + " signed out");
            } else {
              System.out.println(this.username + " offline");
            }
            singleConversationService.sendStatusOnline(this.userId);
          }
          this.username = null;
          this.userId = 0;
        }
        if (request.getType() == MessageType.CHECK_EXITS_USER_ID_REQUEST) {
          AuthService authService = new AuthService();
          MessageObject response = authService.checkExistsUserId(request);
          out.writeObject(response);
          out.flush();
        }
        if (request.getType() == MessageType.CREATE_GROUP_CONVERSATION_REQUEST) {
          MessageObject response = groupConversationService.handleCreateGroupConversation(request);
          out.writeObject(response);
          out.flush();
          if (response.isSuccess()) {
            ChatDao chatDao = new ChatDao();
            UserDao userDao = new UserDao();
            String[] listUsername = chatDao.getListUserInGroupConversation(response.getConversationId());
            if (listUsername != null) {
              for (String name : listUsername) {
                if (name == null || name.trim().isEmpty()) {
                  continue;
                }
                ClientHandler handler = Server.onlineUsers.get(name);
                if (handler != null) {
                  int targetUserId = userDao.fetchUserID(name);
                  if (targetUserId > 0) {
                    handler.sendGroupConversationInfo(targetUserId);
                  }
                }
              }
            }
          }
        }
        if (request.getType() == MessageType.GET_HISTORY_CHAT_REQUEST) {
          ChatRoutingService chatRoutingService = new ChatRoutingService();
          MessageObject response = chatRoutingService.handleGetHistoryChat(request);
          out.writeObject(response);
          out.flush();
        }
        if (request.getType() == MessageType.UPLOAD_FILE_START_REQUEST) {
          FileService fileService = new FileService();
          MessageObject response = fileService.handleUploadFileStart(request);
          out.writeObject(response);
          out.flush();
        }
        if (request.getType() == MessageType.UPLOAD_FILE_CHUNK) {
          int messageId = request.getMessageId();
          String nameFile = request.getNameFile(); // Ví dụ: "123_baocao.pdf"
          byte[] data = request.getFileData();
          boolean isLast = request.isLast();

          // Mở FileOutputStream lần đầu nếu chưa có (chế độ append)
          FileOutputStream fos = fileStreams.get(messageId);
          if (fos == null) {
            File baseDir = new File(System.getProperty("user.dir"));
            File projectDir = null;
            File cursor = baseDir;
            while (cursor != null) {
              if ("ChatAppProject".equals(cursor.getName())) {
                projectDir = cursor;
                break;
              }
              File child = new File(cursor, "ChatAppProject");
              if (child.exists() && child.isDirectory()) {
                projectDir = child;
                break;
              }
              cursor = cursor.getParentFile();
            }

            File dir = projectDir == null
                ? new File(baseDir, "src" + File.separator + "server" + File.separator + "data")
                : new File(projectDir, "src" + File.separator + "server" + File.separator + "data");
            if (!dir.exists())
              dir.mkdirs();
            File saveFile = new File(dir, nameFile);
            fos = new FileOutputStream(saveFile, true); // append = true
            fileStreams.put(messageId, fos);
          }

          // Ghi dữ liệu chunk vào file
          if (data != null && data.length > 0) {
            fos.write(data);
            fos.flush();
          }

          // Chunk cuối: đóng stream và broadcast thông báo hoàn tất
          if (isLast) {
            fos.close();
            fileStreams.remove(messageId);

            // Tạo response hoàn tất
            FileService fileService = new FileService();
            MessageObject broadcast = fileService.buildCompleteResponse(request);

            // Gửi cho tất cả thành viên online trong cuộc trò chuyện
            ChatDao chatDao = new ChatDao();
            String[] participants = chatDao.getUsernameInConversation(request.getConversationId());
            if (participants != null) {
              for (String name : participants) {
                if (name == null || name.trim().isEmpty())
                  continue;
                ClientHandler handler = Server.onlineUsers.get(name);
                if (handler != null) {
                  handler.sendChatMessage(broadcast);
                }
              }
            }

            // Sau khi upload thành công, gửi GET_HISTORY_CHAT_RESPONSE cho tất cả bên liên quan
            // để các client tự cập nhật lại lịch sử chat (giống pattern SEND_SINGLE/GROUP_CHAT)
            ChatRoutingService chatRoutingService = new ChatRoutingService();
            MessageObject historyRequest = new MessageObject(MessageType.GET_HISTORY_CHAT_REQUEST);
            historyRequest.setConversationId(request.getConversationId());
            MessageObject historyResponse = chatRoutingService.handleGetHistoryChat(historyRequest);
            System.out.println("[ClientHandler] Sending GET_HISTORY_CHAT_RESPONSE for conversationId=" + request.getConversationId());
            if (participants != null) {
              for (String name : participants) {
                if (name == null || name.trim().isEmpty())
                  continue;
                ClientHandler handler = Server.onlineUsers.get(name);
                if (handler != null) {
                  handler.sendChatMessage(historyResponse);
                  System.out.println("[ClientHandler] Sent history to: " + name);
                }
              }
            }
          }
        }

        if (request.getType() == MessageType.DOWNLOAD_FILE_REQUEST) {
          String nameFile = request.getNameFile();
          int messageId = request.getMessageId();
          File dataDir = FileService.getDataDir();
          File targetFile = new File(dataDir, nameFile);

          if (!targetFile.exists() || !targetFile.isFile()) {
            MessageObject errorMsg = new MessageObject(MessageType.DOWNLOAD_FILE_COMPLETE);
            errorMsg.setSuccess(false);
            errorMsg.setMessageId(messageId);
            errorMsg.setMessage("File not found on server");
            sendChatMessage(errorMsg);
          } else {
            long totalSize = targetFile.length();
            if (totalSize == 0) {
              MessageObject chunk = new MessageObject(MessageType.DOWNLOAD_FILE_CHUNK);
              chunk.setMessageId(messageId);
              chunk.setNameFile(nameFile);
              chunk.setFileData(new byte[0]);
              chunk.setTotalSize(0);
              chunk.setLast(true);
              sendChatMessage(chunk);
            } else {
              byte[] buffer = new byte[64 * 1024];
              long sent = 0;
              try (FileInputStream fis = new FileInputStream(targetFile)) {
                int read;
                while ((read = fis.read(buffer)) != -1) {
                  byte[] chunkData = java.util.Arrays.copyOf(buffer, read);
                  sent += read;

                  MessageObject chunk = new MessageObject(MessageType.DOWNLOAD_FILE_CHUNK);
                  chunk.setMessageId(messageId);
                  chunk.setNameFile(nameFile);
                  chunk.setFileData(chunkData);
                  chunk.setTotalSize((int) totalSize);
                  chunk.setLast(sent >= totalSize);
                  sendChatMessage(chunk);
                }
              } catch (IOException ex) {
                ex.printStackTrace();
                MessageObject errorMsg = new MessageObject(MessageType.DOWNLOAD_FILE_COMPLETE);
                errorMsg.setSuccess(false);
                errorMsg.setMessageId(messageId);
                errorMsg.setMessage("Error reading file on server");
                sendChatMessage(errorMsg);
              }
            }
          }
        }

        if (request.getType() == MessageType.DELETE_MESSAGE_REQUEST) {
          ChatRoutingService chatRoutingService = new ChatRoutingService();
          MessageObject response = chatRoutingService.handleDeleteMessage(request);
          sendChatMessage(response);

          if (response.isSuccess()) {
            MessageObject historyRequest = new MessageObject(MessageType.GET_HISTORY_CHAT_REQUEST);
            historyRequest.setConversationId(request.getConversationId());
            MessageObject historyResponse = chatRoutingService.handleGetHistoryChat(historyRequest);

            ChatDao chatDao = new ChatDao();
            String[] participants = chatDao.getUsernameInConversation(request.getConversationId());
            if (participants != null) {
              for (String name : participants) {
                if (name == null || name.trim().isEmpty())
                  continue;
                ClientHandler handler = Server.onlineUsers.get(name);
                if (handler != null) {
                  handler.sendChatMessage(historyResponse);
                }
              }
            }
          }
        }

      }

    } catch (java.io.EOFException | java.net.SocketException e) {
      // client disconnected normally
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } finally {

      if (username != null) {
        Server.onlineUsers.remove(username);
        if (server != null) {
          server.log(username + " disconnected");
        } else {
          System.out.println(username + " offline");
        }
        singleConversationService.sendStatusOnline(userId);
      }
      // Đóng tất cả FileOutputStream còn đang mở nếu client disconnect giữa chừng
      for (FileOutputStream fos : fileStreams.values()) {
        try {
          fos.close();
        } catch (IOException e) {
        }
      }
      fileStreams.clear();
      closeConnection();

      // Notify server GUI
      if (server != null) {
        server.notifyClientDisconnected(this);
      }
    }
  }

  public void sendStatusOnline() throws IOException {
    MessageObject response = singleConversationService.sendSingleConversationInfo(userId);
    out.writeObject(response);
    out.flush();
  }

  public void sendChatMessage(MessageObject response) throws IOException {
    out.writeObject(response);
    out.flush();
  }

  public void sendGroupConversationInfo(int userId) throws IOException {
    MessageObject response = groupConversationService.sendGroupConversationInfo(userId);
    out.writeObject(response);
    out.flush();
  }

  /** Đóng tất cả stream và socket của client này. */
  public void closeConnection() {
    try {
      if (in != null)
        in.close();
    } catch (IOException e) {
    }
    try {
      if (out != null)
        out.close();
    } catch (IOException e) {
    }
    try {
      if (socket != null && !socket.isClosed())
        socket.close();
    } catch (IOException e) {
    }
  }
}
