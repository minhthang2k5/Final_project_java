package client.gui;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.*;

public class ChatPanel extends JPanel {
	private static final int MAX_BUBBLE_TEXT_WIDTH = 320;

	private final JLabel headerTitle;
	private final JTextPane chatArea;
	private final JTextField chatInput;
	private final JButton fileButton;
	private final JButton micButton;
	private final JButton sendButton;

	public ChatPanel() {
		this("Conversation");
	}

	public ChatPanel(String title) {
		setLayout(new BorderLayout());
		setBackground(new Color(248, 248, 248));

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
		chatArea.insertComponent(buildMessageBubble(displayName, text.trim(), isSelf));
		document.setParagraphAttributes(insertPos, 1, paragraphStyle, false);
		try {
			document.insertString(document.getLength(), "\n", null);
		} catch (BadLocationException ex) {
			return;
		}
		chatArea.setCaretPosition(document.getLength());
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

	private JComponent buildMessageBubble(String displayName, String text, boolean isSelf) {
		JTextArea messageText = new JTextArea(text);
		messageText.setEditable(false);
		messageText.setLineWrap(true);
		messageText.setWrapStyleWord(true);
		messageText.setFont(new Font(null, Font.PLAIN, 14));
		messageText.setOpaque(false);
		messageText.setBorder(null);
		int textWidth = measureTextWidth(text, messageText.getFontMetrics(messageText.getFont()));
		int targetWidth = Math.min(textWidth, MAX_BUBBLE_TEXT_WIDTH);
		messageText.setSize(new Dimension(targetWidth, Short.MAX_VALUE));
		Dimension textPreferred = messageText.getPreferredSize();
		messageText.setPreferredSize(new Dimension(targetWidth, textPreferred.height));

		JPanel content = new JPanel();
		content.setOpaque(false);
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		if (displayName != null && !displayName.trim().isEmpty()) {
			JLabel nameLabel = new JLabel(displayName.trim());
			nameLabel.setFont(new Font(null, Font.BOLD, 11));
			nameLabel.setForeground(new Color(120, 120, 120));
			nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			content.add(nameLabel);
			content.add(Box.createVerticalStrut(4));
		}
		messageText.setAlignmentX(Component.LEFT_ALIGNMENT);
		content.add(messageText);

		JPanel bubble = new JPanel(new BorderLayout());
		bubble.setOpaque(true);
		bubble.setBackground(isSelf ? new Color(224, 231, 255) : Color.WHITE);
		bubble.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true),
				BorderFactory.createEmptyBorder(6, 10, 6, 10)));
		bubble.add(content, BorderLayout.CENTER);
		return bubble;
	}

	private void sendCurrentMessage() {
		String text = chatInput.getText();
		if (text == null || text.trim().isEmpty()) {
			return;
		}
		appendMessageBubble("You", text.trim(), true);
		chatInput.setText("");
		chatInput.requestFocusInWindow();
	}

	private int measureTextWidth(String text, FontMetrics metrics) {
		if (text == null || text.isEmpty()) {
			return 10;
		}
		int maxWidth = 10;
		String[] lines = text.split("\n", -1);
		for (String line : lines) {
			String normalized = line.replace("\r", "");
			maxWidth = Math.max(maxWidth, metrics.stringWidth(normalized));
		}
		return maxWidth + 4;
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
}
