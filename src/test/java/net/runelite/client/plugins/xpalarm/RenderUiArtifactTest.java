package net.runelite.client.plugins.xpalarm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.runelite.api.Skill;
import net.runelite.client.plugins.xpalarm.audio.CustomSoundManager;
import net.runelite.client.plugins.xpalarm.model.AlarmMode;
import net.runelite.client.plugins.xpalarm.model.XpAlarmTarget;
import net.runelite.client.plugins.xpalarm.ui.XpAlarmPanel;
import org.junit.Assert;
import org.junit.Test;

public class RenderUiArtifactTest
{
	@Test
	public void testNavIconAndRenderPanels() throws Exception
	{
		List<XpAlarmTarget> testAlarms = new ArrayList<>();
		testAlarms.add(XpAlarmTarget.builder().skill(Skill.ATTACK).mode(AlarmMode.REMAINING_XP_TO_LEVEL).thresholdValue(500000).triggered(true).build());
		testAlarms.add(XpAlarmTarget.builder().skill(Skill.HITPOINTS).mode(AlarmMode.ABSOLUTE_XP).thresholdValue(10689670).triggered(true).soundEnabled(false).systemAlertEnabled(false).build());
		testAlarms.add(XpAlarmTarget.builder().skill(Skill.FLETCHING).mode(AlarmMode.REMAINING_XP_TO_LEVEL).thresholdValue(1000).build());
		testAlarms.add(XpAlarmTarget.builder().skill(Skill.CRAFTING).mode(AlarmMode.REMAINING_XP_TO_LEVEL).thresholdValue(10000).build());
		testAlarms.add(XpAlarmTarget.builder().skill(Skill.MINING).mode(AlarmMode.ABSOLUTE_XP).thresholdValue(199999000).build());

		XpAlarmPlugin plugin = new XpAlarmPlugin()
		{
			@Override
			public List<XpAlarmTarget> getAlarms()
			{
				return testAlarms;
			}

			@Override
			public CustomSoundManager getCustomSoundManager()
			{
				return new CustomSoundManager();
			}

			@Override
			public void saveAlarms()
			{
			}
		};

		// 1. Verify and render procedural Nav Icon with XP on top
		Method createNavIconMethod = XpAlarmPlugin.class.getDeclaredMethod("createNavIcon");
		createNavIconMethod.setAccessible(true);
		BufferedImage navIcon = (BufferedImage) createNavIconMethod.invoke(plugin);

		Assert.assertNotNull("Nav icon should not be null", navIcon);
		Assert.assertEquals(16, navIcon.getWidth());
		Assert.assertEquals(16, navIcon.getHeight());

		File artifactDir = new File("C:/Users/snake/.gemini/antigravity/brain/bd3d30e8-d110-434d-8f0d-d18978809387");
		if (artifactDir.exists())
		{
			ImageIO.write(navIcon, "png", new File(artifactDir, "nav_icon_xp_16.png"));
			// Scale 4x for artifact display
			BufferedImage navIconScaled = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = navIconScaled.createGraphics();
			g2.drawImage(navIcon, 0, 0, 64, 64, null);
			g2.dispose();
			ImageIO.write(navIconScaled, "png", new File(artifactDir, "nav_icon_xp_64.png"));
		}

		// 2. Render Panel with new Import, Export, Add icons and populated cards
		final XpAlarmPanel[] panels = new XpAlarmPanel[2];
		SwingUtilities.invokeAndWait(() -> {
			panels[0] = new XpAlarmPanel(plugin);
			panels[0].refreshList();

			panels[1] = new XpAlarmPanel(plugin);
			panels[1].refreshList();
			panels[1].showNewAlarmEditor();
		});

		// Allow pending EDT events from refreshList() to complete
		SwingUtilities.invokeAndWait(() -> {});
		Thread.sleep(150);
		SwingUtilities.invokeAndWait(() -> {
			try
			{
				if (artifactDir.exists())
				{
					renderComponent(panels[0], 225, 600, new File(artifactDir, "panel_normal_225.png").getAbsolutePath());
					renderComponent(panels[0], 242, 600, new File(artifactDir, "panel_normal_242.png").getAbsolutePath());
					renderComponent(panels[1], 225, 600, new File(artifactDir, "panel_edit_225.png").getAbsolutePath());
				}
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		});
	}

	private void renderComponent(JPanel panel, int w, int h, String outPath) throws Exception
	{
		JFrame frame = new JFrame();
		frame.setUndecorated(true);
		frame.setSize(w, h);
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setSize(w, h);
		frame.doLayout();
		panel.setSize(w, h);
		panel.doLayout();
		panel.validate();

		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		panel.paint(g2);
		g2.dispose();
		frame.dispose();

		ImageIO.write(image, "png", new File(outPath));
	}
}
