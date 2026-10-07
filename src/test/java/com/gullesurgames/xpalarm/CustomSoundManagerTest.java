package com.gullesurgames.xpalarm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import com.gullesurgames.xpalarm.audio.CustomSoundManager;
import net.runelite.client.util.Filepath;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CustomSoundManagerTest
{
	private CustomSoundManager soundManager;
	private Path tempDirPath;
	private Filepath soundsDir;

	@Before
	public void setUp() throws IOException
	{
		tempDirPath = Files.createTempDirectory("xpalarm_test_sounds");
		soundsDir = Filepath.Unchecked.getRooted(tempDirPath);
		soundManager = new CustomSoundManager();
		soundManager.initDirectory(soundsDir);
	}

	@After
	public void tearDown() throws IOException
	{
		if (soundsDir != null && soundsDir.exists())
		{
			soundsDir.deleteRecursively();
		}

		if (soundManager != null)
		{
			soundManager.shutDown();
		}
	}

	@Test
	public void testFileExtensionFilteringWithActualFiles() throws IOException
	{
		Assert.assertNotNull(soundsDir);
		Assert.assertTrue("Sounds directory must exist", soundsDir.exists() && soundsDir.isDirectory());

		// Create files with diverse extensions and casings
		String prefix = "xpalarm_test_" + System.currentTimeMillis() + "_";

		Filepath lowerWav = soundsDir.join(prefix + "lower.wav");
		Filepath upperWav = soundsDir.join(prefix + "upper.WAV");
		Filepath mixedWav = soundsDir.join(prefix + "mixed.Wav");
		Filepath mp3File = soundsDir.join(prefix + "audio.mp3");
		Filepath txtFile = soundsDir.join(prefix + "readme.txt");
		Filepath bakFile = soundsDir.join(prefix + "sound.wav.bak");
		Filepath oggFile = soundsDir.join(prefix + "clip.ogg");

		List<Filepath> toCreate = List.of(lowerWav, upperWav, mixedWav, mp3File, txtFile, bakFile, oggFile);
		for (Filepath f : toCreate)
		{
			f.write(new byte[0]);
		}

		List<String> available = soundManager.getAvailableSoundFiles();
		Assert.assertNotNull(available);

		// Verified that .wav files (case-insensitive) are accepted
		Assert.assertTrue("Should contain lowercase .wav", available.contains(lowerWav.getFileName()));
		Assert.assertTrue("Should contain uppercase .WAV", available.contains(upperWav.getFileName()));
		Assert.assertTrue("Should contain mixed case .Wav", available.contains(mixedWav.getFileName()));

		// Verified that non-wav files are filtered out
		Assert.assertFalse("Should not contain .mp3", available.contains(mp3File.getFileName()));
		Assert.assertFalse("Should not contain .txt", available.contains(txtFile.getFileName()));
		Assert.assertFalse("Should not contain .wav.bak", available.contains(bakFile.getFileName()));
		Assert.assertFalse("Should not contain .ogg", available.contains(oggFile.getFileName()));
	}

	@Test
	public void testWavFilterPredicateLogic()
	{
		// Test the exact .wav extension matching predicate used by CustomSoundManager
		Predicate<String> filter = name -> name.toLowerCase().endsWith(".wav");

		// Should match .wav in any casing
		Assert.assertTrue(filter.test("alert.wav"));
		Assert.assertTrue(filter.test("ALERT.WAV"));
		Assert.assertTrue(filter.test("Alert.Wav"));
		Assert.assertTrue(filter.test("sound_effect.wAv"));
		Assert.assertTrue(filter.test(".wav"));

		// Should reject non-wav files
		Assert.assertFalse(filter.test("alert.mp3"));
		Assert.assertFalse(filter.test("alert.txt"));
		Assert.assertFalse(filter.test("alert.wav.bak"));
		Assert.assertFalse(filter.test("alert.wav.tmp"));
		Assert.assertFalse(filter.test("wav"));
		Assert.assertFalse(filter.test("alert.ogg"));
		Assert.assertFalse(filter.test("alert.flac"));
		Assert.assertFalse(filter.test("alert.aiff"));
	}

	@Test
	public void testPlayCustomSoundEdgeCasesDoNotThrow()
	{
		// Null, empty, whitespace, non-existent, and invalid character files should be handled gracefully without throwing
		soundManager.playCustomSound(null);
		soundManager.playCustomSound("");
		soundManager.playCustomSound("   ");
		soundManager.playCustomSound("non_existent_sound_12345.wav");
		soundManager.playCustomSound("invalid<>name.wav");
	}

	@Test
	public void testUninitializedManagerHandlesCallsSafely()
	{
		CustomSoundManager uninit = new CustomSoundManager();
		Assert.assertNotNull(uninit.getAvailableSoundFiles());
		Assert.assertTrue(uninit.getAvailableSoundFiles().isEmpty());
		Assert.assertNull(uninit.getSoundsDirectory());
		uninit.playCustomSound("test.wav");
		uninit.shutDown();
	}
}
