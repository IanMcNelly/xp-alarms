package com.gullesurgames.xpalarm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

/**
 * Full-screen visual overlay that flashes a customizable color when an alarm threshold is triggered.
 */
@Singleton
public class XpAlarmOverlay extends Overlay
{
	private final Client client;

	private volatile boolean flashActive = false;
	private volatile Color flashColor = new Color(255, 0, 0, 140);
	private volatile int remainingFrames = 0;

	@Inject
	public XpAlarmOverlay(Client client)
	{
		this.client = client;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ALWAYS_ON_TOP);
		setPriority(OverlayPriority.HIGHEST);
	}

	/**
	 * Activates the screen flash overlay.
	 *
	 * @param color The RGBA color to render.
	 * @param frameDuration Duration in game render frames.
	 */
	public synchronized void triggerFlash(Color color, int frameDuration)
	{
		this.flashColor = color != null ? color : new Color(255, 0, 0, 140);
		this.remainingFrames = Math.max(1, frameDuration);
		this.flashActive = true;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!flashActive)
		{
			return null;
		}

		synchronized (this)
		{
			if (remainingFrames <= 0)
			{
				flashActive = false;
				return null;
			}

			int width = client.getCanvasWidth();
			int height = client.getCanvasHeight();

			graphics.setColor(flashColor);
			graphics.fillRect(0, 0, width, height);

			remainingFrames--;
			if (remainingFrames <= 0)
			{
				flashActive = false;
			}
		}

		return null;
	}

	public synchronized void reset()
	{
		flashActive = false;
		remainingFrames = 0;
	}
}
