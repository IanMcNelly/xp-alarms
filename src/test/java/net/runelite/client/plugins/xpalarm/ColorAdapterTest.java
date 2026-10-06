package net.runelite.client.plugins.xpalarm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.awt.Color;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import net.runelite.client.plugins.xpalarm.util.ColorAdapter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ColorAdapterTest
{
	private ColorAdapter colorAdapter;
	private Gson gson;

	@Before
	public void setUp()
	{
		colorAdapter = new ColorAdapter();
		gson = new GsonBuilder()
			.registerTypeAdapter(Color.class, colorAdapter)
			.create();
	}

	@Test
	public void testSerializeColorWithAlpha()
	{
		// Test ARGB with alpha transparency (alpha=150, red=255, green=0, blue=128)
		Color transparentColor = new Color(255, 0, 128, 150);
		String json = gson.toJson(transparentColor);
		// 150 = 0x96, 255 = 0xFF, 0 = 0x00, 128 = 0x80 -> #96FF0080
		Assert.assertEquals("\"#96FF0080\"", json);

		// Test fully opaque color
		Color opaqueColor = new Color(0, 255, 64, 255);
		Assert.assertEquals("\"#FF00FF40\"", gson.toJson(opaqueColor));

		// Test fully transparent color
		Color fullyTransparent = new Color(0, 0, 0, 0);
		Assert.assertEquals("\"#00000000\"", gson.toJson(fullyTransparent));
	}

	@Test
	public void testSerializeNullColor()
	{
		String json = gson.toJson(null, Color.class);
		Assert.assertEquals("null", json);
	}

	@Test
	public void testDeserializeHexFormatStrings()
	{
		// Test standard uppercase hex with alpha
		Color c1 = gson.fromJson("\"#96FF0080\"", Color.class);
		Assert.assertNotNull(c1);
		Assert.assertEquals(150, c1.getAlpha());
		Assert.assertEquals(255, c1.getRed());
		Assert.assertEquals(0, c1.getGreen());
		Assert.assertEquals(128, c1.getBlue());

		// Test lowercase hex
		Color c2 = gson.fromJson("\"#ff00ff40\"", Color.class);
		Assert.assertNotNull(c2);
		Assert.assertEquals(255, c2.getAlpha());
		Assert.assertEquals(0, c2.getRed());
		Assert.assertEquals(255, c2.getGreen());
		Assert.assertEquals(64, c2.getBlue());

		// Test fully transparent
		Color c3 = gson.fromJson("\"#00000000\"", Color.class);
		Assert.assertNotNull(c3);
		Assert.assertEquals(0, c3.getAlpha());
		Assert.assertEquals(0, c3.getRed());
		Assert.assertEquals(0, c3.getGreen());
		Assert.assertEquals(0, c3.getBlue());
	}

	@Test
	public void testDeserializeLegacyNumericStrings()
	{
		// Test legacy negative signed integer string (e.g. Color.RED.getRGB() = -65536)
		int redRgb = Color.RED.getRGB();
		Color c1 = gson.fromJson("\"" + redRgb + "\"", Color.class);
		Assert.assertNotNull(c1);
		Assert.assertEquals(Color.RED.getRGB(), c1.getRGB());
		Assert.assertEquals(255, c1.getAlpha());
		Assert.assertEquals(255, c1.getRed());
		Assert.assertEquals(0, c1.getGreen());
		Assert.assertEquals(0, c1.getBlue());

		// Test legacy color with custom alpha
		Color customColor = new Color(100, 150, 200, 75);
		int customRgb = customColor.getRGB();
		Color c2 = gson.fromJson("\"" + customRgb + "\"", Color.class);
		Assert.assertNotNull(c2);
		Assert.assertEquals(customColor.getRGB(), c2.getRGB());
		Assert.assertEquals(75, c2.getAlpha());
		Assert.assertEquals(100, c2.getRed());
		Assert.assertEquals(150, c2.getGreen());
		Assert.assertEquals(200, c2.getBlue());
	}

	@Test
	public void testDeserializeFallbackOnNullOrInvalidValues()
	{
		// Null JSON token returns null
		Color nullColor = gson.fromJson("null", Color.class);
		Assert.assertNull(nullColor);

		// Fallback color is default new Color(255, 0, 0, 140)
		Color expectedFallback = new Color(255, 0, 0, 140);

		// Non-color string
		Color invalidStr = gson.fromJson("\"not-a-color\"", Color.class);
		Assert.assertEquals(expectedFallback, invalidStr);

		// Empty string
		Color emptyStr = gson.fromJson("\"\"", Color.class);
		Assert.assertEquals(expectedFallback, emptyStr);

		// Invalid hex string
		Color invalidHex = gson.fromJson("\"#GGHHIIJJ\"", Color.class);
		Assert.assertEquals(expectedFallback, invalidHex);

		// Malformed hash prefix
		Color malformedHash = gson.fromJson("\"###\"", Color.class);
		Assert.assertEquals(expectedFallback, malformedHash);
	}

	@Test
	public void testDirectTypeAdapterWriteAndRead() throws IOException
	{
		// Direct write null
		StringWriter sw = new StringWriter();
		JsonWriter writer = new JsonWriter(sw);
		colorAdapter.write(writer, null);
		Assert.assertEquals("null", sw.toString());

		// Direct write color
		sw = new StringWriter();
		writer = new JsonWriter(sw);
		colorAdapter.write(writer, new Color(12, 34, 56, 78));
		Assert.assertEquals("\"#4E0C2238\"", sw.toString());

		// Direct read null
		JsonReader reader = new JsonReader(new StringReader("null"));
		Assert.assertNull(colorAdapter.read(reader));

		// Direct read valid hex
		reader = new JsonReader(new StringReader("\"#4E0C2238\""));
		Color readColor = colorAdapter.read(reader);
		Assert.assertEquals(78, readColor.getAlpha());
		Assert.assertEquals(12, readColor.getRed());
		Assert.assertEquals(34, readColor.getGreen());
		Assert.assertEquals(56, readColor.getBlue());
	}
}
