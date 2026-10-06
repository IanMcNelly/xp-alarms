package com.gullesurgames.xpalarm.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import com.gullesurgames.xpalarm.XpAlarmPlugin;
import com.gullesurgames.xpalarm.model.XpAlarmTarget;
import com.gullesurgames.xpalarm.util.AlarmSerialization;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

/**
 * Main side panel navigation tab for managing XP Alarms.
 */
@Slf4j
public class XpAlarmPanel extends PluginPanel
{
	private static final ImageIcon ADD_ICON;
	private static final ImageIcon ADD_HOVER_ICON;
	private static final ImageIcon IMPORT_ICON;
	private static final ImageIcon IMPORT_HOVER_ICON;
	private static final ImageIcon EXPORT_ICON;
	private static final ImageIcon EXPORT_HOVER_ICON;

	static
	{
		BufferedImage addImg = loadOrGenerate("add_icon.png", XpAlarmPanel::createDefaultAddIcon);
		ADD_ICON = new ImageIcon(addImg);
		ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addImg, 0.53f));

		BufferedImage importImg = loadOrGenerate("import_icon.png", XpAlarmPanel::createDefaultImportIcon);
		IMPORT_ICON = new ImageIcon(importImg);
		IMPORT_HOVER_ICON = new ImageIcon(ImageUtil.recolorImage(importImg, Color.WHITE));

		BufferedImage exportImg = loadOrGenerate("export_icon.png", XpAlarmPanel::createDefaultExportIcon);
		EXPORT_ICON = new ImageIcon(exportImg);
		EXPORT_HOVER_ICON = new ImageIcon(ImageUtil.recolorImage(exportImg, Color.WHITE));
	}

	private final XpAlarmPlugin plugin;

	private final JPanel headerActionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
	private final JPanel alarmsListContainer = new JPanel();
	private final JScrollPane scrollPane;
	private final AlarmEditorDrawer editorDrawer;

	public XpAlarmPanel(XpAlarmPlugin plugin)
	{
		super(false);
		this.plugin = plugin;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Top Section: Title & Header Buttons
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel titleRow = new JPanel(new BorderLayout());
		titleRow.setOpaque(false);

		JLabel titleLabel = new JLabel("XP Alarms");
		titleLabel.setFont(FontManager.getRunescapeBoldFont());
		titleLabel.setForeground(Color.WHITE);
		titleRow.add(titleLabel, BorderLayout.WEST);

		// Header Action Buttons: Import, Export, Add
		headerActionsPanel.setOpaque(false);

		JButton importButton = createHeaderIconButton(IMPORT_ICON, IMPORT_HOVER_ICON, "Import alarms from clipboard (Base64)", this::importFromClipboard);
		JButton exportButton = createHeaderIconButton(EXPORT_ICON, EXPORT_HOVER_ICON, "Export alarms to clipboard (Base64)", this::exportToClipboard);
		JButton addButton = createHeaderIconButton(ADD_ICON, ADD_HOVER_ICON, "Add new alarm", this::showNewAlarmEditor);

		headerActionsPanel.add(importButton);
		headerActionsPanel.add(exportButton);
		headerActionsPanel.add(addButton);
		titleRow.add(headerActionsPanel, BorderLayout.EAST);

		topPanel.add(titleRow, BorderLayout.NORTH);

		// Editor Drawer (collapsible / toggleable)
		editorDrawer = new AlarmEditorDrawer(plugin, this);
		editorDrawer.setVisible(false);
		topPanel.add(editorDrawer, BorderLayout.CENTER);

		add(topPanel, BorderLayout.NORTH);

		// Scrollable List of Alarms
		alarmsListContainer.setLayout(new BoxLayout(alarmsListContainer, BoxLayout.Y_AXIS));
		alarmsListContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		alarmsListContainer.setBorder(new EmptyBorder(6, 6, 6, 6));

		JPanel scrollableContent = new ScrollableContainer();
		scrollableContent.setLayout(new BorderLayout());
		scrollableContent.setOpaque(false);
		scrollableContent.add(alarmsListContainer, BorderLayout.NORTH);

		scrollPane = new JScrollPane(
			scrollableContent,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
		);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		add(scrollPane, BorderLayout.CENTER);
	}

	private static class ScrollableContainer extends JPanel implements javax.swing.Scrollable
	{
		@Override
		public Dimension getPreferredScrollableViewportSize()
		{
			return getPreferredSize();
		}

		@Override
		public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction)
		{
			return 16;
		}

		@Override
		public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction)
		{
			return 32;
		}

		@Override
		public boolean getScrollableTracksViewportWidth()
		{
			return true;
		}

		@Override
		public boolean getScrollableTracksViewportHeight()
		{
			return false;
		}
	}

	private JButton createHeaderIconButton(ImageIcon icon, ImageIcon hoverIcon, String tooltip, Runnable action)
	{
		JButton button = new JButton();
		button.setIcon(icon);
		if (hoverIcon != null)
		{
			button.setRolloverIcon(hoverIcon);
		}
		button.setToolTipText(tooltip);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.setOpaque(false);
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		button.setPreferredSize(new Dimension(20, 20));
		button.addActionListener(e -> action.run());
		button.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				if (hoverIcon != null)
				{
					button.setIcon(hoverIcon);
				}
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				button.setIcon(icon);
			}
		});
		return button;
	}

	private static BufferedImage loadOrGenerate(String resourceName, Supplier<BufferedImage> fallback)
	{
		try
		{
			BufferedImage img = ImageUtil.loadImageResource(XpAlarmPlugin.class, resourceName);
			if (img != null)
			{
				return img;
			}
		}
		catch (Exception ignored)
		{
		}
		return fallback.get();
	}

	private static BufferedImage createDefaultAddIcon()
	{
		BufferedImage img = new BufferedImage(14, 14, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setColor(new Color(66, 165, 66));
		g.fillRect(6, 0, 2, 14);
		g.fillRect(0, 6, 14, 2);
		g.dispose();
		return img;
	}

	private static BufferedImage createDefaultExportIcon()
	{
		BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setColor(new Color(218, 218, 218));
		g.fillRect(1, 3, 10, 1);
		g.fillRect(1, 12, 10, 1);
		g.fillRect(1, 4, 1, 8);
		g.fillRect(10, 4, 1, 2);
		g.fillRect(10, 10, 1, 2);
		g.fillRect(7, 7, 6, 2);
		g.fillRect(12, 6, 1, 4);
		g.fillRect(13, 7, 1, 2);
		g.dispose();
		return img;
	}

	private static BufferedImage createDefaultImportIcon()
	{
		BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setColor(new Color(218, 218, 218));
		g.fillRect(5, 3, 10, 1);
		g.fillRect(5, 12, 10, 1);
		g.fillRect(14, 4, 1, 8);
		g.fillRect(5, 4, 1, 2);
		g.fillRect(5, 10, 1, 2);
		g.fillRect(2, 7, 6, 2);
		g.fillRect(7, 6, 1, 4);
		g.fillRect(8, 7, 1, 2);
		g.dispose();
		return img;
	}

	public void refreshList()
	{
		SwingUtilities.invokeLater(() -> {
			alarmsListContainer.removeAll();
			List<XpAlarmTarget> alarms = plugin.getAlarms();

			if (alarms == null || alarms.isEmpty())
			{
				JPanel emptyPanel = new JPanel();
				emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
				emptyPanel.setOpaque(false);
				emptyPanel.setBorder(new EmptyBorder(25, 10, 10, 10));

				JLabel noAlarmsLabel = new JLabel("No XP alarms configured.");
				noAlarmsLabel.setFont(FontManager.getRunescapeSmallFont());
				noAlarmsLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
				noAlarmsLabel.setAlignmentX(CENTER_ALIGNMENT);

				JLabel hintLabel = new JLabel("Click '+' above to create one!");
				hintLabel.setFont(FontManager.getRunescapeSmallFont());
				hintLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
				hintLabel.setAlignmentX(CENTER_ALIGNMENT);

				emptyPanel.add(noAlarmsLabel);
				emptyPanel.add(Box.createVerticalStrut(4));
				emptyPanel.add(hintLabel);

				alarmsListContainer.add(emptyPanel);
			}
			else
			{
				for (XpAlarmTarget alarm : alarms)
				{
					AlarmCardPanel card = new AlarmCardPanel(plugin, this, alarm);
					alarmsListContainer.add(card);
					alarmsListContainer.add(Box.createVerticalStrut(6));
				}
			}

			alarmsListContainer.revalidate();
			alarmsListContainer.repaint();
		});
	}

	public void showNewAlarmEditor()
	{
		editorDrawer.populateForNew();
		editorDrawer.setVisible(true);
		revalidate();
		repaint();
	}

	public void showEditor(XpAlarmTarget alarm)
	{
		editorDrawer.populateForEdit(alarm);
		editorDrawer.setVisible(true);
		revalidate();
		repaint();
	}

	public void hideEditor()
	{
		editorDrawer.setVisible(false);
		revalidate();
		repaint();
	}

	private void exportToClipboard()
	{
		List<XpAlarmTarget> alarms = plugin.getAlarms();
		if (alarms.isEmpty())
		{
			JOptionPane.showMessageDialog(this, "No alarms configured to export.", "Export Alarms", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		try
		{
			String b64 = AlarmSerialization.exportToBase64(alarms);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(new StringSelection(b64), null);
			JOptionPane.showMessageDialog(this, "Exported " + alarms.size() + " alarm(s) to clipboard as Base64 string!", "Export Successful", JOptionPane.INFORMATION_MESSAGE);
		}
		catch (Exception e)
		{
			log.error("Failed to export alarms to clipboard", e);
			JOptionPane.showMessageDialog(this, "Failed to export alarms: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void importFromClipboard()
	{
		String clipboardText = "";
		try
		{
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor))
			{
				clipboardText = (String) clipboard.getData(DataFlavor.stringFlavor);
			}
		}
		catch (Exception ignored) {}

		JTextArea inputArea = new JTextArea(clipboardText, 5, 25);
		inputArea.setLineWrap(true);
		inputArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(inputArea);

		int option = JOptionPane.showConfirmDialog(
			this,
			scrollPane,
			"Paste Base64 Alarms String",
			JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.PLAIN_MESSAGE
		);

		if (option != JOptionPane.OK_OPTION)
		{
			return;
		}

		String payload = inputArea.getText().trim();
		if (payload.isEmpty())
		{
			return;
		}

		try
		{
			List<XpAlarmTarget> imported = AlarmSerialization.importFromBase64(payload);
			if (imported.isEmpty())
			{
				JOptionPane.showMessageDialog(this, "No valid alarms found in provided payload.", "Import Alarms", JOptionPane.WARNING_MESSAGE);
				return;
			}

			Object[] choices = {"Merge With Existing", "Overwrite All", "Cancel"};
			int mode = JOptionPane.showOptionDialog(
				this,
				"Imported " + imported.size() + " alarm(s). Choose import action:",
				"Import Options",
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				choices,
				choices[0]
			);

			if (mode == 0) // Merge
			{
				plugin.mergeAlarms(imported);
			}
			else if (mode == 1) // Overwrite
			{
				plugin.replaceAlarms(imported);
			}
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(this, "Failed to import alarms: " + ex.getMessage(), "Import Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
