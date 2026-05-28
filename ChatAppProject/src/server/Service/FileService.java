package server.Service;

import java.io.File;

import common.net.MessageObject;
import common.net.MessageType;
import server.dao.FileDao;

public class FileService {

  private FileDao fileDao = new FileDao();

  /**
   * Trả về thư mục server/data/ nơi lưu file upload.
   * Dùng chung cho cả upload (ClientHandler) và download.
   */
  public static File getDataDir() {
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
    return dir;
  }

  /**
   * Xử lý yêu cầu bắt đầu upload file từ Client.
   *
   * Client gửi lên: conversationId, senderId, nameFile, totalSize.
   * Server lưu dòng tin nhắn loại 'file' vào database để lấy message_id,
   * sau đó trả lại message_id và tên file đã được đổi mới cho Client.
   * Tên file mới sẽ có dạng: "[message_id]_[tên_file_gốc]"
   * (Ví dụ: "123_baocao.pdf"), đây là tên file vật lý sẽ được lưu trên server.
   *
   * @param request MessageObject từ client chứa thông tin file
   * @return MessageObject phản hồi chứa message_id và tên file mới
   */
  public MessageObject handleUploadFileStart(MessageObject request) {
    MessageObject response = new MessageObject(MessageType.UPLOAD_FILE_START_RESPONSE);

    int conversationId = request.getConversationId();
    int senderId = request.getSenderId();
    String nameFile = request.getNameFile();
    int totalSize = request.getTotalSize();

    // Kiểm tra đầu vào
    if (conversationId <= 0 || senderId <= 0 || nameFile == null || nameFile.trim().isEmpty()) {
      response.setSuccess(false);
      response.setMessage("Fail input value");
      return response;
    }

    // INSERT vào database -> lấy message_id
    int messageId = fileDao.insertFileMessage(conversationId, senderId, nameFile.trim(), totalSize);
    if (messageId <= 0) {
      response.setSuccess(false);
      response.setMessage("Fail database from server");
      return response;
    }

    String originalName = nameFile.trim();
    String newFileName = messageId + "_" + originalName;

    boolean updated = fileDao.updateFileName(messageId, newFileName);
    if (!updated) {
      response.setSuccess(false);
      response.setMessage("Fail to update file name");
      return response;
    }

    response.setSuccess(true);
    response.setMessageId(messageId);
    response.setNameFile(newFileName);
    response.setConversationId(conversationId);
    response.setSenderId(senderId);
    System.out.println("[FileService] Upload started: messageId=" + messageId + ", file=" + newFileName);
    return response;
  }

  /**
   * Tạo response báo upload hoàn tất để broadcast cho tất cả thành viên.
   * Response này được gửi đến mọi participant online trong cuộc trò chuyện,
   * bao gồm cả người gửi, để hiển thị file message trong khung chat.
   *
   * @param request MessageObject của chunk cuối cùng (chứa messageId,
   *                conversationId, senderId, nameFile)
   * @return MessageObject kiểu UPLOAD_FILE_COMPLETE_RESPONSE
   */
  public MessageObject buildCompleteResponse(MessageObject request) {
    MessageObject response = new MessageObject(MessageType.UPLOAD_FILE_COMPLETE_RESPONSE);
    response.setSuccess(true);
    response.setMessageId(request.getMessageId());
    response.setConversationId(request.getConversationId());
    response.setSenderId(request.getSenderId());
    response.setNameFile(request.getNameFile()); // Ví dụ: "123_baocao.pdf"
    System.out.println("[FileService] Upload complete: messageId=" + request.getMessageId()
        + ", file=" + request.getNameFile());
    return response;
  }
}
