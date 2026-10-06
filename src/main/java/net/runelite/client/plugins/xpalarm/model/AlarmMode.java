package net.runelite.client.plugins.xpalarm.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Mode of evaluation for checking XP alarm thresholds.
 */
@Getter
@RequiredArgsConstructor
public enum AlarmMode
{
	ABSOLUTE_XP("Absolute XP"),
	REMAINING_XP_TO_LEVEL("XP Remaining to Next Level");

	private final String displayName;

	@Override
	public String toString()
	{
		return displayName;
	}
}
