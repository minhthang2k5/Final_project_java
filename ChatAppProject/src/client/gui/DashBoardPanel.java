package client.gui;

import javax.swing.*;

import client.ServerHandler;
import client.gui.components.UserListCellRenderer;
import client.model.ServerInfo;
import common.models.GroupConversationInfo;
import common.models.SingleConversationInfo;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class DashBoardPanel extends JPanel implements ActionListener, ListSelectionListener {
	private final JList<SingleConversationInfo> singleList;
	private final JList<GroupConversationInfo> groupList;
	private final JLabel userNameLabel;
	private final JLabel userIdLabel;
	private final JLabel serverNameLabel;
	private final JLabel serverAddrLabel;
	private final JButton signOutButton;
	private final JButton addPeopleButton;
	private final JButton addGroupButton;
	private final JPanel chatContainer;
	private final CardLayout chatLayout;
	private final Map<Integer, ChatPanel> chatPanels; // conversationId
	private AddGroupDialog addGroupDialog;
	ServerHandler serverHandler;
	private ServerInfo connectedServerInfo;
	public DashBoardPanel() {
		this.setBounds(0, 0, 900, 700);
		this.setLayout(new BorderLayout());
		this.setBackground(new Color(240, 240, 240));
		this.chatPanels = new HashMap<>();

		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.setPreferredSize(new Dimension(260, 0));
		leftPanel.setBackground(Color.white);
		leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));

		// === Server info section ===
		JPanel serverInfoSection = new JPanel();
		serverInfoSection.setBackground(new Color(240, 243, 255));
		serverInfoSection.setLayout(new BoxLayout(serverInfoSection, BoxLayout.Y_AXIS));
		serverInfoSection.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 215, 230)),
				BorderFactory.createEmptyBorder(8, 16, 8, 16)));

		JLabel connectedLabel = new JLabel("\u25CF Connected to:");
		connectedLabel.setFont(new Font(null, Font.PLAIN, 11));
		connectedLabel.setForeground(new Color(52, 168, 83));
		connectedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		serverInfoSection.add(connectedLabel);

		serverNameLabel = new JLabel("Server");
		serverNameLabel.setFont(new Font(null, Font.BOLD, 13));
		serverNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		serverInfoSection.add(serverNameLabel);

		serverAddrLabel = new JLabel("localhost:4321");
		serverAddrLabel.setFont(new Font(null, Font.PLAIN, 11));
		serverAddrLabel.setForeground(new Color(100, 100, 100));
		serverAddrLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		serverInfoSection.add(serverAddrLabel);

		// === Top section: server info + user info ===
		JPanel topSection = new JPanel();
		topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
		topSection.setBackground(Color.white);
		topSection.add(serverInfoSection);

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
		topSection.add(leftHeader);

		leftPanel.add(topSection, BorderLayout.NORTH);

		DefaultListModel<SingleConversationInfo> singleModel = new DefaultListModel<>();
		DefaultListModel<GroupConversationInfo> groupModel = new DefaultListModel<>();
		singleList = new JList<>(singleModel);
		groupList = new JList<>(groupModel);
		singleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		singleList.setFixedCellHeight(56);
		groupList.setFixedCellHeight(56);
		singleList.setCellRenderer(new UserListCellRenderer());
		groupList.setCellRenderer(new UserListCellRenderer());
		singleList.addListSelectionListener(this);
		groupList.addListSelectionListener(this);
		
		JScrollPane singleScroll = new JScrollPane(singleList);
		JScrollPane groupScroll = new JScrollPane(groupList);
		singleScroll.setBorder(BorderFactory.createEmptyBorder());
		groupScroll.setBorder(BorderFactory.createEmptyBorder());

		addPeopleButton = createTabActionButton("add people");
		addGroupButton = createTabActionButton("add group");
		addPeopleButton.addActionListener(this);
		addGroupButton.addActionListener(this);
		signOutButton.addActionListener(this);

		JPanel singleTab = buildTabPanel(singleScroll, addPeopleButton);
		JPanel groupTab = buildTabPanel(groupScroll, addGroupButton);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setFont(new Font(null, Font.PLAIN, 13));
		tabbedPane.addTab("Single", singleTab);
		tabbedPane.addTab("Group", groupTab);

		leftPanel.add(tabbedPane, BorderLayout.CENTER);

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.setBackground(new Color(248, 248, 248));

		chatLayout = new CardLayout();
		chatContainer = new JPanel(chatLayout);
		chatContainer.setBackground(new Color(248, 248, 248));
		chatContainer.add(buildEmptyChatState(), "empty");
		chatLayout.show(chatContainer, "empty");
		rightPanel.add(chatContainer, BorderLayout.CENTER);

		this.add(leftPanel, BorderLayout.WEST);
		this.add(rightPanel, BorderLayout.CENTER);
	}

	public JList<SingleConversationInfo> getSingleList() {
		return singleList;
	}

	public JList<GroupConversationInfo> getGroupList() {
		return groupList;
	}

	public JLabel getUserNameLabel() {
		return userNameLabel;
	}

	public JLabel getUserIdLabel() {
		return userIdLabel;
	}

	public void setUserNameText(String userName) {
		userNameLabel.setText(userName == null ? "" : userName);
	}

	public void setUserIdText(String userId) {
		userIdLabel.setText(userId == null ? "" : userId);
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

	public ChatPanel getActiveChatPanel() {
		return null;  // Không còn cần activeChatPanel
	}

	public AddGroupDialog getAddGroupDialog() {
		return addGroupDialog;
	}

	public void openChat(SingleConversationInfo conversationInfo) {
		if (conversationInfo == null) {
			return;
		}
		int conversationId = conversationInfo.getConversationId();
		String conversationKey = String.valueOf(conversationId);
		ChatPanel chatPanel = chatPanels.get(conversationId);
		if (chatPanel == null) {
			chatPanel = createChatPanel(conversationInfo);
			if (serverHandler != null) {
				try {
					serverHandler.requestGetHistoryChat(conversationId);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		chatLayout.show(chatContainer, conversationKey);
	}

	public void openChat(GroupConversationInfo conversationInfo) {
		if (conversationInfo == null) {
			return;
		}
		int conversationId = conversationInfo.getConversationId();
		String conversationKey = String.valueOf(conversationId);
		ChatPanel chatPanel = chatPanels.get(conversationId);
		if (chatPanel == null) {
			chatPanel = createChatPanel(conversationInfo);
			if (serverHandler != null) {
				try {
					serverHandler.requestGetHistoryChat(conversationId);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		chatLayout.show(chatContainer, conversationKey);
	}

	public void setChatPanel(ChatPanel chatPanel, int conversationId) {
		// Dùng hàm này nếu bạn muốn tự tạo ChatPanel
		if (chatPanel != null) {
			String conversationKey = String.valueOf(conversationId);
			chatPanels.put(conversationId, chatPanel);
			chatContainer.add(chatPanel, conversationKey);
			chatLayout.show(chatContainer, conversationKey);
		}
	}

	public ChatPanel getChatPanel(int conversationId) {
		return chatPanels.get(conversationId);
	}

	public ChatPanel createChatPanel(SingleConversationInfo conversationInfo) {
		if (conversationInfo == null) {
			return null;
		}

		int conversationId = conversationInfo.getConversationId();
		ChatPanel existing = chatPanels.get(conversationId);
		if (existing != null) {
			return existing;
		}
		ChatPanel chatPanel = new ChatPanel(conversationInfo.getDisplayName(), conversationId, serverHandler, false);
		chatPanel.setConversationInfo(conversationInfo);
		String conversationKey = String.valueOf(conversationId);
		chatPanels.put(conversationId, chatPanel);
		chatContainer.add(chatPanel, conversationKey);
		return chatPanel;
	}

	public ChatPanel createChatPanel(GroupConversationInfo conversationInfo) {
		if (conversationInfo == null) {
			return null;
		}
		int conversationId = conversationInfo.getConversationId();
		ChatPanel existing = chatPanels.get(conversationId);
		if (existing != null) {
			return existing;
		}
		ChatPanel chatPanel = new ChatPanel(conversationInfo.getGroupName(), conversationId, serverHandler, true);
		chatPanel.setConversationInfo(conversationInfo);
		String conversationKey = String.valueOf(conversationId);
		chatPanels.put(conversationId, chatPanel);
		chatContainer.add(chatPanel, conversationKey);
		return chatPanel;
	}

	public void setServerHandler(ServerHandler serverHandler) {
		this.serverHandler = serverHandler;
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

	private static JPanel buildEmptyChatState() {
		JPanel emptyPanel = new JPanel(new GridBagLayout());
		emptyPanel.setBackground(new Color(248, 248, 248));
		JLabel label = new JLabel("Select a conversation");
		label.setForeground(new Color(120, 120, 120));
		label.setFont(new Font(null, Font.PLAIN, 14));
		emptyPanel.add(label);
		return emptyPanel;
	}


	public DefaultListModel<SingleConversationInfo> getSingleListModel() {
    return (DefaultListModel<SingleConversationInfo>) singleList.getModel();
	}

	public DefaultListModel<GroupConversationInfo> getGroupListModel() {
		return (DefaultListModel<GroupConversationInfo>) groupList.getModel();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == signOutButton) {
			if (serverHandler == null) {
				JOptionPane.showMessageDialog(this, "Sign-out handler not ready", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				serverHandler.requestSignOut();
			} catch (IOException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(this, "Sign-out failed", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
			return;
		}
		if (e.getSource() == addPeopleButton) {
			showAddPeopleDialog();
		}
		if (e.getSource() == addGroupButton) {
			showAddGroupDialog();
		}
	}

	public void resetForSignOut() {
		setUserNameText("");
		setUserIdText("");
		DefaultListModel<SingleConversationInfo> model = getSingleListModel();
		model.clear();
		DefaultListModel<GroupConversationInfo> groupModel = getGroupListModel();
		groupModel.clear();
		chatPanels.clear();
		chatContainer.removeAll();
		chatContainer.add(buildEmptyChatState(), "empty");
		chatLayout.show(chatContainer, "empty");
		chatContainer.revalidate();
		chatContainer.repaint();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			return;
		}
		if (e.getSource() == singleList) {
			SingleConversationInfo selected = singleList.getSelectedValue();
			if (selected != null) {
				groupList.clearSelection();
				openChat(selected);
			}
		}
		if (e.getSource() == groupList) {
			GroupConversationInfo selected = groupList.getSelectedValue();
			if (selected != null) {
				singleList.clearSelection();
				openChat(selected);
			}
		}
	}

	private void showAddPeopleDialog() {
		JDialog dialog = new JDialog((Frame) null, "Add People", true);
		ImageIcon dialogIcon = loadIcon("/client/gui/asset/dialogue.png", 22);
		if (dialogIcon != null) {
			dialog.setIconImage(dialogIcon.getImage());
		}
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setLayout(new BorderLayout(12, 12));
		dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		JPanel inputPanel = new JPanel(new BorderLayout(8, 8));
		JLabel label = new JLabel("User ID");
		JTextField userIdField = new JTextField();
		inputPanel.add(label, BorderLayout.NORTH);
		inputPanel.add(userIdField, BorderLayout.CENTER);

		JButton addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String userIdText = userIdField.getText() == null ? "" : userIdField.getText().trim();
				if (userIdText.isEmpty() || !userIdText.matches("\\d+")) {
					JOptionPane.showMessageDialog(dialog, "User ID must be a number", "Invalid input",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					serverHandler.requestCreateConversation(userIdText);
					dialog.dispose();
				} catch (NumberFormatException | IOException e1) {
					e1.printStackTrace();
				}
			}
			
		});
		JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		actionPanel.add(addButton);

		dialog.add(inputPanel, BorderLayout.CENTER);
		dialog.add(actionPanel, BorderLayout.SOUTH);
		dialog.setSize(320, 150);
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
	}

	private void showAddGroupDialog() {
		if (addGroupDialog == null || !addGroupDialog.isDisplayable()) {
			addGroupDialog = new AddGroupDialog(this, serverHandler);
		}
		int currentUserId = serverHandler == null ? 0 : serverHandler.getCurrentUserId();
		addGroupDialog.prepareForNewGroup(currentUserId);
		addGroupDialog.setVisible(true);
	}

	// === Server info & Online users ===

	public void setConnectedServerInfo(ServerInfo info) {
		this.connectedServerInfo = info;
		if (info != null) {
			serverNameLabel.setText(info.getName());
			serverAddrLabel.setText(info.getHost() + ":" + info.getPort());
		} else {
			serverNameLabel.setText("Not connected");
			serverAddrLabel.setText("");
		}
	}

	public ServerInfo getConnectedServerInfo() {
		return connectedServerInfo;
	}

}
