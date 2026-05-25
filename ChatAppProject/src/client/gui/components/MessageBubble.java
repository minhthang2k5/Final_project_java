package client.gui.components;

import javax.swing.*;

import java.awt.*;

public class MessageBubble extends JPanel {
	private final int messageId;
	private final String displayName;
	private final String text;
	private final boolean isSelf;

	public MessageBubble(int messageId, String displayName, String text, boolean isSelf, int maxTextWidth) {
		this.messageId = messageId;
		this.displayName = displayName;
		this.text = text;
		this.isSelf = isSelf;
		buildUi(maxTextWidth);
	}

	public int getMessageId() {
		return messageId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getText() {
		return text;
	}

	public boolean isSelf() {
		return isSelf;
	}

	private void buildUi(int maxTextWidth) {
		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(isSelf ? new Color(224, 231, 255) : Color.WHITE);
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true),
				BorderFactory.createEmptyBorder(6, 10, 6, 10)));

		JTextArea messageText = new JTextArea(text);
		messageText.setEditable(false);
		messageText.setLineWrap(true);
		messageText.setWrapStyleWord(true);
		messageText.setFont(new Font(null, Font.PLAIN, 14));
		messageText.setOpaque(false);
		messageText.setBorder(null);

		int textWidth = measureTextWidth(text, messageText.getFontMetrics(messageText.getFont()));
		int targetWidth = Math.min(textWidth, maxTextWidth);
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

		add(content, BorderLayout.CENTER);
	}

	private int measureTextWidth(String message, FontMetrics metrics) {
		if (message == null || message.isEmpty()) {
			return 10;
		}
		int maxWidth = 10;
		String[] lines = message.split("\n", -1);
		for (String line : lines) {
			String normalized = line.replace("\r", "");
			maxWidth = Math.max(maxWidth, metrics.stringWidth(normalized));
		}
		return maxWidth + 4;
	}
}
