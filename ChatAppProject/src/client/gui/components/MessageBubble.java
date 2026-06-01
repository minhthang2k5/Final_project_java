package client.gui.components;

import javax.swing.*;

import java.awt.*;

public class MessageBubble extends JPanel {

	/** Callback được gọi khi người dùng nhấn nút Download trên file message. */
	public interface DownloadListener {
		void onDownload(int messageId, String fileName);
	}

	public interface AudioPlayListener {
		void onPlayRequested(int messageId, String fileName, Runnable onComplete);
		void onStopRequested();
	}

	public interface DeleteListener {
		void onDeleteRequested(int messageId);
	}

	private final int messageId;
	private final String displayName;
	private final String text;
	private final boolean isSelf;
	private final boolean isFile;
	private final String fileName;
	private DownloadListener downloadListener;
	private AudioPlayListener audioPlayListener;
	private DeleteListener deleteListener;

	/** Constructor cho tin nhắn văn bản thông thường. */
	public MessageBubble(int messageId, String displayName, String text, boolean isSelf, int maxTextWidth) {
		this.messageId = messageId;
		this.displayName = displayName;
		this.text = text;
		this.isSelf = isSelf;
		this.isFile = false;
		this.fileName = null;
		buildTextUi(maxTextWidth);
		setupClickSupport();
	}

	/** Constructor cho tin nhắn file (icon file + tên file + nút Download). */
	public MessageBubble(int messageId, String displayName, String fileName, boolean isSelf) {
		this.messageId = messageId;
		this.displayName = displayName;
		this.text = fileName;
		this.isSelf = isSelf;
		this.isFile = true;
		this.fileName = fileName;
		if (fileName != null && fileName.toLowerCase().endsWith(".wav")) {
			buildAudioUi();
		} else {
			buildFileUi();
		}
		setupClickSupport();
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

	public boolean isFile() {
		return isFile;
	}

	public String getFileName() {
		return fileName;
	}

	/** Đặt listener để xử lý sự kiện Download từ bên ngoài (ChatPanel). */
	public void setDownloadListener(DownloadListener listener) {
		this.downloadListener = listener;
	}

	public void setAudioPlayListener(AudioPlayListener listener) {
		this.audioPlayListener = listener;
	}

	public void setDeleteListener(DeleteListener listener) {
		this.deleteListener = listener;
	}

	private void setupClickSupport() {
		java.awt.event.MouseAdapter clickAdapter = new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if (deleteListener != null) {
					deleteListener.onDeleteRequested(messageId);
				}
			}
		};
		addMouseListenerRecursively(this, clickAdapter);
	}

	private void addMouseListenerRecursively(Component comp, java.awt.event.MouseAdapter adapter) {
		// Bỏ qua nút Download để không bị trùng sự kiện click
		if (comp instanceof JButton) return; 
		comp.addMouseListener(adapter);
		comp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		if (comp instanceof Container) {
			for (Component child : ((Container) comp).getComponents()) {
				addMouseListenerRecursively(child, adapter);
			}
		}
	}

	// -------------------------------------------------------------------------
	// UI builders
	// -------------------------------------------------------------------------

	/** Xây dựng giao diện cho tin nhắn văn bản thông thường. */
	private void buildTextUi(int maxTextWidth) {
		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(isSelf ? new Color(224, 231, 255) : Color.WHITE);
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true),
				BorderFactory.createEmptyBorder(6, 10, 6, 10)));

		JTextPane messageText = new JTextPane();
		messageText.setEditable(false);
		messageText.setFont(new Font(null, Font.PLAIN, 14));
		messageText.setOpaque(false);
		messageText.setBorder(null);

		javax.swing.text.StyledDocument doc = messageText.getStyledDocument();
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(":([a-z-]+):");
		java.util.regex.Matcher matcher = pattern.matcher(text);
		int lastEnd = 0;
		try {
			while (matcher.find()) {
				if (matcher.start() > lastEnd) {
					doc.insertString(doc.getLength(), text.substring(lastEnd, matcher.start()), null);
				}
				String emojiCode = matcher.group(1);
				ImageIcon icon = loadIcon("/client/gui/asset/emoji/" + emojiCode + ".png", 20);
				if (icon != null) {
					messageText.setCaretPosition(doc.getLength());
					messageText.insertIcon(icon);
				} else {
					doc.insertString(doc.getLength(), matcher.group(0), null);
				}
				lastEnd = matcher.end();
			}
			if (lastEnd < text.length()) {
				doc.insertString(doc.getLength(), text.substring(lastEnd), null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		messageText.setSize(new Dimension(maxTextWidth, Short.MAX_VALUE));
		Dimension textPreferred = messageText.getPreferredSize();
		int targetWidth = Math.min(textPreferred.width + 5, maxTextWidth);
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

	/**
	 * Xây dựng giao diện cho tin nhắn file:
	 * [icon file]  [tên file]  [nút Download]
	 */
	private void buildFileUi() {
		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(isSelf ? new Color(224, 231, 255) : Color.WHITE);
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true),
				BorderFactory.createEmptyBorder(8, 10, 8, 10)));

		JPanel wrapper = new JPanel();
		wrapper.setOpaque(false);
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

		// --- Tên người gửi (nếu có) ---
		if (displayName != null && !displayName.trim().isEmpty()) {
			JLabel nameLabel = new JLabel(displayName.trim());
			nameLabel.setFont(new Font(null, Font.BOLD, 11));
			nameLabel.setForeground(new Color(120, 120, 120));
			nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			wrapper.add(nameLabel);
			wrapper.add(Box.createVerticalStrut(6));
		}

		// --- Hàng ngang: [icon file] [tên file] [khoảng trống] [nút download] ---
		JPanel fileRow = new JPanel(new BorderLayout(8, 0));
		fileRow.setOpaque(false);
		fileRow.setAlignmentX(Component.LEFT_ALIGNMENT);

		// Bên trái: icon + tên file
		JPanel leftPart = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		leftPart.setOpaque(false);

		ImageIcon fileIcon = loadIcon("/client/gui/asset/file.png", 22);
		JLabel iconLabel = fileIcon != null
				? new JLabel(fileIcon)
				: new JLabel("[");
		iconLabel.setVerticalAlignment(SwingConstants.CENTER);
		leftPart.add(iconLabel);

		// Hiển thị tên file (bỏ phần messageId_ ở đầu nếu có dạng "123_tenfile.txt")
		String displayFileName = stripMessageIdPrefix(fileName);
		JLabel fileNameLabel = new JLabel(displayFileName);
		fileNameLabel.setFont(new Font(null, Font.PLAIN, 13));
		fileNameLabel.setForeground(new Color(40, 40, 40));
		leftPart.add(fileNameLabel);

		fileRow.add(leftPart, BorderLayout.CENTER);

		// Bên phải: nút Download
		ImageIcon downloadIcon = loadIcon("/client/gui/asset/download.png", 18);
		JButton downloadBtn = downloadIcon != null
				? new JButton(downloadIcon)
				: new JButton("↓");
		downloadBtn.setToolTipText("Download " + displayFileName);
		downloadBtn.setFocusable(false);
		downloadBtn.setOpaque(false);
		downloadBtn.setContentAreaFilled(false);
		downloadBtn.setBorderPainted(false);
		downloadBtn.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 2));
		downloadBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		downloadBtn.addActionListener(e -> {
			if (downloadListener != null) {
				downloadListener.onDownload(messageId, fileName);
			}
		});
		fileRow.add(downloadBtn, BorderLayout.EAST);

		wrapper.add(fileRow);
		add(wrapper, BorderLayout.CENTER);
	}

	private void buildAudioUi() {
		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(isSelf ? new Color(224, 231, 255) : Color.WHITE);
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true),
				BorderFactory.createEmptyBorder(8, 10, 8, 10)));

		JPanel wrapper = new JPanel();
		wrapper.setOpaque(false);
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

		if (displayName != null && !displayName.trim().isEmpty()) {
			JLabel nameLabel = new JLabel(displayName.trim());
			nameLabel.setFont(new Font(null, Font.BOLD, 11));
			nameLabel.setForeground(new Color(120, 120, 120));
			nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			wrapper.add(nameLabel);
			wrapper.add(Box.createVerticalStrut(6));
		}

		JPanel audioRow = new JPanel(new BorderLayout(8, 0));
		audioRow.setOpaque(false);
		audioRow.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel leftPart = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		leftPart.setOpaque(false);

		JLabel iconLabel = new JLabel("🎤");
		iconLabel.setFont(new Font(null, Font.PLAIN, 18));
		iconLabel.setVerticalAlignment(SwingConstants.CENTER);
		leftPart.add(iconLabel);

		String displayFileName = stripMessageIdPrefix(fileName);
		JLabel fileNameLabel = new JLabel(displayFileName.startsWith("voice_") ? "Voice Message" : displayFileName);
		fileNameLabel.setFont(new Font(null, Font.PLAIN, 13));
		fileNameLabel.setForeground(new Color(40, 40, 40));
		leftPart.add(fileNameLabel);

		audioRow.add(leftPart, BorderLayout.CENTER);

		JButton playBtn = new JButton("▶ Play");
		playBtn.setFocusable(false);
		playBtn.setFont(new Font(null, Font.BOLD, 11));
		playBtn.setForeground(new Color(0x63, 0x66, 0xF1));
		playBtn.setBackground(new Color(240, 240, 255));
		playBtn.setBorderPainted(false);
		playBtn.setOpaque(true);
		playBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		playBtn.addActionListener(e -> {
			if (playBtn.getText().equals("▶ Play")) {
				if (audioPlayListener != null) {
					playBtn.setText("⏹ Stop");
					audioPlayListener.onPlayRequested(messageId, fileName, () -> {
						SwingUtilities.invokeLater(() -> playBtn.setText("▶ Play"));
					});
				}
			} else {
				if (audioPlayListener != null) {
					audioPlayListener.onStopRequested();
				}
				playBtn.setText("▶ Play");
			}
		});
		audioRow.add(playBtn, BorderLayout.EAST);

		wrapper.add(audioRow);
		add(wrapper, BorderLayout.CENTER);
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	/**
	 * Bỏ prefix dạng "<messageId>_" khỏi tên file nếu có.
	 * Ví dụ: "42_report.pdf" -> "report.pdf"
	 */
	private static String stripMessageIdPrefix(String raw) {
		if (raw == null) return "";
		int underscore = raw.indexOf('_');
		if (underscore > 0) {
			String prefix = raw.substring(0, underscore);
			try {
				Integer.parseInt(prefix);
				return raw.substring(underscore + 1);
			} catch (NumberFormatException ignored) {
			}
		}
		return raw;
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

	private static ImageIcon loadIcon(String path, int size) {
		java.net.URL url = MessageBubble.class.getResource(path);
		ImageIcon icon;
		if (url == null) {
			java.io.File f = new java.io.File("src" + path);
			if (!f.exists()) f = new java.io.File("ChatAppProject/src" + path);
			if (!f.exists()) return null;
			icon = new ImageIcon(f.getAbsolutePath());
		} else {
			icon = new ImageIcon(url);
		}
		Image image = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
		return new ImageIcon(image);
	}
}
