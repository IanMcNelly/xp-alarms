package net.runelite.client.plugins.xpalarm.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.awt.Color;
import java.io.IOException;

/**
 * Custom Gson TypeAdapter for serializing and deserializing java.awt.Color with alpha transparency.
 */
public class ColorAdapter extends TypeAdapter<Color>
{
	@Override
	public void write(JsonWriter out, Color value) throws IOException
	{
		if (value == null)
		{
			out.nullValue();
			return;
		}
		// Write 32-bit ARGB hex string (#AARRGGBB)
		out.value(String.format("#%08X", value.getRGB()));
	}

	@Override
	public Color read(JsonReader in) throws IOException
	{
		if (in.peek() == JsonToken.NULL)
		{
			in.nextNull();
			return null;
		}

		String str = in.nextString();
		try
		{
			if (str.startsWith("#"))
			{
				long parsed = Long.parseUnsignedLong(str.substring(1), 16);
				return new Color((int) parsed, true);
			}
			else
			{
				long parsed = Long.parseLong(str);
				return new Color((int) parsed, true);
			}
		}
		catch (Exception ex)
		{
			return new Color(255, 0, 0, 140);
		}
	}
}
