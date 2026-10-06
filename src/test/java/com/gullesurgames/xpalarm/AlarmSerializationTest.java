package com.gullesurgames.xpalarm;

import com.google.gson.Gson;
import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.runelite.api.Skill;
import com.gullesurgames.xpalarm.model.AlarmMode;
import com.gullesurgames.xpalarm.model.SoundType;
import com.gullesurgames.xpalarm.model.XpAlarmTarget;
import com.gullesurgames.xpalarm.util.AlarmSerialization;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AlarmSerializationTest
{
	private AlarmSerialization alarmSerialization;

	@Before
	public void setUp()
	{
		alarmSerialization = new AlarmSerialization(new Gson());
	}

	@Test
	public void testSerializationRoundTrip()
	{
		UUID id = UUID.randomUUID();
		XpAlarmTarget target = XpAlarmTarget.builder()
			.id(id)
			.skill(Skill.SLAYER)
			.mode(AlarmMode.REMAINING_XP_TO_LEVEL)
			.thresholdValue(5000)
			.flashEnabled(true)
			.flashColor(new Color(0, 255, 128, 150))
			.soundEnabled(true)
			.soundType(SoundType.CUSTOM)
			.soundId(2266)
			.customSoundFileName("slayer_ding.wav")
			.systemAlertEnabled(true)
			.chatMessageEnabled(true)
			.active(true)
			.triggered(false)
			.build();

		List<XpAlarmTarget> list = Collections.singletonList(target);

		String json = alarmSerialization.toJson(list);
		Assert.assertNotNull(json);

		List<XpAlarmTarget> deserialized = alarmSerialization.fromJson(json);
		Assert.assertEquals(1, deserialized.size());

		XpAlarmTarget restored = deserialized.get(0);
		Assert.assertEquals(target.getId(), restored.getId());
		Assert.assertEquals(target.getSkill(), restored.getSkill());
		Assert.assertEquals(target.getMode(), restored.getMode());
		Assert.assertEquals(target.getThresholdValue(), restored.getThresholdValue());
		Assert.assertEquals(target.getFlashColor().getRGB(), restored.getFlashColor().getRGB());
		Assert.assertEquals(target.getCustomSoundFileName(), restored.getCustomSoundFileName());
	}

	@Test
	public void testBase64ExportAndImport()
	{
		XpAlarmTarget target = XpAlarmTarget.builder()
			.id(UUID.randomUUID())
			.skill(Skill.WOODCUTTING)
			.mode(AlarmMode.ABSOLUTE_XP)
			.thresholdValue(13034431)
			.flashEnabled(false)
			.flashColor(Color.RED)
			.soundEnabled(true)
			.soundType(SoundType.BUILTIN)
			.soundId(2266)
			.customSoundFileName("test.wav")
			.systemAlertEnabled(false)
			.chatMessageEnabled(true)
			.active(true)
			.triggered(true)
			.build();

		String b64 = alarmSerialization.exportToBase64(Collections.singletonList(target));
		Assert.assertNotNull(b64);

		List<XpAlarmTarget> imported = alarmSerialization.importFromBase64(b64);
		Assert.assertEquals(1, imported.size());
		Assert.assertEquals(Skill.WOODCUTTING, imported.get(0).getSkill());
		Assert.assertEquals(13034431, imported.get(0).getThresholdValue());
	}
}
