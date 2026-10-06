package com.gullesurgames.xpalarm;

import java.awt.Color;
import java.lang.reflect.Field;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class XpAlarmOverlayTest
{
	private XpAlarmOverlay overlay;

	@Before
	public void setUp()
	{
		overlay = new XpAlarmOverlay(null);
	}

	@Test
	public void testInitialOverlayProperties() throws Exception
	{
		Assert.assertEquals(OverlayPosition.DYNAMIC, overlay.getPosition());
		Assert.assertEquals(OverlayLayer.ALWAYS_ON_TOP, overlay.getLayer());
		Assert.assertEquals(Overlay.PRIORITY_HIGHEST, overlay.getPriority(), 0.001f);

		Assert.assertFalse(getFlashActive(overlay));
		Assert.assertEquals(0, getRemainingFrames(overlay));
		Assert.assertEquals(new Color(255, 0, 0, 140), getFlashColor(overlay));
	}

	@Test
	public void testTriggerFlashSetsInternalState() throws Exception
	{
		Color customColor = new Color(0, 255, 128, 160);
		overlay.triggerFlash(customColor, 30);

		Assert.assertTrue("flashActive should be true after triggerFlash", getFlashActive(overlay));
		Assert.assertEquals("flashColor should match custom color", customColor, getFlashColor(overlay));
		Assert.assertEquals("remainingFrames should match requested duration", 30, getRemainingFrames(overlay));
	}

	@Test
	public void testTriggerFlashNullColorFallback() throws Exception
	{
		overlay.triggerFlash(null, 15);

		Assert.assertTrue("flashActive should be true", getFlashActive(overlay));
		Assert.assertEquals("null color should fallback to default RGBA(255, 0, 0, 140)",
			new Color(255, 0, 0, 140), getFlashColor(overlay));
		Assert.assertEquals("remainingFrames should be set", 15, getRemainingFrames(overlay));
	}

	@Test
	public void testTriggerFlashEnforcesMinimumFrameCount() throws Exception
	{
		// Duration 0 should clamp to 1
		overlay.triggerFlash(Color.GREEN, 0);
		Assert.assertEquals(1, getRemainingFrames(overlay));

		// Negative duration should clamp to 1
		overlay.triggerFlash(Color.GREEN, -10);
		Assert.assertEquals(1, getRemainingFrames(overlay));
	}

	@Test
	public void testResetClearsStateImmediately() throws Exception
	{
		overlay.triggerFlash(new Color(255, 200, 0, 180), 45);

		Assert.assertTrue("Should be active before reset", getFlashActive(overlay));
		Assert.assertEquals(45, getRemainingFrames(overlay));

		overlay.reset();

		Assert.assertFalse("flashActive should be false after reset", getFlashActive(overlay));
		Assert.assertEquals("remainingFrames should be 0 after reset", 0, getRemainingFrames(overlay));
	}

	@Test
	public void testRenderWhenInactiveReturnsNull()
	{
		// render should immediately return null when flash is not active, without interacting with graphics
		Assert.assertNull(overlay.render(null));
	}

	private boolean getFlashActive(XpAlarmOverlay target) throws Exception
	{
		Field field = XpAlarmOverlay.class.getDeclaredField("flashActive");
		field.setAccessible(true);
		return field.getBoolean(target);
	}

	private Color getFlashColor(XpAlarmOverlay target) throws Exception
	{
		Field field = XpAlarmOverlay.class.getDeclaredField("flashColor");
		field.setAccessible(true);
		return (Color) field.get(target);
	}

	private int getRemainingFrames(XpAlarmOverlay target) throws Exception
	{
		Field field = XpAlarmOverlay.class.getDeclaredField("remainingFrames");
		field.setAccessible(true);
		return field.getInt(target);
	}
}
