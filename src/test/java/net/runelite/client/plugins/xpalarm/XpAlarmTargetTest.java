package net.runelite.client.plugins.xpalarm;

import java.awt.Color;
import java.util.UUID;
import net.runelite.api.Skill;
import net.runelite.client.plugins.xpalarm.model.AlarmMode;
import net.runelite.client.plugins.xpalarm.model.SoundType;
import net.runelite.client.plugins.xpalarm.model.XpAlarmTarget;
import org.junit.Assert;
import org.junit.Test;

public class XpAlarmTargetTest
{
	@Test
	public void testDefaultBuilderValues()
	{
		XpAlarmTarget target = XpAlarmTarget.builder().build();

		Assert.assertNotNull(target.getId());
		Assert.assertEquals(Skill.ATTACK, target.getSkill());
		Assert.assertEquals(AlarmMode.REMAINING_XP_TO_LEVEL, target.getMode());
		Assert.assertEquals(1000, target.getThresholdValue());
		Assert.assertTrue(target.isFlashEnabled());
		Assert.assertEquals(new Color(255, 0, 0, 140), target.getFlashColor());
		Assert.assertTrue(target.isSoundEnabled());
		Assert.assertEquals(SoundType.BUILTIN, target.getSoundType());
		Assert.assertEquals(2266, target.getSoundId());
		Assert.assertEquals("alarm.wav", target.getCustomSoundFileName());
		Assert.assertTrue(target.isSystemAlertEnabled());
		Assert.assertTrue(target.isChatMessageEnabled());
		Assert.assertTrue(target.isActive());
		Assert.assertFalse(target.isTriggered());
	}

	@Test
	public void testNoArgsConstructorDefaults()
	{
		XpAlarmTarget target = new XpAlarmTarget();

		Assert.assertNotNull(target.getId());
		Assert.assertEquals(Skill.ATTACK, target.getSkill());
		Assert.assertEquals(AlarmMode.REMAINING_XP_TO_LEVEL, target.getMode());
		Assert.assertEquals(1000, target.getThresholdValue());
		Assert.assertTrue(target.isFlashEnabled());
		Assert.assertEquals(new Color(255, 0, 0, 140), target.getFlashColor());
		Assert.assertTrue(target.isSoundEnabled());
		Assert.assertEquals(SoundType.BUILTIN, target.getSoundType());
		Assert.assertEquals(2266, target.getSoundId());
		Assert.assertEquals("alarm.wav", target.getCustomSoundFileName());
		Assert.assertTrue(target.isSystemAlertEnabled());
		Assert.assertTrue(target.isChatMessageEnabled());
		Assert.assertTrue(target.isActive());
		Assert.assertFalse(target.isTriggered());
	}

	@Test
	public void testAllArgsConstructor()
	{
		UUID id = UUID.randomUUID();
		Color color = new Color(10, 20, 30, 40);
		XpAlarmTarget target = new XpAlarmTarget(
			id,
			Skill.MINING,
			AlarmMode.ABSOLUTE_XP,
			5000000,
			false,
			color,
			false,
			SoundType.CUSTOM,
			1234,
			"ding.wav",
			false,
			false,
			false,
			true
		);

		Assert.assertEquals(id, target.getId());
		Assert.assertEquals(Skill.MINING, target.getSkill());
		Assert.assertEquals(AlarmMode.ABSOLUTE_XP, target.getMode());
		Assert.assertEquals(5000000, target.getThresholdValue());
		Assert.assertFalse(target.isFlashEnabled());
		Assert.assertEquals(color, target.getFlashColor());
		Assert.assertFalse(target.isSoundEnabled());
		Assert.assertEquals(SoundType.CUSTOM, target.getSoundType());
		Assert.assertEquals(1234, target.getSoundId());
		Assert.assertEquals("ding.wav", target.getCustomSoundFileName());
		Assert.assertFalse(target.isSystemAlertEnabled());
		Assert.assertFalse(target.isChatMessageEnabled());
		Assert.assertFalse(target.isActive());
		Assert.assertTrue(target.isTriggered());
	}

	@Test
	public void testDeepCopyDetachedInstance()
	{
		Color originalColor = new Color(120, 80, 240, 160);
		XpAlarmTarget original = XpAlarmTarget.builder()
			.id(UUID.randomUUID())
			.skill(Skill.AGILITY)
			.mode(AlarmMode.ABSOLUTE_XP)
			.thresholdValue(200000)
			.flashEnabled(true)
			.flashColor(originalColor)
			.soundEnabled(true)
			.soundType(SoundType.CUSTOM)
			.soundId(9999)
			.customSoundFileName("custom_cheer.wav")
			.systemAlertEnabled(false)
			.chatMessageEnabled(true)
			.active(true)
			.triggered(false)
			.build();

		XpAlarmTarget copy = original.deepCopy();

		// Structural equality
		Assert.assertEquals(original, copy);
		Assert.assertNotSame(original, copy);

		// Color detached verification
		Assert.assertEquals(original.getFlashColor(), copy.getFlashColor());
		Assert.assertNotSame(original.getFlashColor(), copy.getFlashColor());

		// Mutating copy fields does not mutate original
		copy.setFlashColor(new Color(0, 255, 0, 255));
		Assert.assertNotEquals(original.getFlashColor(), copy.getFlashColor());
		Assert.assertEquals(originalColor, original.getFlashColor());

		copy.setSkill(Skill.THIEVING);
		Assert.assertNotEquals(original.getSkill(), copy.getSkill());
		Assert.assertEquals(Skill.AGILITY, original.getSkill());

		copy.setThresholdValue(55555);
		Assert.assertNotEquals(original.getThresholdValue(), copy.getThresholdValue());
		Assert.assertEquals(200000, original.getThresholdValue());

		copy.setActive(false);
		Assert.assertNotEquals(original.isActive(), copy.isActive());
		Assert.assertTrue(original.isActive());

		copy.setCustomSoundFileName("modified.wav");
		Assert.assertNotEquals(original.getCustomSoundFileName(), copy.getCustomSoundFileName());
		Assert.assertEquals("custom_cheer.wav", original.getCustomSoundFileName());
	}

	@Test
	public void testEqualsAndHashCodeContract()
	{
		UUID id = UUID.randomUUID();
		Color color = new Color(255, 0, 0, 140);

		XpAlarmTarget a = XpAlarmTarget.builder()
			.id(id)
			.skill(Skill.FISHING)
			.mode(AlarmMode.REMAINING_XP_TO_LEVEL)
			.thresholdValue(12345)
			.flashEnabled(true)
			.flashColor(color)
			.soundEnabled(true)
			.soundType(SoundType.BUILTIN)
			.soundId(2266)
			.customSoundFileName("alarm.wav")
			.systemAlertEnabled(true)
			.chatMessageEnabled(true)
			.active(true)
			.triggered(false)
			.build();

		XpAlarmTarget b = a.deepCopy();

		// Reflexive & Symmetric
		Assert.assertEquals(a, a);
		Assert.assertEquals(a, b);
		Assert.assertEquals(b, a);
		Assert.assertEquals(a.hashCode(), b.hashCode());

		// Non-nullity & Type comparison
		Assert.assertNotNull(a);
		Assert.assertFalse(a.equals(null));
		Assert.assertFalse(a.equals("not-an-alarm-target"));
	}

	@Test
	public void testPropertyMutationsBreakEquals()
	{
		XpAlarmTarget baseline = XpAlarmTarget.builder()
			.id(UUID.randomUUID())
			.skill(Skill.COOKING)
			.mode(AlarmMode.ABSOLUTE_XP)
			.thresholdValue(50000)
			.flashEnabled(true)
			.flashColor(new Color(255, 0, 0, 140))
			.soundEnabled(true)
			.soundType(SoundType.BUILTIN)
			.soundId(2266)
			.customSoundFileName("alarm.wav")
			.systemAlertEnabled(true)
			.chatMessageEnabled(true)
			.active(true)
			.triggered(false)
			.build();

		XpAlarmTarget modified;

		// ID
		modified = baseline.deepCopy();
		modified.setId(UUID.randomUUID());
		Assert.assertNotEquals(baseline, modified);

		// Skill
		modified = baseline.deepCopy();
		modified.setSkill(Skill.CRAFTING);
		Assert.assertNotEquals(baseline, modified);

		// Mode
		modified = baseline.deepCopy();
		modified.setMode(AlarmMode.REMAINING_XP_TO_LEVEL);
		Assert.assertNotEquals(baseline, modified);

		// Threshold
		modified = baseline.deepCopy();
		modified.setThresholdValue(99999);
		Assert.assertNotEquals(baseline, modified);

		// Flash enabled
		modified = baseline.deepCopy();
		modified.setFlashEnabled(!baseline.isFlashEnabled());
		Assert.assertNotEquals(baseline, modified);

		// Flash color
		modified = baseline.deepCopy();
		modified.setFlashColor(new Color(0, 0, 255, 200));
		Assert.assertNotEquals(baseline, modified);

		// Sound enabled
		modified = baseline.deepCopy();
		modified.setSoundEnabled(!baseline.isSoundEnabled());
		Assert.assertNotEquals(baseline, modified);

		// Sound type
		modified = baseline.deepCopy();
		modified.setSoundType(SoundType.CUSTOM);
		Assert.assertNotEquals(baseline, modified);

		// Sound ID
		modified = baseline.deepCopy();
		modified.setSoundId(8888);
		Assert.assertNotEquals(baseline, modified);

		// Custom sound file name
		modified = baseline.deepCopy();
		modified.setCustomSoundFileName("other.wav");
		Assert.assertNotEquals(baseline, modified);

		// System alert enabled
		modified = baseline.deepCopy();
		modified.setSystemAlertEnabled(!baseline.isSystemAlertEnabled());
		Assert.assertNotEquals(baseline, modified);

		// Chat message enabled
		modified = baseline.deepCopy();
		modified.setChatMessageEnabled(!baseline.isChatMessageEnabled());
		Assert.assertNotEquals(baseline, modified);

		// Active
		modified = baseline.deepCopy();
		modified.setActive(!baseline.isActive());
		Assert.assertNotEquals(baseline, modified);

		// Triggered
		modified = baseline.deepCopy();
		modified.setTriggered(!baseline.isTriggered());
		Assert.assertNotEquals(baseline, modified);
	}
}
