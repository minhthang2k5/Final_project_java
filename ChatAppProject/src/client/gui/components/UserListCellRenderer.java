package client.gui.components;

import javax.swing.*;
import java.awt.*;

public class UserListCellRenderer extends JPanel implements ListCellRenderer<String> {

    private final JLabel avatarLabel;
    private final JLabel nameLabel;
    private final JLabel statusLabel;

    public UserListCellRenderer() {
        setLayout(new BorderLayout(10, 5));
        setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        setOpaque(true);

        avatarLabel = new JLabel(loadIcon("/client/gui/asset/user.png", 22));
        statusLabel = new JLabel();
        nameLabel = new JLabel();
        nameLabel.setFont(new Font(null, Font.BOLD, 14));

        add(avatarLabel, BorderLayout.WEST);
        add(nameLabel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
        nameLabel.setText(formatDisplayText(value, index));
        statusLabel.setIcon(new StatusIcon(Color.GREEN));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));

        if (isSelected) {
            setBackground(new Color(200, 220, 240));
            nameLabel.setForeground(Color.BLACK);
        } else {
            setBackground(list.getBackground());
            nameLabel.setForeground(list.getForeground());
        }

        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        return this;
    }

    private static String formatDisplayText(String value, int index) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }

        String baseName = value.trim();
        String suffix = null;
        int hashIndex = baseName.lastIndexOf('#');
        if (hashIndex > 0 && hashIndex < baseName.length() - 1) {
            suffix = baseName.substring(hashIndex).trim();
            baseName = baseName.substring(0, hashIndex).trim();
        }

        if (suffix == null) {
            suffix = "#" + (index + 1);
        }

        String safeBase = escapeHtml(baseName);
        String safeSuffix = escapeHtml(suffix);
        return "<html>" + safeBase + " <span style='color:#9aa0a6;'>" + safeSuffix + "</span></html>";
    }

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static ImageIcon loadIcon(String path, int size) {
        java.net.URL url = UserListCellRenderer.class.getResource(path);
        if (url == null) {
            return null;
        }
        ImageIcon icon = new ImageIcon(url);
        Image image = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    private static class StatusIcon implements Icon {
        private final Color color;
        private final int size = 12;

        public StatusIcon(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.fillOval(x, y, size, size);
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}