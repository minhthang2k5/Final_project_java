package client.gui;

import javax.swing.*;

import java.awt.*;

public class ChatPanel extends JPanel {
	private final JLabel headerTitle;
	private final JTextArea chatArea;
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

		chatArea = new JTextArea();
		chatArea.setEditable(false);
		chatArea.setLineWrap(true);
		chatArea.setWrapStyleWord(true);
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

	public JTextArea getChatArea() {
		return chatArea;
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
