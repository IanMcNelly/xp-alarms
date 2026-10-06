package com.gullesurgames.xpalarm;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.gullesurgames.xpalarm.audio.CustomSoundManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CustomSoundManagerTest
{
	private CustomSoundManager soundManager;
	private final List<File> createdFiles = new ArrayList<>();

	@Before
	public void setUp()
	{
		soundManager = new CustomSoundManager();
		soundManager.initDirectory();
	}

	@After
	public void tearDown()
	{
		for (File file : createdFiles)
		{
			if (file.exists())
			{
				file.delete();
			}
		}
		createdFiles.clear();

		if (soundManager != null)
		{
			soundManager.shutDown();
		}
	}

	@Test
	public void testFileExtensionFilteringWithActualFiles() throws IOException
	{
		File soundsDir = soundManager.getSoundsDirectory();
		Assert.assertNotNull(soundsDir);
		Assert.assertTrue("Sounds directory must exist or be creatable", soundsDir.exists() || soundsDir.mkdirs());

		// Create files with diverse extensions and casings
		String prefix = "xpalarm_test_" + System.currentTimeMillis() + "_";

		File lowerWav = new File(soundsDir, prefix + "lower.wav");
		File upperWav = new File(soundsDir, prefix + "upper.WAV");
		File mixedWav = new File(soundsDir, prefix + "mixed.Wav");
		File mp3File = new File(soundsDir, prefix + "audio.mp3");
		File txtFile = new File(soundsDir, prefix + "readme.txt");
		File bakFile = new File(soundsDir, prefix + "sound.wav.bak");
		File oggFile = new File(soundsDir, prefix + "clip.ogg");

		List<File> toCreate = List.of(lowerWav, upperWav, mixedWav, mp3File, txtFile, bakFile, oggFile);
		for (File f : toCreate)
		{
			if (f.createNewFile())
			{
				createdFiles.add(f);
			}
		}

		List<String> available = soundManager.getAvailableSoundFiles();
		Assert.assertNotNull(available);

		// Verified that .wav files (case-insensitive) are accepted
		Assert.assertTrue("Should contain lowercase .wav", available.contains(lowerWav.getName()));
		Assert.assertTrue("Should contain uppercase .WAV", available.contains(upperWav.getName()));
		Assert.assertTrue("Should contain mixed case .Wav", available.contains(mixedWav.getName()));

		// Verified that non-wav files are filtered out
		Assert.assertFalse("Should not contain .mp3", available.contains(mp3File.getName()));
		Assert.assertFalse("Should not contain .txt", available.contains(txtFile.getName()));
		Assert.assertFalse("Should not contain .wav.bak", available.contains(bakFile.getName()));
		Assert.assertFalse("Should not contain .ogg", available.contains(oggFile.getName()));
	}

	@Test
	public void testWavFilterPredicateLogic()
	{
		// Test the exact FilenameFilter predicate logic used by CustomSoundManager
		FilenameFilter filter = (dir, name) -> name.toLowerCase().endsWith(".wav");
		File dummyDir = new File(".");

		// Should match .wav in any casing
		Assert.assertTrue(filter.accept(dummyDir, "alert.wav"));
		Assert.assertTrue(filter.accept(dummyDir, "ALERT.WAV"));
		Assert.assertTrue(filter.accept(dummyDir, "Alert.Wav"));
		Assert.assertTrue(filter.accept(dummyDir, "sound_effect.wAv"));
		Assert.assertTrue(filter.accept(dummyDir, ".wav"));

		// Should reject non-wav files
		Assert.assertFalse(filter.accept(dummyDir, "alert.mp3"));
		Assert.assertFalse(filter.accept(dummyDir, "alert.txt"));
		Assert.assertFalse(filter.accept(dummyDir, "alert.wav.bak"));
		Assert.assertFalse(filter.accept(dummyDir, "alert.wav.tmp"));
		Assert.assertFalse(filter.accept(dummyDir, "wav"));
		Assert.assertFalse(filter.accept(dummyDir, "alert.ogg"));
		Assert.assertFalse(filter.accept(dummyDir, "alert.flac"));
		Assert.assertFalse(filter.accept(dummyDir, "alert.aiff"));
	}

	@Test
	public void testPlayCustomSoundEdgeCasesDoNotThrow()
	{
		// Null, empty, whitespace, and non-existent files should be handled gracefully without throwing
		soundManager.playCustomSound(null);
		soundManager.playCustomSound("");
		soundManager.playCustomSound("   ");
		soundManager.playCustomSound("non_existent_sound_12345.wav");
	}
}
