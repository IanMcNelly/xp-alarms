package com.gullesurgames.xpalarm.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Type of audio cue played when an alarm threshold is triggered.
 */
@Getter
@RequiredArgsConstructor
public enum SoundType
{
	BUILTIN("Built-in Sound Effect"),
	CUSTOM("Custom WAV File");

	private final String displayName;

	@Override
	public String toString()
	{
		return displayName;
	}
}
