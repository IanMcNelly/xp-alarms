package net.runelite.client.plugins.xpalarm.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import net.runelite.client.plugins.xpalarm.XpAlarmPlugin;
import net.runelite.client.plugins.xpalarm.model.AlarmMode;
import net.runelite.client.plugins.xpalarm.model.XpAlarmTarget;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

/**
 * Visual card representing a single configured XP Alarm inside the panel list.
 */
public class AlarmCardPanel extends JPanel
{
	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

	private final XpAlarmPlugin plugin;
	private final XpAlarmPanel parentPanel;
	private final XpAlarmTarget alarm;

	public AlarmCardPanel(XpAlarmPlugin plugin, XpAlarmPanel parentPanel, XpAlarmTarget alarm)
	{
		this.plugin = plugin;
		this.parentPanel = parentPanel;
		this.alarm = alarm;

		setLayout(new BorderLayout(5, 5));
		setBackground(alarm.isActive() ? ColorScheme.DARKER_GRAY_COLOR : ColorScheme.DARK_GRAY_COLOR.darker());
		setBorder(new CompoundBorder(
			new LineBorder(alarm.isTriggered() ? ColorScheme.PROGRESS_COMPLETE_COLOR : ColorScheme.BORDER_COLOR, 1),
			new EmptyBorder(8, 8, 8, 8)
		));

		initCardUi();
	}

	private void initCardUi()
	{
		// Top Bar: Skill Name + Active Checkbox
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setOpaque(false);

		JLabel skillLabel = new JLabel(alarm.getSkill().getName().toUpperCase());
		skillLabel.setFont(FontManager.getRunescapeBoldFont());
		skillLabel.setForeground(alarm.isActive() ? Color.WHITE : Color.GRAY);

		JCheckBox activeToggle = new JCheckBox();
		activeToggle.setSelected(alarm.isActive());
		activeToggle.setToolTipText(alarm.isActive() ? "Alarm Active (Click to disable)" : "Alarm Inactive (Click to enable)");
		activeToggle.setOpaque(false);
		activeToggle.addActionListener(e -> {
			alarm.setActive(activeToggle.isSelected());
			plugin.saveAlarms();
			parentPanel.refreshList();
		});

		headerPanel.add(skillLabel, BorderLayout.WEST);
		headerPanel.add(activeToggle, BorderLayout.EAST);
		add(headerPanel, BorderLayout.NORTH);

		// Center: Mode, Threshold, Badges
		JPanel bodyPanel = new JPanel();
		bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
		bodyPanel.setOpaque(false);

		String modeDesc = alarm.getMode() == AlarmMode.ABSOLUTE_XP ? "Absolute XP: " : "Remaining XP: ";
		JLabel thresholdLabel = new JLabel(modeDesc + NUMBER_FORMAT.format(alarm.getThresholdValue()));
		thresholdLabel.setFont(FontManager.getRunescapeSmallFont());
		thresholdLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		thresholdLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		bodyPanel.add(thresholdLabel);

		// Alert indicators row
		JPanel alertsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
		alertsRow.setOpaque(false);
		alertsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

		if (alarm.isFlashEnabled())
		{
			alertsRow.add(createBadge("FLASH", alarm.getFlashColor()));
		}
		if (alarm.isSoundEnabled())
		{
			alertsRow.add(createBadge("AUDIO", ColorScheme.PROGRESS_COMPLETE_COLOR));
		}
		if (alarm.isSystemAlertEnabled())
		{
			alertsRow.add(createBadge("NOTIF", ColorScheme.BRAND_ORANGE));
		}
		if (alarm.isChatMessageEnabled())
		{
			alertsRow.add(createBadge("CHAT", Color.CYAN));
		}

		if (alarm.isTriggered())
		{
			JLabel triggeredBadge = createBadge("TRIGGERED", Color.RED);
			triggeredBadge.setToolTipText("Threshold met. Click Reset to re-arm.");
			alertsRow.add(triggeredBadge);
		}

		bodyPanel.add(Box.createVerticalStrut(4));
		bodyPanel.add(alertsRow);
		add(bodyPanel, BorderLayout.CENTER);

		// Bottom / Actions: Re-arm (if triggered), Edit, Delete
		JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		actionsPanel.setOpaque(false);

		if (alarm.isTriggered())
		{
			JButton rearmButton = createSmallButton("Rearm", "Re-arm this alarm to trigger again");
			rearmButton.addActionListener(e -> {
				alarm.setTriggered(false);
				plugin.rearmAlarm(alarm.getId());
				parentPanel.refreshList();
			});
			actionsPanel.add(rearmButton);
		}

		JButton editButton = createSmallButton("Edit", "Edit alarm parameters");
		editButton.addActionListener(e -> parentPanel.showEditor(alarm));
		actionsPanel.add(editButton);

		JButton deleteButton = createSmallButton("Delete", "Delete this alarm");
		deleteButton.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
		deleteButton.addActionListener(e -> {
			plugin.deleteAlarm(alarm.getId());
			parentPanel.refreshList();
		});
		actionsPanel.add(deleteButton);

		add(actionsPanel, BorderLayout.SOUTH);
	}

	private JLabel createBadge(String text, Color color)
	{
		JLabel badge = new JLabel(text);
		badge.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 9));
		badge.setForeground(Color.WHITE);
		badge.setOpaque(true);
		badge.setBackground(color);
		badge.setBorder(new CompoundBorder(
			new LineBorder(color.darker(), 1),
			new EmptyBorder(1, 3, 1, 3)
		));
		return badge;
	}

	private JButton createSmallButton(String text, String tooltip)
	{
		JButton button = new JButton(text);
		button.setFont(FontManager.getRunescapeSmallFont());
		button.setFocusPainted(false);
		button.setMargin(new java.awt.Insets(1, 3, 1, 3));
		button.setPreferredSize(new Dimension(button.getPreferredSize().width, 22));
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		button.setToolTipText(tooltip);
		return button;
	}
}
