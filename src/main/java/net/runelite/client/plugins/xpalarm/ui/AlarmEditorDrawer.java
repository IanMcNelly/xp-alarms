package net.runelite.client.plugins.xpalarm.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import java.util.UUID;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import net.runelite.api.Skill;
import net.runelite.client.plugins.xpalarm.XpAlarmPlugin;
import net.runelite.client.plugins.xpalarm.model.AlarmMode;
import net.runelite.client.plugins.xpalarm.model.SoundType;
import net.runelite.client.plugins.xpalarm.model.XpAlarmTarget;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

/**
 * Collapsible editor drawer for creating and modifying XP alarms.
 */
public class AlarmEditorDrawer extends JPanel
{
	private final XpAlarmPlugin plugin;
	private final XpAlarmPanel parentPanel;

	private UUID editingAlarmId;

	private final JComboBox<Skill> skillComboBox = new JComboBox<>(Skill.values());
	private final JComboBox<AlarmMode> modeComboBox = new JComboBox<>(AlarmMode.values());
	private final JTextField thresholdInput = new JTextField();

	private final JCheckBox flashCheckBox = new JCheckBox("Screen Flash");
	private final JButton colorPickerButton = new JButton();
	private Color selectedFlashColor = new Color(255, 0, 0, 140);

	private final JCheckBox soundCheckBox = new JCheckBox("Audio Cue");
	private final JComboBox<SoundType> soundTypeComboBox = new JComboBox<>(SoundType.values());
	private final JTextField soundIdInput = new JTextField("2266");
	private final JComboBox<String> customSoundComboBox = new JComboBox<>();

	private final JCheckBox notifierCheckBox = new JCheckBox("System Tray Alert");
	private final JCheckBox chatCheckBox = new JCheckBox("Game Chat Message");

	public AlarmEditorDrawer(XpAlarmPlugin plugin, XpAlarmPanel parentPanel)
	{
		this.plugin = plugin;
		this.parentPanel = parentPanel;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(new CompoundBorder(
			new EmptyBorder(6, 0, 0, 0),
			new CompoundBorder(
				new LineBorder(ColorScheme.BRAND_ORANGE, 1),
				new EmptyBorder(10, 10, 10, 10)
			)
		));

		buildUi();
	}

	private static Component createSpacer(int height)
	{
		JComponent spacer = (JComponent) Box.createVerticalStrut(height);
		spacer.setAlignmentX(LEFT_ALIGNMENT);
		return spacer;
	}

	@Override
	public Dimension getMaximumSize()
	{
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	private void buildUi()
	{
		// Header Label
		JLabel titleLabel = new JLabel("Configure Alarm");
		titleLabel.setFont(FontManager.getRunescapeBoldFont());
		titleLabel.setForeground(ColorScheme.BRAND_ORANGE);
		titleLabel.setAlignmentX(LEFT_ALIGNMENT);
		titleLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, titleLabel.getPreferredSize().height));
		add(titleLabel);
		add(createSpacer(8));

		// Skill Dropdown
		add(createFieldLabel("Skill:"));
		skillComboBox.setAlignmentX(LEFT_ALIGNMENT);
		skillComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
		add(skillComboBox);
		add(createSpacer(6));

		// Mode Dropdown
		add(createFieldLabel("Alarm Mode:"));
		modeComboBox.setAlignmentX(LEFT_ALIGNMENT);
		modeComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
		add(modeComboBox);
		add(createSpacer(6));

		// Threshold Input
		add(createFieldLabel("Threshold (XP):"));
		thresholdInput.setAlignmentX(LEFT_ALIGNMENT);
		thresholdInput.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
		add(thresholdInput);
		add(createSpacer(6));

		// Preset Quick Buttons
		JLabel presetLabel = createFieldLabel("Quick Presets:");
		add(presetLabel);
		JPanel presetsPanel = new JPanel(new GridLayout(1, 3, 4, 4));
		presetsPanel.setOpaque(false);
		presetsPanel.setAlignmentX(LEFT_ALIGNMENT);
		presetsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

		JButton preset1k = new JButton("1k rem");
		preset1k.setFont(FontManager.getRunescapeSmallFont());
		preset1k.setMargin(new Insets(1, 2, 1, 2));
		preset1k.setToolTipText("1,000 XP to next level");
		preset1k.addActionListener(e -> {
			modeComboBox.setSelectedItem(AlarmMode.REMAINING_XP_TO_LEVEL);
			thresholdInput.setText("1000");
		});

		JButton preset10k = new JButton("10k rem");
		preset10k.setFont(FontManager.getRunescapeSmallFont());
		preset10k.setMargin(new Insets(1, 2, 1, 2));
		preset10k.setToolTipText("10,000 XP to next level");
		preset10k.addActionListener(e -> {
			modeComboBox.setSelectedItem(AlarmMode.REMAINING_XP_TO_LEVEL);
			thresholdInput.setText("10000");
		});

		JButton presetMax = new JButton("199.9M");
		presetMax.setFont(FontManager.getRunescapeSmallFont());
		presetMax.setMargin(new Insets(1, 2, 1, 2));
		presetMax.setToolTipText("199,999,000 Absolute XP");
		presetMax.addActionListener(e -> {
			modeComboBox.setSelectedItem(AlarmMode.ABSOLUTE_XP);
			thresholdInput.setText("199999000");
		});

		presetsPanel.add(preset1k);
		presetsPanel.add(preset10k);
		presetsPanel.add(presetMax);
		add(presetsPanel);
		add(createSpacer(10));

		// Alert Configuration Group
		JPanel alertsPanel = new JPanel()
		{
			@Override
			public Dimension getMaximumSize()
			{
				return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
			}
		};
		alertsPanel.setLayout(new BoxLayout(alertsPanel, BoxLayout.Y_AXIS));
		alertsPanel.setOpaque(false);
		alertsPanel.setAlignmentX(LEFT_ALIGNMENT);
		alertsPanel.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR), "Alert Channels",
			TitledBorder.LEFT, TitledBorder.TOP, FontManager.getRunescapeSmallFont(), ColorScheme.LIGHT_GRAY_COLOR
		));

		// Flash row
		JPanel flashRow = new JPanel(new BorderLayout(5, 0));
		flashRow.setOpaque(false);
		flashRow.setAlignmentX(LEFT_ALIGNMENT);
		flashRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
		flashCheckBox.setOpaque(false);
		flashCheckBox.setAlignmentX(LEFT_ALIGNMENT);
		flashRow.add(flashCheckBox, BorderLayout.WEST);

		updateColorPickerButton();
		colorPickerButton.setPreferredSize(new Dimension(30, 20));
		colorPickerButton.addActionListener(e -> {
			Color newColor = JColorChooser.showDialog(this, "Select Flash Color", selectedFlashColor);
			if (newColor != null)
			{
				selectedFlashColor = new Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), 140);
				updateColorPickerButton();
			}
		});
		flashRow.add(colorPickerButton, BorderLayout.EAST);
		alertsPanel.add(flashRow);

		// Sound row & config
		soundCheckBox.setOpaque(false);
		soundCheckBox.setAlignmentX(LEFT_ALIGNMENT);
		soundCheckBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, soundCheckBox.getPreferredSize().height));
		alertsPanel.add(soundCheckBox);

		JPanel soundConfigPanel = new JPanel(new BorderLayout(4, 4));
		soundConfigPanel.setOpaque(false);
		soundConfigPanel.setAlignmentX(LEFT_ALIGNMENT);
		soundConfigPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
		soundTypeComboBox.setPreferredSize(new Dimension(105, 24));
		soundConfigPanel.add(soundTypeComboBox, BorderLayout.WEST);

		soundIdInput.setPreferredSize(new Dimension(50, 24));
		soundIdInput.setToolTipText("RuneLite Sound ID (e.g. 2266)");
		soundConfigPanel.add(soundIdInput, BorderLayout.CENTER);

		customSoundComboBox.setPreferredSize(new Dimension(80, 24));
		customSoundComboBox.setVisible(false);
		soundConfigPanel.add(customSoundComboBox, BorderLayout.EAST);

		alertsPanel.add(soundConfigPanel);

		soundTypeComboBox.addActionListener(e -> {
			boolean isCustom = soundTypeComboBox.getSelectedItem() == SoundType.CUSTOM;
			soundIdInput.setVisible(!isCustom);
			customSoundComboBox.setVisible(isCustom);
			revalidate();
			repaint();
		});

		// Test Audio Button
		JButton testSoundButton = new JButton("Test Audio");
		testSoundButton.setFont(FontManager.getRunescapeSmallFont());
		testSoundButton.setMargin(new Insets(1, 2, 1, 2));
		testSoundButton.setAlignmentX(LEFT_ALIGNMENT);
		testSoundButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
		testSoundButton.addActionListener(e -> {
			if (soundTypeComboBox.getSelectedItem() == SoundType.CUSTOM)
			{
				String selected = (String) customSoundComboBox.getSelectedItem();
				if (selected != null)
				{
					plugin.getCustomSoundManager().playCustomSound(selected);
				}
			}
			else
			{
				try
				{
					int sId = Integer.parseInt(soundIdInput.getText().trim());
					plugin.getClientThread().invokeLater(() -> plugin.getClient().playSoundEffect(sId));
				}
				catch (NumberFormatException ignored) {}
			}
		});
		alertsPanel.add(createSpacer(4));
		alertsPanel.add(testSoundButton);

		// Notifier & Chat
		notifierCheckBox.setOpaque(false);
		notifierCheckBox.setAlignmentX(LEFT_ALIGNMENT);
		notifierCheckBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, notifierCheckBox.getPreferredSize().height));
		chatCheckBox.setOpaque(false);
		chatCheckBox.setAlignmentX(LEFT_ALIGNMENT);
		chatCheckBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, chatCheckBox.getPreferredSize().height));
		alertsPanel.add(notifierCheckBox);
		alertsPanel.add(chatCheckBox);

		add(alertsPanel);
		add(createSpacer(10));

		// Action Buttons: Save & Cancel
		JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 6, 0));
		buttonsPanel.setOpaque(false);
		buttonsPanel.setAlignmentX(LEFT_ALIGNMENT);
		buttonsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

		JButton saveButton = new JButton("Save");
		saveButton.setFont(FontManager.getRunescapeBoldFont());
		saveButton.setMargin(new Insets(2, 4, 2, 4));
		saveButton.setBackground(ColorScheme.PROGRESS_COMPLETE_COLOR.darker());
		saveButton.addActionListener(e -> saveAlarm());

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setFont(FontManager.getRunescapeBoldFont());
		cancelButton.setMargin(new Insets(2, 4, 2, 4));
		cancelButton.addActionListener(e -> parentPanel.hideEditor());

		buttonsPanel.add(saveButton);
		buttonsPanel.add(cancelButton);
		add(buttonsPanel);
	}

	private void updateColorPickerButton()
	{
		colorPickerButton.setBackground(selectedFlashColor);
		colorPickerButton.setOpaque(true);
		colorPickerButton.setBorder(new LineBorder(Color.BLACK, 1));
	}

	private JLabel createFieldLabel(String text)
	{
		JLabel label = new JLabel(text);
		label.setFont(FontManager.getRunescapeSmallFont());
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		label.setAlignmentX(LEFT_ALIGNMENT);
		label.setMaximumSize(new Dimension(Integer.MAX_VALUE, label.getPreferredSize().height));
		return label;
	}

	public void populateForNew()
	{
		this.editingAlarmId = null;
		skillComboBox.setSelectedItem(Skill.ATTACK);
		modeComboBox.setSelectedItem(AlarmMode.REMAINING_XP_TO_LEVEL);
		thresholdInput.setText("1000");

		flashCheckBox.setSelected(true);
		selectedFlashColor = new Color(255, 0, 0, 140);
		updateColorPickerButton();

		soundCheckBox.setSelected(true);
		soundTypeComboBox.setSelectedItem(SoundType.BUILTIN);
		soundIdInput.setText("2266");
		soundIdInput.setVisible(true);

		refreshCustomSoundsList(null);
		customSoundComboBox.setVisible(false);

		notifierCheckBox.setSelected(true);
		chatCheckBox.setSelected(true);
	}

	public void populateForEdit(XpAlarmTarget alarm)
	{
		this.editingAlarmId = alarm.getId();
		skillComboBox.setSelectedItem(alarm.getSkill());
		modeComboBox.setSelectedItem(alarm.getMode());
		thresholdInput.setText(String.valueOf(alarm.getThresholdValue()));

		flashCheckBox.setSelected(alarm.isFlashEnabled());
		selectedFlashColor = alarm.getFlashColor() != null ? alarm.getFlashColor() : new Color(255, 0, 0, 140);
		updateColorPickerButton();

		soundCheckBox.setSelected(alarm.isSoundEnabled());
		soundTypeComboBox.setSelectedItem(alarm.getSoundType());
		soundIdInput.setText(String.valueOf(alarm.getSoundId()));

		boolean isCustom = alarm.getSoundType() == SoundType.CUSTOM;
		soundIdInput.setVisible(!isCustom);
		refreshCustomSoundsList(alarm.getCustomSoundFileName());
		customSoundComboBox.setVisible(isCustom);

		notifierCheckBox.setSelected(alarm.isSystemAlertEnabled());
		chatCheckBox.setSelected(alarm.isChatMessageEnabled());
	}

	private void refreshCustomSoundsList(String selectedName)
	{
		List<String> files = plugin.getCustomSoundManager().getAvailableSoundFiles();
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
		for (String f : files)
		{
			model.addElement(f);
		}
		if (files.isEmpty())
		{
			model.addElement("alarm.wav");
		}
		customSoundComboBox.setModel(model);
		if (selectedName != null && files.contains(selectedName))
		{
			customSoundComboBox.setSelectedItem(selectedName);
		}
	}

	private void saveAlarm()
	{
		int threshold;
		try
		{
			threshold = Integer.parseInt(thresholdInput.getText().replaceAll("[,.]", "").trim());
			if (threshold < 0)
			{
				throw new NumberFormatException();
			}
		}
		catch (NumberFormatException ex)
		{
			JOptionPane.showMessageDialog(this, "Please enter a valid positive number for threshold XP.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
			return;
		}

		int soundId = 2266;
		try
		{
			soundId = Integer.parseInt(soundIdInput.getText().trim());
		}
		catch (NumberFormatException ignored) {}

		String customFile = (String) customSoundComboBox.getSelectedItem();
		if (customFile == null || customFile.isEmpty())
		{
			customFile = "alarm.wav";
		}

		XpAlarmTarget target = XpAlarmTarget.builder()
			.id(editingAlarmId != null ? editingAlarmId : UUID.randomUUID())
			.skill((Skill) skillComboBox.getSelectedItem())
			.mode((AlarmMode) modeComboBox.getSelectedItem())
			.thresholdValue(threshold)
			.flashEnabled(flashCheckBox.isSelected())
			.flashColor(selectedFlashColor)
			.soundEnabled(soundCheckBox.isSelected())
			.soundType((SoundType) soundTypeComboBox.getSelectedItem())
			.soundId(soundId)
			.customSoundFileName(customFile)
			.systemAlertEnabled(notifierCheckBox.isSelected())
			.chatMessageEnabled(chatCheckBox.isSelected())
			.active(true)
			.triggered(false)
			.build();

		if (editingAlarmId != null)
		{
			plugin.updateAlarm(target);
		}
		else
		{
			plugin.addAlarm(target);
		}

		parentPanel.hideEditor();
	}
}
