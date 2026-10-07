package com.gullesurgames.xpalarm.audio;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.audio.AudioPlayer;
import net.runelite.client.util.Filepath;

/**
 * Manages loading and asynchronous playback of custom .wav audio files located in the plugin's sounds directory.
 */
@Slf4j
@Singleton
public class CustomSoundManager
{
	private final ExecutorService soundExecutor = Executors.newSingleThreadExecutor();
	private final AudioPlayer audioPlayer;

	@Getter
	private Filepath soundsDirectory;

	public CustomSoundManager()
	{
		this(new AudioPlayer());
	}

	@Inject
	public CustomSoundManager(AudioPlayer audioPlayer)
	{
		this.audioPlayer = audioPlayer;
	}

	/**
	 * Ensures the custom sounds directory structure exists on disk.
	 */
	public void initDirectory(Filepath dir)
	{
		this.soundsDirectory = dir;
		if (soundsDirectory != null && !soundsDirectory.exists())
		{
			try
			{
				soundsDirectory.createDirectories();
				log.info("Created custom XP Alarm sound directory at: {}", soundsDirectory);
			}
			catch (IOException e)
			{
				log.error("Failed to create custom sound directory: {}", soundsDirectory, e);
			}
		}
	}

	/**
	 * Scans the custom sounds directory for available .wav files.
	 */
	public List<String> getAvailableSoundFiles()
	{
		if (soundsDirectory == null || !soundsDirectory.exists() || !soundsDirectory.isDirectory())
		{
			return Collections.emptyList();
		}

		try (Stream<Filepath> stream = soundsDirectory.walk(1))
		{
			return stream
				.filter(fp -> !fp.equals(soundsDirectory) && fp.isFile())
				.map(Filepath::getFileName)
				.filter(name -> name.toLowerCase().endsWith(".wav"))
				.sorted()
				.collect(Collectors.toList());
		}
		catch (IOException e)
		{
			log.error("Failed to list custom sound files from {}", soundsDirectory, e);
			return Collections.emptyList();
		}
	}

	/**
	 * Asynchronously plays a .wav audio clip by filename.
	 */
	public void playCustomSound(String fileName)
	{
		if (fileName == null || fileName.trim().isEmpty() || soundsDirectory == null)
		{
			return;
		}

		soundExecutor.submit(() -> {
			try
			{
				Filepath soundFile = soundsDirectory.join(fileName);
				if (!soundFile.exists() || !soundFile.isFile())
				{
					log.warn("Custom sound file not found: {}", soundFile);
					return;
				}

				audioPlayer.play(soundFile, 0.0f);
			}
			catch (IllegalArgumentException e)
			{
				log.warn("Invalid sound filename: {}", fileName, e);
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
		soundExecutor.shutdownNow();
	}
}
