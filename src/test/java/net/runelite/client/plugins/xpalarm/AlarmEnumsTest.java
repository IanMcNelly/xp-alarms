package net.runelite.client.plugins.xpalarm;

import net.runelite.client.plugins.xpalarm.model.AlarmMode;
import net.runelite.client.plugins.xpalarm.model.SoundType;
import org.junit.Assert;
import org.junit.Test;

public class AlarmEnumsTest
{
	@Test
	public void testAlarmModeEnumValuesAndNames()
	{
		AlarmMode[] modes = AlarmMode.values();
		Assert.assertEquals(2, modes.length);

		// ValueOf lookup
		Assert.assertEquals(AlarmMode.ABSOLUTE_XP, AlarmMode.valueOf("ABSOLUTE_XP"));
		Assert.assertEquals(AlarmMode.REMAINING_XP_TO_LEVEL, AlarmMode.valueOf("REMAINING_XP_TO_LEVEL"));

		// Absolute XP
		Assert.assertEquals("Absolute XP", AlarmMode.ABSOLUTE_XP.getDisplayName());
		Assert.assertEquals("Absolute XP", AlarmMode.ABSOLUTE_XP.toString());

		// XP Remaining to Next Level
		Assert.assertEquals("XP Remaining to Next Level", AlarmMode.REMAINING_XP_TO_LEVEL.getDisplayName());
		Assert.assertEquals("XP Remaining to Next Level", AlarmMode.REMAINING_XP_TO_LEVEL.toString());

		// Invariant: toString() matches getDisplayName()
		for (AlarmMode mode : modes)
		{
			Assert.assertEquals(mode.getDisplayName(), mode.toString());
		}
	}

	@Test
	public void testSoundTypeEnumValuesAndNames()
	{
		SoundType[] types = SoundType.values();
		Assert.assertEquals(2, types.length);

		// ValueOf lookup
		Assert.assertEquals(SoundType.BUILTIN, SoundType.valueOf("BUILTIN"));
		Assert.assertEquals(SoundType.CUSTOM, SoundType.valueOf("CUSTOM"));

		// Builtin
		Assert.assertEquals("Built-in Sound Effect", SoundType.BUILTIN.getDisplayName());
		Assert.assertEquals("Built-in Sound Effect", SoundType.BUILTIN.toString());

		// Custom
		Assert.assertEquals("Custom WAV File", SoundType.CUSTOM.getDisplayName());
		Assert.assertEquals("Custom WAV File", SoundType.CUSTOM.toString());

		// Invariant: toString() matches getDisplayName()
		for (SoundType type : types)
		{
			Assert.assertEquals(type.getDisplayName(), type.toString());
		}
	}
}
