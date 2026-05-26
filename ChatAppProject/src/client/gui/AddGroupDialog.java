package client.gui;

import javax.swing.*;

import client.ServerHandler;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashSet;

public class AddGroupDialog extends JDialog implements ActionListener {
  private static final String ACTION_ADD_MEMBER = "addMember";
  private static final String ACTION_CREATE = "create";
  private static final String ACTION_CANCEL = "cancel";

  private final JTextField nameField;
  private final JTextField memberField;
  private final JButton addMemberButton;
  private final JButton createButton;
  private final JButton cancelButton;
  private final JPanel memberListPanel;
  private final JLabel memberStatusLabel;
  private final LinkedHashSet<String> members = new LinkedHashSet<>();
  private final ServerHandler serverHandler;
  private String pendingMemberId;

  public AddGroupDialog(Component parent, ServerHandler serverHandler) {
    super(SwingUtilities.getWindowAncestor(parent), "Create Group", ModalityType.APPLICATION_MODAL);
    this.serverHandler = serverHandler;
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setLayout(new BorderLayout(12, 12));
    getRootPane().setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

    JPanel formPanel = new JPanel();
    formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
    formPanel.setOpaque(false);

    JPanel namePanel = new JPanel(new BorderLayout(8, 6));
    namePanel.setOpaque(false);
    JLabel nameLabel = new JLabel("Group name");
    nameField = new JTextField();
    namePanel.add(nameLabel, BorderLayout.NORTH);
    namePanel.add(nameField, BorderLayout.CENTER);

    JPanel memberInputPanel = new JPanel(new BorderLayout(8, 6));
    memberInputPanel.setOpaque(false);
    JLabel memberLabel = new JLabel("Add member id");
    memberField = new JTextField();
    addMemberButton = new JButton("Add");
    addMemberButton.setFocusable(false);
    addMemberButton.setActionCommand(ACTION_ADD_MEMBER);
    addMemberButton.addActionListener(this);
    memberField.setActionCommand(ACTION_ADD_MEMBER);
    memberField.addActionListener(this);
    memberInputPanel.add(memberLabel, BorderLayout.NORTH);
    memberInputPanel.add(memberField, BorderLayout.CENTER);
    memberInputPanel.add(addMemberButton, BorderLayout.EAST);

    memberStatusLabel = new JLabel(" ");
    memberStatusLabel.setForeground(new Color(120, 120, 120));

    memberListPanel = new JPanel();
    memberListPanel.setLayout(new BoxLayout(memberListPanel, BoxLayout.Y_AXIS));
    memberListPanel.setBackground(Color.white);
    JScrollPane memberScroll = new JScrollPane(memberListPanel);
    memberScroll.setPreferredSize(new Dimension(360, 160));
    memberScroll.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(220, 220, 220)));

    JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    createButton = new JButton("Create");
    cancelButton = new JButton("Cancel");
    createButton.setFocusable(false);
    cancelButton.setFocusable(false);
    createButton.setActionCommand(ACTION_CREATE);
    cancelButton.setActionCommand(ACTION_CANCEL);
    createButton.addActionListener(this);
    cancelButton.addActionListener(this);
    actionPanel.add(cancelButton);
    actionPanel.add(createButton);

    formPanel.add(namePanel);
    formPanel.add(Box.createVerticalStrut(10));
    formPanel.add(memberInputPanel);

    JPanel memberStatusPanel = new JPanel(new BorderLayout());
    memberStatusPanel.setOpaque(false);
    memberStatusPanel.add(memberStatusLabel, BorderLayout.WEST);
    formPanel.add(Box.createVerticalStrut(4));
    formPanel.add(memberStatusPanel);
    formPanel.add(Box.createVerticalStrut(10));
    formPanel.add(memberScroll);

    add(formPanel, BorderLayout.CENTER);
    add(actionPanel, BorderLayout.SOUTH);
    setSize(420, 420);
    setLocationRelativeTo(parent);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    if (source == cancelButton) {
      dispose();
      return;
    }
    if (source == addMemberButton || source == memberField) {
      handleAddMember();
      return;
    }
    if (source == createButton) {
      handleCreate();
      return;
    }
    if (source instanceof JButton button) {
      Object memberValue = button.getClientProperty("member");
      if (memberValue instanceof String member) {
        members.remove(member);
        refreshMembers();
      }
    }
  }

  private void handleAddMember() {
    String memberText = memberField.getText() == null ? "" : memberField.getText().trim();
    if (memberText.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Member cannot be empty", "Invalid input",
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (!memberText.matches("\\d+")) {
      JOptionPane.showMessageDialog(this, "Member id must be a number", "Invalid input",
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (!members.add(memberText)) {
      JOptionPane.showMessageDialog(this, "Member already added", "Duplicate",
          JOptionPane.WARNING_MESSAGE);
      return;
    }
    members.remove(memberText);
    if (serverHandler == null) {
      setMemberStatus("Server handler not ready", true);
      return;
    }
    pendingMemberId = memberText;
    setMemberStatus("Checking user id...", false);
    try {
      serverHandler.requestCheckUserId(Integer.parseInt(memberText));
    } catch (NumberFormatException | java.io.IOException ex) {
      setMemberStatus("Failed to check user id", true);
      pendingMemberId = null;
    }
  }

  private void handleCreate() {
    String groupName = nameField.getText() == null ? "" : nameField.getText().trim();
    if (groupName.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Group name is required", "Invalid input",
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (members.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Please add at least one member", "Invalid input",
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    // TODO: Hook to server request when group create API is ready.
    dispose();
  }

  private void refreshMembers() {
    memberListPanel.removeAll();
    for (String member : members) {
      memberListPanel.add(createMemberRow(member));
    }
    memberListPanel.revalidate();
    memberListPanel.repaint();
  }

  private JPanel createMemberRow(String member) {
    JPanel row = new JPanel(new BorderLayout(8, 0));
    row.setBackground(Color.white);
    row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
    JLabel nameLabel = new JLabel(member);
    JButton removeButton = new JButton("Remove");
    removeButton.setFocusable(false);
    removeButton.putClientProperty("member", member);
    removeButton.addActionListener(this);
    row.add(nameLabel, BorderLayout.CENTER);
    row.add(removeButton, BorderLayout.EAST);
    return row;
  }

  public void setMemberStatus(String text, boolean isError) {
    String displayText = text == null || text.trim().isEmpty() ? " " : text.trim();
    memberStatusLabel.setText(displayText);
    if (isError) {
      memberStatusLabel.setForeground(new Color(200, 70, 70));
    } else {
      memberStatusLabel.setForeground(new Color(120, 120, 120));
    }
  }

  public void handleCheckUserIdResult(int userId, boolean exists) {
    String userIdText = String.valueOf(userId);
    if (pendingMemberId == null || !pendingMemberId.equals(userIdText)) {
      return;
    }
    if (!exists) {
      setMemberStatus("User id not found", true);
      pendingMemberId = null;
      return;
    }
    if (!members.add(userIdText)) {
      setMemberStatus("Member already added", true);
      pendingMemberId = null;
      return;
    }
    memberField.setText("");
    memberField.requestFocusInWindow();
    setMemberStatus("Member added", false);
    pendingMemberId = null;
    refreshMembers();
  }
}
