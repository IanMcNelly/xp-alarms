package com.gullesurgames.xpalarm.model;

import java.awt.Color;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.runelite.api.Skill;

/**
 * Model representing an independent XP or Level Alarm target.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XpAlarmTarget
{
	@Builder.Default
	private UUID id = UUID.randomUUID();

	@Builder.Default
	private Skill skill = Skill.ATTACK;

	@Builder.Default
	private AlarmMode mode = AlarmMode.REMAINING_XP_TO_LEVEL;

	@Builder.Default
	private int thresholdValue = 1000;

	@Builder.Default
	private boolean flashEnabled = true;

	@Builder.Default
	private Color flashColor = new Color(255, 0, 0, 140);

	@Builder.Default
	private boolean soundEnabled = true;

	@Builder.Default
	private SoundType soundType = SoundType.BUILTIN;

	@Builder.Default
	private int soundId = 2266; // Level-up jingle default sound effect ID

	@Builder.Default
	private String customSoundFileName = "alarm.wav";

	@Builder.Default
	private boolean systemAlertEnabled = true;

	@Builder.Default
	private boolean chatMessageEnabled = true;

	@Builder.Default
	private boolean active = true;

	@Builder.Default
	private boolean triggered = false;

	/**
	 * Creates a deep copy of the alarm configuration for editing.
	 */
	public XpAlarmTarget deepCopy()
	{
		return XpAlarmTarget.builder()
			.id(this.id)
			.skill(this.skill)
			.mode(this.mode)
			.thresholdValue(this.thresholdValue)
			.flashEnabled(this.flashEnabled)
			.flashColor(new Color(this.flashColor.getRGB(), true))
			.soundEnabled(this.soundEnabled)
			.soundType(this.soundType)
			.soundId(this.soundId)
			.customSoundFileName(this.customSoundFileName)
			.systemAlertEnabled(this.systemAlertEnabled)
			.chatMessageEnabled(this.chatMessageEnabled)
			.active(this.active)
			.triggered(this.triggered)
			.build();
	}
}
