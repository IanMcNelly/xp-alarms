package com.gullesurgames.xpalarm;

import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.runelite.api.Skill;
import com.gullesurgames.xpalarm.model.AlarmMode;
import com.gullesurgames.xpalarm.model.SoundType;
import com.gullesurgames.xpalarm.model.XpAlarmTarget;
import com.gullesurgames.xpalarm.util.AlarmSerialization;
import org.junit.Assert;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;

public class AlarmImportExportEdgeCasesTest
{
	private AlarmSerialization alarmSerialization;

	@Before
	public void setUp()
	{
		alarmSerialization = new AlarmSerialization(new Gson());
	}
	@Test
	public void testImportFromBase64NullAndWhitespace()
	{
		// Null input returns empty list
		List<XpAlarmTarget> fromNull = alarmSerialization.importFromBase64(null);
		Assert.assertNotNull(fromNull);
		Assert.assertTrue(fromNull.isEmpty());

		// Empty string returns empty list
		List<XpAlarmTarget> fromEmpty = alarmSerialization.importFromBase64("");
		Assert.assertNotNull(fromEmpty);
		Assert.assertTrue(fromEmpty.isEmpty());

		// Whitespace only returns empty list
		List<XpAlarmTarget> fromSpaces = alarmSerialization.importFromBase64("    ");
		Assert.assertNotNull(fromSpaces);
		Assert.assertTrue(fromSpaces.isEmpty());

		// Tab and newline whitespace returns empty list
		List<XpAlarmTarget> fromWhitespace = alarmSerialization.importFromBase64("\t\r\n ");
		Assert.assertNotNull(fromWhitespace);
		Assert.assertTrue(fromWhitespace.isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testImportFromBase64CorruptedPayloadThrows()
	{
		alarmSerialization.importFromBase64("Not Valid Base64!@#$*&");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testImportFromBase64InvalidPaddingThrows()
	{
		alarmSerialization.importFromBase64("YWJjZA===");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testImportFromBase64SpecialCharactersThrows()
	{
		alarmSerialization.importFromBase64("???***###!!!");
	}

	@Test
	public void testImportFromBase64ValidBase64MalformedJsonGraceful()
	{
		// Valid Base64 encoding of non-JSON string
		String malformedJson = "{broken json syntax: [not-closed";
		String b64 = Base64.getEncoder().encodeToString(malformedJson.getBytes(StandardCharsets.UTF_8));

		List<XpAlarmTarget> result = alarmSerialization.importFromBase64(b64);
		Assert.assertNotNull(result);
		Assert.assertTrue(result.isEmpty());

		// Valid Base64 encoding of plain scalar string
		String scalarJson = "\"just a plain string\"";
		String b64Scalar = Base64.getEncoder().encodeToString(scalarJson.getBytes(StandardCharsets.UTF_8));

		List<XpAlarmTarget> scalarResult = alarmSerialization.importFromBase64(b64Scalar);
		Assert.assertNotNull(scalarResult);
		Assert.assertTrue(scalarResult.isEmpty());

		// Valid Base64 encoding of JSON object instead of array
		String objJson = "{\"field\": \"value\"}";
		String b64Obj = Base64.getEncoder().encodeToString(objJson.getBytes(StandardCharsets.UTF_8));

		List<XpAlarmTarget> objResult = alarmSerialization.importFromBase64(b64Obj);
		Assert.assertNotNull(objResult);
		Assert.assertTrue(objResult.isEmpty());
	}

	@Test
	public void testExportAndImportEmptyAndNullList()
	{
		// Exporting null yields valid Base64 of empty array
		String b64Null = alarmSerialization.exportToBase64(null);
		Assert.assertNotNull(b64Null);
		List<XpAlarmTarget> fromNullExport = alarmSerialization.importFromBase64(b64Null);
		Assert.assertNotNull(fromNullExport);
		Assert.assertTrue(fromNullExport.isEmpty());

		// Exporting empty list yields valid Base64 of empty array
		String b64Empty = alarmSerialization.exportToBase64(Collections.emptyList());
		Assert.assertNotNull(b64Empty);
		List<XpAlarmTarget> fromEmptyExport = alarmSerialization.importFromBase64(b64Empty);
		Assert.assertNotNull(fromEmptyExport);
		Assert.assertTrue(fromEmptyExport.isEmpty());
	}

	@Test
	public void testDiverseMultiAlarmRoundTrip()
	{
		List<XpAlarmTarget> originalAlarms = new ArrayList<>();

		// 1. Agility alarm with transparent green flash and built-in sound
		originalAlarms.add(XpAlarmTarget.builder()
			.id(UUID.randomUUID())
			.skill(Skill.AGILITY)
			.mode(AlarmMode.REMAINING_XP_TO_LEVEL)
			.thresholdValue(250)
			.flashEnabled(true)
			.flashColor(new Color(0, 255, 128, 180))
			.soundEnabled(true)
			.soundType(SoundType.BUILTIN)
			.soundId(2266)
			.customSoundFileName("alarm.wav")
			.systemAlertEnabled(true)
			.chatMessageEnabled(true)
			.active(true)
			.triggered(false)
			.build());

		// 2. Runecraft 99 absolute XP alarm with custom sound and flash disabled
		originalAlarms.add(XpAlarmTarget.builder()
			.id(UUID.randomUUID())
			.skill(Skill.RUNECRAFT)
			.mode(AlarmMode.ABSOLUTE_XP)
			.thresholdValue(13034431)
			.flashEnabled(false)
			.flashColor(new Color(255, 0, 0, 140))
			.soundEnabled(true)
			.soundType(SoundType.CUSTOM)
			.soundId(0)
			.customSoundFileName("runecraft_99_ding.wav")
			.systemAlertEnabled(false)
			.chatMessageEnabled(true)
			.active(true)
			.triggered(false)
			.build());

		// 3. Magic 50k remaining alarm with no sound, only system alert and chat message
		originalAlarms.add(XpAlarmTarget.builder()
			.id(UUID.randomUUID())
			.skill(Skill.MAGIC)
			.mode(AlarmMode.REMAINING_XP_TO_LEVEL)
			.thresholdValue(50000)
			.flashEnabled(true)
			.flashColor(new Color(64, 128, 255, 220))
			.soundEnabled(false)
			.soundType(SoundType.BUILTIN)
			.soundId(2266)
			.customSoundFileName("alarm.wav")
			.systemAlertEnabled(true)
			.chatMessageEnabled(true)
			.active(false)
			.triggered(true)
			.build());

		// 4. Farming 200M XP target with semi-transparent amber flash
		originalAlarms.add(XpAlarmTarget.builder()
			.id(UUID.randomUUID())
			.skill(Skill.FARMING)
			.mode(AlarmMode.ABSOLUTE_XP)
			.thresholdValue(200000000)
			.flashEnabled(true)
			.flashColor(new Color(255, 191, 0, 100))
			.soundEnabled(true)
			.soundType(SoundType.CUSTOM)
			.soundId(1234)
			.customSoundFileName("farming_cheer.wav")
			.systemAlertEnabled(true)
			.chatMessageEnabled(false)
			.active(true)
			.triggered(false)
			.build());

		// 5. Hitpoints 1 XP remaining final-hit alarm
		originalAlarms.add(XpAlarmTarget.builder()
			.id(UUID.randomUUID())
			.skill(Skill.HITPOINTS)
			.mode(AlarmMode.REMAINING_XP_TO_LEVEL)
			.thresholdValue(1)
			.flashEnabled(true)
			.flashColor(new Color(255, 50, 50, 255))
			.soundEnabled(true)
			.soundType(SoundType.BUILTIN)
			.soundId(3924)
			.customSoundFileName("alarm.wav")
			.systemAlertEnabled(false)
			.chatMessageEnabled(true)
			.active(true)
			.triggered(false)
			.build());

		// Export
		String exportedBase64 = alarmSerialization.exportToBase64(originalAlarms);
		Assert.assertNotNull(exportedBase64);
		Assert.assertFalse(exportedBase64.isEmpty());

		// Import
		List<XpAlarmTarget> importedAlarms = alarmSerialization.importFromBase64(exportedBase64);
		Assert.assertNotNull(importedAlarms);
		Assert.assertEquals(originalAlarms.size(), importedAlarms.size());

		// Verify every alarm and its properties
		for (int i = 0; i < originalAlarms.size(); i++)
		{
			XpAlarmTarget expected = originalAlarms.get(i);
			XpAlarmTarget actual = importedAlarms.get(i);

			Assert.assertEquals("ID mismatch at index " + i, expected.getId(), actual.getId());
			Assert.assertEquals("Skill mismatch at index " + i, expected.getSkill(), actual.getSkill());
			Assert.assertEquals("Mode mismatch at index " + i, expected.getMode(), actual.getMode());
			Assert.assertEquals("Threshold mismatch at index " + i, expected.getThresholdValue(), actual.getThresholdValue());
			Assert.assertEquals("FlashEnabled mismatch at index " + i, expected.isFlashEnabled(), actual.isFlashEnabled());
			Assert.assertEquals("FlashColor RGB mismatch at index " + i, expected.getFlashColor().getRGB(), actual.getFlashColor().getRGB());
			Assert.assertEquals("SoundEnabled mismatch at index " + i, expected.isSoundEnabled(), actual.isSoundEnabled());
			Assert.assertEquals("SoundType mismatch at index " + i, expected.getSoundType(), actual.getSoundType());
			Assert.assertEquals("SoundId mismatch at index " + i, expected.getSoundId(), actual.getSoundId());
			Assert.assertEquals("CustomSoundFileName mismatch at index " + i, expected.getCustomSoundFileName(), actual.getCustomSoundFileName());
			Assert.assertEquals("SystemAlertEnabled mismatch at index " + i, expected.isSystemAlertEnabled(), actual.isSystemAlertEnabled());
			Assert.assertEquals("ChatMessageEnabled mismatch at index " + i, expected.isChatMessageEnabled(), actual.isChatMessageEnabled());
			Assert.assertEquals("Active mismatch at index " + i, expected.isActive(), actual.isActive());
			Assert.assertEquals("Triggered mismatch at index " + i, expected.isTriggered(), actual.isTriggered());
		}
	}
}
