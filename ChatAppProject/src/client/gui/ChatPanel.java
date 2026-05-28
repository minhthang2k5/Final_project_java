package client.gui;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import client.ServerHandler;
import client.gui.components.MessageBubble;
import common.models.GroupConversationInfo;
import common.models.SingleConversationInfo;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChatPanel extends JPanel {
	private static final int MAX_BUBBLE_TEXT_WIDTH = 320;

	private SingleConversationInfo singleConversationInfo;
	private GroupConversationInfo groupConversationInfo;
	private final int conversationId;
	private final boolean isGroupConversation;
	private final ServerHandler serverHandler;
	private final Map<Integer, MessageBubble> messageBubbles;
	private final JLabel headerTitle;
	private final JTextPane chatArea;
	private final JTextField chatInput;
	private final JButton fileButton;
	private final JButton micButton;
	private final JButton sendButton;

	public ChatPanel() {
		this("Conversation", -1, null);
	}

	public ChatPanel(String title) {
		this(title, -1, null);
	}

	public ChatPanel(String title, int conversationId, ServerHandler serverHandler) {
		this(title, conversationId, serverHandler, false);
	}

	public ChatPanel(String title, int conversationId, ServerHandler serverHandler, boolean isGroupConversation) {
		this.conversationId = conversationId;
		this.isGroupConversation = isGroupConversation;
		this.serverHandler = serverHandler;
		setLayout(new BorderLayout());
		setBackground(new Color(248, 248, 248));
		this.messageBubbles = new HashMap<>();

		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(Color.white);
		headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
		headerTitle = new JLabel(title == null ? "" : title);
		headerTitle.setFont(new Font(null, Font.BOLD, 16));
		headerTitle.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
		headerPanel.add(headerTitle, BorderLayout.WEST);
		add(headerPanel, BorderLayout.NORTH);

		chatArea = new JTextPane();
		chatArea.setEditable(false);
		chatArea.setFont(new Font(null, Font.PLAIN, 14));
		chatArea.setBackground(new Color(250, 250, 250));
		chatArea.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
		JScrollPane chatScroll = new JScrollPane(chatArea);
		chatScroll.setBorder(BorderFactory.createEmptyBorder());
		add(chatScroll, BorderLayout.CENTER);

		JPanel inputPanel = new JPanel(new BorderLayout(8, 0));
		inputPanel.setBackground(Color.white);
		inputPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
		chatInput = new JTextField();
		chatInput.setFont(new Font(null, Font.PLAIN, 14));
		chatInput.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
		ImageIcon fileIcon = loadIcon("/client/gui/asset/file.png", 18);
		ImageIcon micIcon = loadIcon("/client/gui/asset/microphone.png", 18);
		fileButton = new JButton(fileIcon);
		micButton = new JButton(micIcon);
		fileButton.setToolTipText("Attach file");
		micButton.setToolTipText("Voice message");
		fileButton.setFocusable(false);
		micButton.setFocusable(false);
		fileButton.setOpaque(false);
		micButton.setOpaque(false);
		fileButton.setContentAreaFilled(false);
		micButton.setContentAreaFilled(false);
		fileButton.setBorderPainted(false);
		micButton.setBorderPainted(false);
		fileButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		micButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		fileButton.setPreferredSize(new Dimension(36, 36));
		micButton.setPreferredSize(new Dimension(36, 36));
		sendButton = new JButton("Send");
		sendButton.setFocusable(false);
		sendButton.setBackground(new Color(0x63, 0x66, 0xF1));
		sendButton.setForeground(Color.WHITE);
		sendButton.setFont(new Font(null, Font.BOLD, 12));
		sendButton.setOpaque(true);
		sendButton.setBorderPainted(false);
		sendButton.setPreferredSize(new Dimension(90, 36));
		sendButton.addActionListener(event -> sendCurrentMessage());
		chatInput.addActionListener(event -> sendCurrentMessage());
		fileButton.addActionListener(event -> handleFileUpload());
		inputPanel.add(chatInput, BorderLayout.CENTER);
		JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
		actionPanel.setOpaque(false);
		actionPanel.add(fileButton);
		actionPanel.add(micButton);
		actionPanel.add(sendButton);
		inputPanel.add(actionPanel, BorderLayout.EAST);
		add(inputPanel, BorderLayout.SOUTH);
	}

	public void setHeaderTitle(String title) {
		headerTitle.setText(title == null ? "" : title);
	}

	public JLabel getHeaderTitleLabel() {
		return headerTitle;
	}

	public void setConversationInfo(SingleConversationInfo conversationInfo) {
		this.singleConversationInfo = conversationInfo;
		this.groupConversationInfo = null;
	}

	public void setConversationInfo(GroupConversationInfo conversationInfo) {
		this.groupConversationInfo = conversationInfo;
		this.singleConversationInfo = null;
	}

	public SingleConversationInfo getSingleConversationInfo() {
		return singleConversationInfo;
	}

	public GroupConversationInfo getGroupConversationInfo() {
		return groupConversationInfo;
	}

	public boolean isGroupConversation() {
		return isGroupConversation;
	}

	public JTextPane getChatArea() {
		return chatArea;
	}

	public void appendStyledText(String text, AttributeSet attributes) {
		if (text == null || text.isEmpty()) {
			return;
		}
		StyledDocument document = chatArea.getStyledDocument();
		try {
			document.insertString(document.getLength(), text, attributes);
		} catch (BadLocationException ex) {
			return;
		}
		chatArea.setCaretPosition(document.getLength());
	}

	public void appendMessageBubble(String text, boolean isSelf) {
		appendMessageBubble(null, text, isSelf);
	}

	public void appendMessageBubble(String displayName, String text, boolean isSelf) {
		appendMessageBubble(-1, displayName, text, isSelf);
	}

	public void appendMessageBubble(int messageId, String displayName, String text, boolean isSelf) {
		if (text == null || text.trim().isEmpty()) {
			return;
		}
		StyledDocument document = chatArea.getStyledDocument();
		int insertPos = document.getLength();
		SimpleAttributeSet paragraphStyle = new SimpleAttributeSet();
		StyleConstants.setAlignment(paragraphStyle, isSelf ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);
		StyleConstants.setSpaceAbove(paragraphStyle, 6f);
		StyleConstants.setSpaceBelow(paragraphStyle, 6f);
		chatArea.setCaretPosition(insertPos);
		MessageBubble bubble = new MessageBubble(messageId, displayName, text.trim(), isSelf, MAX_BUBBLE_TEXT_WIDTH);
		bubble.setDeleteListener(msgId -> {
			if (serverHandler == null || msgId < 0 || conversationId <= 0) return;
			int choice = JOptionPane.showConfirmDialog(ChatPanel.this, 
					"Do you want to delete this message?", 
					"Delete Message", 
					JOptionPane.YES_NO_OPTION);
			if (choice == JOptionPane.YES_OPTION) {
				try {
					serverHandler.requestDeleteMessage(msgId, conversationId);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
		if (messageId >= 0) {
			messageBubbles.put(messageId, bubble);
		}
		chatArea.insertComponent(bubble);
		document.setParagraphAttributes(insertPos, 1, paragraphStyle, false);
		try {
			document.insertString(document.getLength(), "\n", null);
		} catch (BadLocationException ex) {
			return;
		}
		chatArea.setCaretPosition(document.getLength());
	}

	/**
	 * Thêm bubble tin nhắn file vào chat:
	 * hiển thị icon file, tên file (bỏ prefix messageId_) và nút Download.
	 */
	public void appendFileBubble(int messageId, String displayName, String fileName, boolean isSelf) {
		if (fileName == null || fileName.trim().isEmpty()) {
			return;
		}
		StyledDocument document = chatArea.getStyledDocument();
		int insertPos = document.getLength();
		SimpleAttributeSet paragraphStyle = new SimpleAttributeSet();
		StyleConstants.setAlignment(paragraphStyle, isSelf ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);
		StyleConstants.setSpaceAbove(paragraphStyle, 6f);
		StyleConstants.setSpaceBelow(paragraphStyle, 6f);
		chatArea.setCaretPosition(insertPos);
		MessageBubble bubble = new MessageBubble(messageId, displayName, fileName.trim(), isSelf);
		bubble.setDownloadListener((msgId, name) -> {
			if (serverHandler == null || msgId < 0) return;
			
			String cleanName = name;
			int underscore = cleanName.indexOf('_');
			if (underscore > 0) {
				try {
					Integer.parseInt(cleanName.substring(0, underscore));
					cleanName = cleanName.substring(underscore + 1);
				} catch (NumberFormatException ignored) {}
			}

			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Save file as");
			chooser.setSelectedFile(new File(cleanName));
			int result = chooser.showSaveDialog(this);
			if (result != JFileChooser.APPROVE_OPTION) {
				return;
			}
			File saveFile = chooser.getSelectedFile();
			
			DownloadProgressDialog progressDialog = new DownloadProgressDialog(
					SwingUtilities.getWindowAncestor(this), cleanName);
			progressDialog.showDialog();

			try {
				serverHandler.requestDownloadFile(msgId, name, saveFile, new ServerHandler.DownloadListener() {
					@Override
					public void onProgress(int percent) {
						progressDialog.updateProgress(percent);
					}

					@Override
					public void onCompleted() {
						progressDialog.closeDialog();
						JOptionPane.showMessageDialog(ChatPanel.this, "Download complete: " + saveFile.getName(), "Download", JOptionPane.INFORMATION_MESSAGE);
					}

					@Override
					public void onError(String message) {
						progressDialog.closeDialog();
						String text = message == null ? "Download failed" : message;
						JOptionPane.showMessageDialog(ChatPanel.this, text, "Download", JOptionPane.ERROR_MESSAGE);
					}
				});
			} catch (IOException ex) {
				progressDialog.closeDialog();
				JOptionPane.showMessageDialog(this, "Download failed", "Download", JOptionPane.ERROR_MESSAGE);
			}
		});
		bubble.setDeleteListener(msgId -> {
			if (serverHandler == null || msgId < 0 || conversationId <= 0) return;
			int choice = JOptionPane.showConfirmDialog(ChatPanel.this, 
					"Do you want to delete this message?", 
					"Delete Message", 
					JOptionPane.YES_NO_OPTION);
			if (choice == JOptionPane.YES_OPTION) {
				try {
					serverHandler.requestDeleteMessage(msgId, conversationId);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
		if (messageId >= 0) {
			messageBubbles.put(messageId, bubble);
		}
		chatArea.insertComponent(bubble);
		document.setParagraphAttributes(insertPos, 1, paragraphStyle, false);
		try {
			document.insertString(document.getLength(), "\n", null);
		} catch (BadLocationException ex) {
			return;
		}
		chatArea.setCaretPosition(document.getLength());
	}

	/** Xóa sạch toàn bộ nội dung chat area để chuẩn bị render lại lịch sử. */
	public void clearChatArea() {
		chatArea.setText("");
		messageBubbles.clear();
	}

	public JTextField getChatInput() {
		return chatInput;
	}

	public JButton getFileButton() {
		return fileButton;
	}

	public JButton getMicButton() {
		return micButton;
	}

	public JButton getSendButton() {
		return sendButton;
	}

	private void sendCurrentMessage() {
		String text = chatInput.getText();
		if (text == null || text.trim().isEmpty()) {
			return;
		}
		if (serverHandler == null || conversationId <= 0) {
			return;
		}
		try {
			if (isGroupConversation) {
				serverHandler.requestSendGroupMessage(text.trim(), conversationId);
			} else {
				serverHandler.requestSendMessage(text.trim(), conversationId);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}

		chatInput.setText("");
		chatInput.requestFocusInWindow();
	}

	private void handleFileUpload() {
		if (serverHandler == null || conversationId <= 0) {
			return;
		}
		JFileChooser chooser = new JFileChooser();
		int result = chooser.showOpenDialog(this);
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File selectedFile = chooser.getSelectedFile();
		if (selectedFile == null || !selectedFile.exists() || !selectedFile.isFile()) {
			JOptionPane.showMessageDialog(this, "Invalid file", "Upload", JOptionPane.ERROR_MESSAGE);
			return;
		}

		UploadProgressDialog progressDialog = new UploadProgressDialog(
				SwingUtilities.getWindowAncestor(this), selectedFile.getName());
		progressDialog.showDialog();
		fileButton.setEnabled(false);

		try {
			serverHandler.requestUploadFile(selectedFile, conversationId, isGroupConversation,
					new ServerHandler.UploadListener() {
						@Override
						public void onProgress(int percent) {
							progressDialog.updateProgress(percent);
						}

						@Override
						public void onCompleted() {
							progressDialog.closeDialog();
							fileButton.setEnabled(true);
						}

						@Override
						public void onError(String message) {
							progressDialog.closeDialog();
							fileButton.setEnabled(true);
							String text = message == null ? "Upload failed" : message;
							JOptionPane.showMessageDialog(ChatPanel.this, text, "Upload", JOptionPane.ERROR_MESSAGE);
						}
					});
		} catch (IOException ex) {
			progressDialog.closeDialog();
			fileButton.setEnabled(true);
			JOptionPane.showMessageDialog(this, "Upload failed", "Upload", JOptionPane.ERROR_MESSAGE);
		}
	}

	private static ImageIcon loadIcon(String path, int size) {
		java.net.URL url = ChatPanel.class.getResource(path);
		if (url == null) {
			return null;
		}
		ImageIcon icon = new ImageIcon(url);
		Image image = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
		return new ImageIcon(image);
	}

	private static class UploadProgressDialog {
		private final JDialog dialog;
		private final JProgressBar progressBar;
		private final JLabel statusLabel;

		private UploadProgressDialog(Window owner, String fileName) {
			dialog = new JDialog(owner, "Uploading", Dialog.ModalityType.MODELESS);
			dialog.setLayout(new BorderLayout(10, 10));
			dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
			statusLabel = new JLabel("Uploading: " + (fileName == null ? "file" : fileName));
			progressBar = new JProgressBar(0, 100);
			progressBar.setStringPainted(true);
			progressBar.setValue(0);
			dialog.add(statusLabel, BorderLayout.NORTH);
			dialog.add(progressBar, BorderLayout.CENTER);
			dialog.setSize(360, 130);
			dialog.setLocationRelativeTo(owner);
		}

		private void showDialog() {
			dialog.setVisible(true);
		}

		private void updateProgress(int percent) {
			progressBar.setValue(Math.max(0, Math.min(100, percent)));
		}

		private void closeDialog() {
			dialog.setVisible(false);
			dialog.dispose();
		}
	}

	private static class DownloadProgressDialog {
		private final JDialog dialog;
		private final JProgressBar progressBar;
		private final JLabel statusLabel;

		private DownloadProgressDialog(Window owner, String fileName) {
			dialog = new JDialog(owner, "Downloading", Dialog.ModalityType.MODELESS);
			dialog.setLayout(new BorderLayout(10, 10));
			dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
			statusLabel = new JLabel("Downloading: " + (fileName == null ? "file" : fileName));
			progressBar = new JProgressBar(0, 100);
			progressBar.setStringPainted(true);
			progressBar.setValue(0);
			dialog.add(statusLabel, BorderLayout.NORTH);
			dialog.add(progressBar, BorderLayout.CENTER);
			dialog.setSize(360, 130);
			dialog.setLocationRelativeTo(owner);
		}

		private void showDialog() {
			dialog.setVisible(true);
		}

		private void updateProgress(int percent) {
			progressBar.setValue(Math.max(0, Math.min(100, percent)));
		}

		private void closeDialog() {
			dialog.setVisible(false);
			dialog.dispose();
		}
	}

}
