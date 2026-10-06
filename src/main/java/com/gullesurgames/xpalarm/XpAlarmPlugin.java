package com.gullesurgames.xpalarm;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import com.gullesurgames.xpalarm.audio.CustomSoundManager;
import com.gullesurgames.xpalarm.model.AlarmMode;
import com.gullesurgames.xpalarm.model.SoundType;
import com.gullesurgames.xpalarm.model.XpAlarmTarget;
import com.gullesurgames.xpalarm.ui.XpAlarmPanel;
import com.gullesurgames.xpalarm.util.AlarmSerialization;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@Singleton
@PluginDescriptor(
	name = "XP Alarm",
	description = "Customizable XP and level thresholds with screen flashes, sounds, and notifications",
	tags = {"xp", "experience", "level", "alarm", "flash", "sound", "notify"}
)
public class XpAlarmPlugin extends Plugin
{
	@Inject
	@Getter
	private Client client;

	@Inject
	@Getter
	private ClientThread clientThread;

	@Inject
	private XpAlarmConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private XpAlarmOverlay overlay;

	@Inject
	@Getter
	private CustomSoundManager customSoundManager;

	@Inject
	private Notifier notifier;

	@Inject
	private ChatMessageManager chatMessageManager;

	private XpAlarmPanel panel;
	private NavigationButton navButton;

	@Getter
	private final List<XpAlarmTarget> alarms = Collections.synchronizedList(new ArrayList<>());
	private final Set<UUID> firedAlarmsSessionCache = ConcurrentHashMap.newKeySet();

	@Provides
	XpAlarmConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(XpAlarmConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		log.info("Starting XP Alarm plugin...");

		// Load alarms from configuration
		loadAlarms();

		// Ensure audio directories are initialized
		customSoundManager.initDirectory();

		// Register overlay
		overlayManager.add(overlay);

		// Initialize Swing UI Panel
		panel = new XpAlarmPanel(this);
		panel.refreshList();

		BufferedImage icon = createNavIcon();
		navButton = NavigationButton.builder()
			.tooltip("XP Alarms")
			.icon(icon)
			.priority(7)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Stopping XP Alarm plugin...");

		if (navButton != null)
		{
			clientToolbar.removeNavigation(navButton);
			navButton = null;
		}

		overlayManager.remove(overlay);
		overlay.reset();
		customSoundManager.shutDown();

		alarms.clear();
		firedAlarmsSessionCache.clear();
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		Skill skill = statChanged.getSkill();
		int currentXp = statChanged.getXp();
		int currentLevel = statChanged.getLevel();

		// Calculate XP remaining to the next level
		int nextLevel = Math.min(Experience.MAX_VIRT_LEVEL, currentLevel + 1);
		int targetLevelXp = Experience.getXpForLevel(nextLevel);
		int remainingXpToNextLevel = Math.max(0, targetLevelXp - currentXp);

		boolean configNeedsSave = false;

		synchronized (alarms)
		{
			for (XpAlarmTarget alarm : alarms)
			{
				if (!alarm.isActive() || alarm.isTriggered() || alarm.getSkill() != skill)
				{
					continue;
				}

				if (firedAlarmsSessionCache.contains(alarm.getId()))
				{
					continue;
				}

				boolean conditionMet = false;

				if (alarm.getMode() == AlarmMode.ABSOLUTE_XP)
				{
					if (currentXp >= alarm.getThresholdValue())
					{
						conditionMet = true;
					}
				}
				else if (alarm.getMode() == AlarmMode.REMAINING_XP_TO_LEVEL)
				{
					if (remainingXpToNextLevel <= alarm.getThresholdValue())
					{
						conditionMet = true;
					}
				}

				if (conditionMet)
				{
					fireAlarm(alarm, currentXp, remainingXpToNextLevel);
					alarm.setTriggered(true);
					firedAlarmsSessionCache.add(alarm.getId());
					configNeedsSave = true;
				}
			}
		}

		if (configNeedsSave)
		{
			saveAlarms();
			if (panel != null)
			{
				panel.refreshList();
			}
		}
	}

	private void fireAlarm(XpAlarmTarget alarm, int currentXp, int remainingXp)
	{
		log.info("XP Alarm triggered for skill {} (mode: {}, threshold: {})",
			alarm.getSkill(), alarm.getMode(), alarm.getThresholdValue());

		// 1. Screen Flash Alert
		if (alarm.isFlashEnabled())
		{
			overlay.triggerFlash(alarm.getFlashColor(), config.flashDurationFrames());
		}

		// 2. Audio Alert
		if (alarm.isSoundEnabled())
		{
			if (alarm.getSoundType() == SoundType.BUILTIN)
			{
				clientThread.invokeLater(() -> client.playSoundEffect(alarm.getSoundId()));
			}
			else if (alarm.getSoundType() == SoundType.CUSTOM && config.enableCustomSounds())
			{
				customSoundManager.playCustomSound(alarm.getCustomSoundFileName());
			}
		}

		// 3. Desktop / Tray Notification
		if (alarm.isSystemAlertEnabled())
		{
			String msg = String.format("XP Alarm: %s reached target threshold! Current XP: %,d",
				alarm.getSkill().getName(), currentXp);
			notifier.notify(msg);
		}

		// 4. Formatted In-Game Chat Message
		if (alarm.isChatMessageEnabled())
		{
			String desc = alarm.getMode() == AlarmMode.ABSOLUTE_XP
				? String.format("Total XP: %,d (Target: %,d)", currentXp, alarm.getThresholdValue())
				: String.format("XP to Next Level: %,d (Target: %,d)", remainingXp, alarm.getThresholdValue());

			String chatMsg = new ChatMessageBuilder()
				.append(ChatColorType.HIGHLIGHT)
				.append("[XP Alarm] ")
				.append(ChatColorType.NORMAL)
				.append(alarm.getSkill().getName() + " alarm reached! " + desc)
				.build();

			chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage(chatMsg)
				.build());
		}
	}

	public void loadAlarms()
	{
		String json = config.alarmsListJson();
		List<XpAlarmTarget> loaded = AlarmSerialization.fromJson(json);
		alarms.clear();
		alarms.addAll(loaded);

		firedAlarmsSessionCache.clear();
		for (XpAlarmTarget alarm : loaded)
		{
			if (alarm.isTriggered())
			{
				firedAlarmsSessionCache.add(alarm.getId());
			}
		}
	}

	public void saveAlarms()
	{
		String json = AlarmSerialization.toJson(alarms);
		configManager.setConfiguration(XpAlarmConfig.CONFIG_GROUP, XpAlarmConfig.ALARMS_LIST_KEY, json);
	}

	public void addAlarm(XpAlarmTarget alarm)
	{
		alarms.add(alarm);
		saveAlarms();
		if (panel != null)
		{
			panel.refreshList();
		}
	}

	public void updateAlarm(XpAlarmTarget updated)
	{
		synchronized (alarms)
		{
			for (int i = 0; i < alarms.size(); i++)
			{
				if (alarms.get(i).getId().equals(updated.getId()))
				{
					alarms.set(i, updated);
					// Reset fired session cache if alarm was edited
					firedAlarmsSessionCache.remove(updated.getId());
					break;
				}
			}
		}
		saveAlarms();
		if (panel != null)
		{
			panel.refreshList();
		}
	}

	public void deleteAlarm(UUID id)
	{
		alarms.removeIf(a -> a.getId().equals(id));
		firedAlarmsSessionCache.remove(id);
		saveAlarms();
	}

	public void rearmAlarm(UUID id)
	{
		firedAlarmsSessionCache.remove(id);
		synchronized (alarms)
		{
			for (XpAlarmTarget target : alarms)
			{
				if (target.getId().equals(id))
				{
					target.setTriggered(false);
					target.setActive(true);
					break;
				}
			}
		}
		saveAlarms();
	}

	public void mergeAlarms(List<XpAlarmTarget> newAlarms)
	{
		synchronized (alarms)
		{
			for (XpAlarmTarget na : newAlarms)
			{
				// Generate new UUID to prevent collision
				na.setId(UUID.randomUUID());
				na.setTriggered(false);
				alarms.add(na);
			}
		}
		saveAlarms();
		if (panel != null)
		{
			panel.refreshList();
		}
	}

	public void replaceAlarms(List<XpAlarmTarget> newAlarms)
	{
		synchronized (alarms)
		{
			alarms.clear();
			firedAlarmsSessionCache.clear();
			alarms.addAll(newAlarms);
		}
		saveAlarms();
		if (panel != null)
		{
			panel.refreshList();
		}
	}

	/**
	 * Generates a clean 16x16 bell/alarm icon procedurally with "XP" drawn on top
	 * for the side panel navigation button.
	 */
	private BufferedImage createNavIcon()
	{
		BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Gold/Orange alarm bell
		g.setColor(new Color(230, 160, 40));
		g.fillArc(2, 2, 12, 11, 0, 180);
		g.fillRect(3, 7, 10, 4);

		// Bell rim
		g.setColor(new Color(250, 190, 60));
		g.fillRoundRect(1, 10, 14, 3, 2, 2);

		// Clapper
		g.setColor(new Color(200, 130, 20));
		g.fillOval(6, 12, 4, 3);

		// Top loop
		g.setColor(new Color(250, 190, 60));
		g.drawOval(6, 1, 3, 2);

		// Draw crisp "XP" lettering on the bell body
		g.setColor(new Color(28, 18, 5));
		// 'X' at x=4..6, y=4..8
		g.fillRect(4, 4, 1, 2);
		g.fillRect(6, 4, 1, 2);
		g.fillRect(5, 6, 1, 1);
		g.fillRect(4, 7, 1, 2);
		g.fillRect(6, 7, 1, 2);

		// 'P' at x=9..11, y=4..8
		g.fillRect(9, 4, 1, 5);
		g.fillRect(10, 4, 2, 1);
		g.fillRect(11, 5, 1, 1);
		g.fillRect(10, 6, 2, 1);

		g.dispose();
		return img;
	}
}
