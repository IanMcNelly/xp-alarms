package com.gullesurgames.xpalarm.audio;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;
import net.runelite.client.audio.AudioPlayer;

/**
 * Manages loading and asynchronous playback of custom .wav audio files located in .runelite/xpalarm/sounds/
 */
@Slf4j
@Singleton
public class CustomSoundManager
{
	private static final File SOUNDS_DIR = new File(new File(RuneLite.RUNELITE_DIR, "xpalarm"), "sounds");
	private final ExecutorService soundExecutor = Executors.newSingleThreadExecutor();
	private final AudioPlayer audioPlayer;

	public CustomSoundManager()
	{
		this(new AudioPlayer());
	}

	@Inject
	public CustomSoundManager(AudioPlayer audioPlayer)
	{
		this.audioPlayer = audioPlayer;
		initDirectory();
	}

	/**
	 * Ensures the custom sounds directory structure exists on disk.
	 */
	public void initDirectory()
	{
		if (!SOUNDS_DIR.exists())
		{
			if (SOUNDS_DIR.mkdirs())
			{
				log.info("Created custom XP Alarm sound directory at: {}", SOUNDS_DIR.getAbsolutePath());
			}
		}
	}

	public File getSoundsDirectory()
	{
		return SOUNDS_DIR;
	}

	/**
	 * Scans the custom sounds directory for available .wav files.
	 */
	public List<String> getAvailableSoundFiles()
	{
		if (!SOUNDS_DIR.exists() || !SOUNDS_DIR.isDirectory())
		{
			return Collections.emptyList();
		}

		File[] files = SOUNDS_DIR.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
		if (files == null || files.length == 0)
		{
			return Collections.emptyList();
		}

		List<String> fileNames = new ArrayList<>();
		for (File file : files)
		{
			fileNames.add(file.getName());
		}
		Collections.sort(fileNames);
		return fileNames;
	}

	/**
	 * Asynchronously plays a .wav audio clip by filename.
	 */
	public void playCustomSound(String fileName)
	{
		if (fileName == null || fileName.trim().isEmpty())
		{
			return;
		}

		soundExecutor.submit(() -> {
			try
			{
				File soundFile = new File(SOUNDS_DIR, fileName);
				if (!soundFile.exists() || !soundFile.isFile())
				{
					log.warn("Custom sound file not found: {}", soundFile.getAbsolutePath());
					return;
				}

				audioPlayer.play(soundFile, 0.0f);
			}
			catch (Exception e)
			{
				log.error("Failed to play custom sound: " + fileName, e);
			}
		});
	}

	/**
	 * Shuts down the sound executor service upon plugin shutdown.
	 */
	public void shutDown()
	{
		soundExecutor.shutdown();
	}
}
