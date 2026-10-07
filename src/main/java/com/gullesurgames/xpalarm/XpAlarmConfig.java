package com.gullesurgames.xpalarm;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(XpAlarmConfig.CONFIG_GROUP)
public interface XpAlarmConfig extends Config
{
	String CONFIG_GROUP = "xpalarm";
	String ALARMS_LIST_KEY = "alarmsListJson";

	@ConfigItem(
		keyName = ALARMS_LIST_KEY,
		name = "Alarms JSON",
		description = "Serialized JSON payload containing all active and inactive XP alarms",
		hidden = true
	)
	default String alarmsListJson()
	{
		return "[]";
	}

	@Range(min = 5, max = 60)
	@ConfigItem(
		keyName = "flashDurationFrames",
		name = "Flash Duration (Frames)",
		description = "Number of client render frames the screen flash overlay remains visible",
		position = 1
	)
	default int flashDurationFrames()
	{
		return 60;
	}

	@ConfigItem(
		keyName = "enableCustomSounds",
		name = "Enable Custom Sounds",
		description = "Enables loading and playing external .wav files from the plugin sounds directory (.runelite/plugin-data/xp-alarms/sounds/)",
		position = 2
	)
	default boolean enableCustomSounds()
	{
		return true;
	}
}
