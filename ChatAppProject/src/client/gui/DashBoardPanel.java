package client.gui;

import javax.swing.*;

import client.gui.components.UserListCellRenderer;
import java.awt.*;

public class DashBoardPanel extends JPanel {
	private final JList<String> singleList;
	private final JList<String> groupList;
	private final JLabel userNameLabel;
	private final JLabel userIdLabel;
	private final JButton signOutButton;
	private final JButton addPeopleButton;
	private final JButton addGroupButton;
	private final JTextArea chatArea;
	private final JTextField chatInput;
	private final JButton fileButton;
	private final JButton micButton;
	private final JButton sendButton;

	public DashBoardPanel() {
		this.setBounds(0, 0, 900, 700);
		this.setLayout(new BorderLayout());
		this.setBackground(new Color(240, 240, 240));

		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.setPreferredSize(new Dimension(260, 0));
		leftPanel.setBackground(Color.white);
		leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));

		JPanel leftHeader = new JPanel(new BorderLayout());
		leftHeader.setBackground(Color.white);
		leftHeader.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
		JPanel userInfoPanel = new JPanel();
		userInfoPanel.setOpaque(false);
		userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
		userNameLabel = new JLabel("Username");
		userNameLabel.setFont(new Font(null, Font.BOLD, 14));
		userIdLabel = new JLabel("ID: 0");
		userIdLabel.setFont(new Font(null, Font.PLAIN, 12));
		userIdLabel.setForeground(new Color(120, 120, 120));
		userInfoPanel.add(userNameLabel);
		userInfoPanel.add(Box.createVerticalStrut(4));
		userInfoPanel.add(userIdLabel);
		ImageIcon userIcon = loadIcon("/client/gui/asset/user.png", 26);
		JLabel userIconLabel = new JLabel(userIcon);
		JPanel userRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		userRow.setOpaque(false);
		userRow.add(userIconLabel);
		userRow.add(userInfoPanel);
		signOutButton = new JButton("Sign out");
		signOutButton.setFocusable(false);
		signOutButton.setFont(new Font(null, Font.PLAIN, 12));
		signOutButton.setBackground(new Color(245, 245, 245));
		signOutButton.setBorderPainted(false);
		leftHeader.add(userRow, BorderLayout.WEST);
		leftHeader.add(signOutButton, BorderLayout.EAST);
		leftPanel.add(leftHeader, BorderLayout.NORTH);

		DefaultListModel<String> singleModel = new DefaultListModel<>();
		DefaultListModel<String> groupModel = new DefaultListModel<>();
		singleList = new JList<>(singleModel);
		groupList = new JList<>(groupModel);
		singleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		singleList.setFixedCellHeight(56);
		groupList.setFixedCellHeight(56);
		singleList.setCellRenderer(new UserListCellRenderer());
		groupList.setCellRenderer(new UserListCellRenderer());
		groupModel.addElement("Java class group");
		groupModel.addElement("Database study");

		JScrollPane singleScroll = new JScrollPane(singleList);
		JScrollPane groupScroll = new JScrollPane(groupList);
		singleScroll.setBorder(BorderFactory.createEmptyBorder());
		groupScroll.setBorder(BorderFactory.createEmptyBorder());

		addPeopleButton = createTabActionButton("add people");
		addGroupButton = createTabActionButton("add group");
		JPanel singleTab = buildTabPanel(singleScroll, addPeopleButton);
		JPanel groupTab = buildTabPanel(groupScroll, addGroupButton);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setFont(new Font(null, Font.PLAIN, 13));
		tabbedPane.addTab("Single", singleTab);
		tabbedPane.addTab("Group", groupTab);

		leftPanel.add(tabbedPane, BorderLayout.CENTER);

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.setBackground(new Color(248, 248, 248));

		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(Color.white);
		headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
		JLabel headerTitle = new JLabel("Conversation");
		headerTitle.setFont(new Font(null, Font.BOLD, 16));
		headerTitle.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
		headerPanel.add(headerTitle, BorderLayout.WEST);
		rightPanel.add(headerPanel, BorderLayout.NORTH);

		chatArea = new JTextArea();
		chatArea.setEditable(false);
		chatArea.setLineWrap(true);
		chatArea.setWrapStyleWord(true);
		chatArea.setFont(new Font(null, Font.PLAIN, 14));
		chatArea.setBackground(new Color(250, 250, 250));
		chatArea.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
		JScrollPane chatScroll = new JScrollPane(chatArea);
		chatScroll.setBorder(BorderFactory.createEmptyBorder());
		rightPanel.add(chatScroll, BorderLayout.CENTER);

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
		rightPanel.add(inputPanel, BorderLayout.SOUTH);

		this.add(leftPanel, BorderLayout.WEST);
		this.add(rightPanel, BorderLayout.CENTER);
	}

	public JList<String> getSingleList() {
		return singleList;
	}

	public JList<String> getGroupList() {
		return groupList;
	}

	public JLabel getUserNameLabel() {
		return userNameLabel;
	}

	public JLabel getUserIdLabel() {
		return userIdLabel;
	}

	public JButton getSignOutButton() {
		return signOutButton;
	}

	public JButton getAddPeopleButton() {
		return addPeopleButton;
	}

	public JButton getAddGroupButton() {
		return addGroupButton;
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

	private static JPanel buildTabPanel(JScrollPane listScroll, JButton actionButton) {
		listScroll.getViewport().setBackground(Color.white);
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(Color.white);

		JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
		header.setBackground(Color.white);
		header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
		header.add(actionButton);
		panel.add(header, BorderLayout.NORTH);
		panel.add(listScroll, BorderLayout.CENTER);
		return panel;
	}

	private static JButton createTabActionButton(String text) {
		JButton button = new JButton(text);
		button.setFocusable(false);
		button.setFont(new Font(null, Font.PLAIN, 12));
		button.setBackground(new Color(0x63, 0x66, 0xF1));
		button.setForeground(Color.white);
		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setOpaque(true);
		button.setContentAreaFilled(true);
		button.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
		return button;
	}

	private static ImageIcon loadIcon(String path, int size) {
		java.net.URL url = DashBoardPanel.class.getResource(path);
		if (url == null) {
			return null;
		}
		ImageIcon icon = new ImageIcon(url);
		Image image = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
		return new ImageIcon(image);
	}


	public DefaultListModel<String> getSingleListModel() {
    return (DefaultListModel<String>) singleList.getModel();
	}

}
