package com.gullesurgames.xpalarm.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.awt.Color;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import com.gullesurgames.xpalarm.model.XpAlarmTarget;

/**
 * Utility for JSON and Base64 serialization and deserialization of XP Alarms.
 */
@Slf4j
@Singleton
public class AlarmSerialization
{
	private static final Type ALARM_LIST_TYPE = new TypeToken<List<XpAlarmTarget>>() {}.getType();

	private final Gson gson;

	@Inject
	public AlarmSerialization(Gson clientGson)
	{
		this.gson = clientGson.newBuilder()
			.registerTypeAdapter(Color.class, new ColorAdapter())
			.create();
	}

	public String toJson(List<XpAlarmTarget> alarms)
	{
		if (alarms == null || alarms.isEmpty())
		{
			return "[]";
		}
		return gson.toJson(alarms, ALARM_LIST_TYPE);
	}

	public List<XpAlarmTarget> fromJson(String json)
	{
		if (json == null || json.trim().isEmpty())
		{
			return new ArrayList<>();
		}
		try
		{
			List<XpAlarmTarget> list = gson.fromJson(json, ALARM_LIST_TYPE);
			return list != null ? list : new ArrayList<>();
		}
		catch (Exception e)
		{
			log.error("Failed to parse XP alarm JSON payload", e);
			return new ArrayList<>();
		}
	}

	public String exportToBase64(List<XpAlarmTarget> alarms)
	{
		String json = toJson(alarms);
		return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
	}

	public List<XpAlarmTarget> importFromBase64(String base64) throws IllegalArgumentException
	{
		if (base64 == null || base64.trim().isEmpty())
		{
			return new ArrayList<>();
		}
		try
		{
			byte[] decoded = Base64.getDecoder().decode(base64.trim());
			String json = new String(decoded, StandardCharsets.UTF_8);
			return fromJson(json);
		}
		catch (Exception e)
		{
			log.error("Failed to decode Base64 alarms string", e);
			throw new IllegalArgumentException("Invalid Base64 alarm payload", e);
		}
	}
}
