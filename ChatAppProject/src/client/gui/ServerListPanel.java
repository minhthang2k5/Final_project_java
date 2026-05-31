package client.gui;

import client.model.ServerInfo;
import client.service.ServerConfigManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Màn hình chọn server để kết nối — hiển thị trước LoginPanel.
 * Cho phép thêm/sửa/xóa server, lưu vào file config.
 */
public class ServerListPanel extends JPanel {

  private final MainFrame mainFrame;
  private final ServerConfigManager configManager;
  private final DefaultListModel<ServerInfo> listModel;
  private final JList<ServerInfo> serverList;
  private final JButton connectButton;
  private final JButton addButton;
  private final JButton editButton;
  private final JButton deleteButton;
  private final JLabel statusLabel;

  public ServerListPanel(MainFrame mainFrame) {
    this.mainFrame = mainFrame;
    this.configManager = new ServerConfigManager();

    setLayout(new BorderLayout());
    setBackground(new Color(240, 240, 240));

    // ====== Center card ======
    JPanel centerWrapper = new JPanel(new GridBagLayout());
    centerWrapper.setOpaque(false);

    JPanel card = new JPanel();
    card.setBackground(Color.WHITE);
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
    card.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
        BorderFactory.createEmptyBorder(28, 36, 28, 36)));
    card.setPreferredSize(new Dimension(420, 480));

    // Title
    JLabel title = new JLabel("Connect to Server");
    title.setFont(new Font(null, Font.BOLD, 24));
    title.setAlignmentX(Component.CENTER_ALIGNMENT);
    card.add(title);
    card.add(Box.createVerticalStrut(6));

    JLabel subtitle = new JLabel("Select a server or add a new one");
    subtitle.setFont(new Font(null, Font.PLAIN, 13));
    subtitle.setForeground(new Color(120, 120, 120));
    subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
    card.add(subtitle);
    card.add(Box.createVerticalStrut(18));

    // Server list
    listModel = new DefaultListModel<>();
    refreshList();

    serverList = new JList<>(listModel);
    serverList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    serverList.setFixedCellHeight(48);
    serverList.setCellRenderer(new ServerListCellRenderer());
    serverList.setFont(new Font(null, Font.PLAIN, 14));
    if (listModel.size() > 0) {
      serverList.setSelectedIndex(0);
    }
    serverList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          handleConnect();
        }
      }
    });

    JScrollPane scrollPane = new JScrollPane(serverList);
    scrollPane.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
    scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
    scrollPane.setPreferredSize(new Dimension(340, 200));
    scrollPane.setMaximumSize(new Dimension(340, 200));
    card.add(scrollPane);
    card.add(Box.createVerticalStrut(10));

    // Action buttons row: Add / Edit / Delete
    JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
    actionRow.setOpaque(false);
    actionRow.setAlignmentX(Component.CENTER_ALIGNMENT);

    addButton = createSmallButton("+ Add");
    editButton = createSmallButton("Edit");
    deleteButton = createSmallButton("Delete");

    addButton.addActionListener(e -> handleAdd());
    editButton.addActionListener(e -> handleEdit());
    deleteButton.addActionListener(e -> handleDelete());

    actionRow.add(addButton);
    actionRow.add(editButton);
    actionRow.add(deleteButton);
    card.add(actionRow);
    card.add(Box.createVerticalStrut(18));

    // Status label
    statusLabel = new JLabel(" ");
    statusLabel.setFont(new Font(null, Font.PLAIN, 12));
    statusLabel.setForeground(new Color(200, 60, 60));
    statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    card.add(statusLabel);
    card.add(Box.createVerticalStrut(10));

    // Connect button
    connectButton = new JButton("Connect");
    connectButton.setFont(new Font(null, Font.BOLD, 14));
    connectButton.setFocusable(false);
    connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    connectButton.setMaximumSize(new Dimension(340, 36));
    connectButton.setPreferredSize(new Dimension(340, 36));
    connectButton.setBackground(new Color(0x63, 0x66, 0xF1));
    connectButton.setForeground(Color.WHITE);
    connectButton.setOpaque(true);
    connectButton.setBorderPainted(false);
    connectButton.addActionListener(e -> handleConnect());
    card.add(connectButton);

    centerWrapper.add(card);
    add(centerWrapper, BorderLayout.CENTER);
  }

  // =========================================================================
  // Event handlers
  // =========================================================================

  private void handleConnect() {
    int index = serverList.getSelectedIndex();
    if (index < 0) {
      setStatus("Please select a server", true);
      return;
    }
    ServerInfo info = listModel.get(index);
    setStatus("Connecting to " + info.getHost() + ":" + info.getPort() + "...", false);
    connectButton.setEnabled(false);

    // Kết nối trong thread riêng để không block UI
    new SwingWorker<Boolean, Void>() {
      private String errorMessage;

      @Override
      protected Boolean doInBackground() {
        try {
          mainFrame.connectToServer(info);
          return true;
        } catch (Exception ex) {
          errorMessage = ex.getMessage();
          return false;
        }
      }

      @Override
      protected void done() {
        try {
          if (get()) {
            setStatus(" ", false);
          } else {
            setStatus("Connection failed: " + errorMessage, true);
          }
        } catch (Exception ex) {
          setStatus("Connection failed", true);
        }
        connectButton.setEnabled(true);
      }
    }.execute();
  }

  private void handleAdd() {
    ServerInfoDialog dialog = new ServerInfoDialog(
        SwingUtilities.getWindowAncestor(this), "Add Server", null);
    dialog.setVisible(true);
    ServerInfo result = dialog.getResult();
    if (result != null) {
      configManager.addServer(result);
      refreshList();
      serverList.setSelectedIndex(listModel.size() - 1);
    }
  }

  private void handleEdit() {
    int index = serverList.getSelectedIndex();
    if (index < 0) return;
    ServerInfo current = listModel.get(index);
    ServerInfoDialog dialog = new ServerInfoDialog(
        SwingUtilities.getWindowAncestor(this), "Edit Server", current);
    dialog.setVisible(true);
    ServerInfo result = dialog.getResult();
    if (result != null) {
      configManager.updateServer(index, result);
      refreshList();
      serverList.setSelectedIndex(index);
    }
  }

  private void handleDelete() {
    int index = serverList.getSelectedIndex();
    if (index < 0) return;
    ServerInfo info = listModel.get(index);
    int choice = JOptionPane.showConfirmDialog(this,
        "Delete server \"" + info.getName() + "\"?",
        "Delete Server", JOptionPane.YES_NO_OPTION);
    if (choice == JOptionPane.YES_OPTION) {
      configManager.removeServer(index);
      refreshList();
      if (listModel.size() > 0) {
        serverList.setSelectedIndex(Math.min(index, listModel.size() - 1));
      }
    }
  }

  // =========================================================================
  // Helpers
  // =========================================================================

  private void refreshList() {
    listModel.clear();
    for (ServerInfo info : configManager.getServers()) {
      listModel.addElement(info);
    }
  }

  private void setStatus(String text, boolean isError) {
    statusLabel.setText(text);
    statusLabel.setForeground(isError ? new Color(200, 60, 60) : new Color(100, 100, 100));
  }

  private static JButton createSmallButton(String text) {
    JButton button = new JButton(text);
    button.setFont(new Font(null, Font.PLAIN, 12));
    button.setFocusable(false);
    button.setBackground(new Color(245, 245, 245));
    button.setBorderPainted(false);
    button.setPreferredSize(new Dimension(80, 28));
    return button;
  }

  // =========================================================================
  // Inner classes
  // =========================================================================

  /** Cell renderer cho server list. */
  private static class ServerListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (value instanceof ServerInfo) {
        ServerInfo info = (ServerInfo) value;
        setText("<html><b>" + info.getName() + "</b><br>"
            + "<span style='color:#888888;font-size:11px'>"
            + info.getHost() + ":" + info.getPort()
            + "</span></html>");
      }
      setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
      return this;
    }
  }

  /** Dialog thêm/sửa thông tin server. */
  private static class ServerInfoDialog extends JDialog {
    private ServerInfo result;

    ServerInfoDialog(Window owner, String title, ServerInfo existing) {
      super(owner, title, ModalityType.APPLICATION_MODAL);
      setLayout(new BorderLayout(12, 12));
      getRootPane().setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

      JPanel form = new JPanel(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.anchor = GridBagConstraints.WEST;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.insets = new Insets(4, 0, 4, 8);

      JTextField nameField = new JTextField(existing != null ? existing.getName() : "", 20);
      JTextField hostField = new JTextField(existing != null ? existing.getHost() : "", 20);
      JTextField portField = new JTextField(existing != null ? String.valueOf(existing.getPort()) : "4321", 6);

      gbc.gridy = 0;
      form.add(new JLabel("Name:"), gbc);
      gbc.gridy = 1;
      form.add(nameField, gbc);
      gbc.gridy = 2;
      form.add(new JLabel("Host:"), gbc);
      gbc.gridy = 3;
      form.add(hostField, gbc);
      gbc.gridy = 4;
      form.add(new JLabel("Port:"), gbc);
      gbc.gridy = 5;
      form.add(portField, gbc);

      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
      JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(e -> dispose());
      JButton saveButton = new JButton("Save");
      saveButton.setBackground(new Color(0x63, 0x66, 0xF1));
      saveButton.setForeground(Color.WHITE);
      saveButton.setOpaque(true);
      saveButton.setBorderPainted(false);
      saveButton.addActionListener(e -> {
        String name = nameField.getText().trim();
        String host = hostField.getText().trim();
        String portText = portField.getText().trim();
        if (name.isEmpty() || host.isEmpty()) {
          JOptionPane.showMessageDialog(this, "Name and Host are required",
              "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }
        try {
          int port = Integer.parseInt(portText);
          if (port < 1 || port > 65535) throw new NumberFormatException();
          result = new ServerInfo(name, host, port);
          dispose();
        } catch (NumberFormatException ex) {
          JOptionPane.showMessageDialog(this, "Invalid port (1-65535)",
              "Error", JOptionPane.ERROR_MESSAGE);
        }
      });
      buttonPanel.add(cancelButton);
      buttonPanel.add(saveButton);

      add(form, BorderLayout.CENTER);
      add(buttonPanel, BorderLayout.SOUTH);
      setSize(340, 280);
      setLocationRelativeTo(owner);
    }

    ServerInfo getResult() {
      return result;
    }
  }
}
