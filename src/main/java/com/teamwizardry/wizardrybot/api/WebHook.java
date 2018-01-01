package com.teamwizardry.wizardrybot.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.teamwizardry.wizardrybot.WizardryBot;
import de.btobastian.javacord.ImplDiscordApi;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.impl.ImplUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.IOException;

public class WebHook {

	private String webHookID;
	private String serverID;
	private String channelID;
	private User sender;
	private String webHookName;
	private String avatar;
	private String token;

	public WebHook(String channelID, String webHookName, String avatar) {
		JsonObject object = new JsonObject();
		object.addProperty("name", webHookName);
		object.addProperty("avatar", avatar);

		try {
			HttpResponse<JsonNode> response = Unirest
					.post("https://discordapp.com/api/v6/channels/" + channelID + "/webhooks")
					.header("authorization", WizardryBot.API.getToken())
					.header("Content-Type", "application/json")
					.body(object.toString())
					.asJson();
			JsonElement element = new JsonParser().parse(response.getBody().getObject().toString());
			fromJson(element.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public WebHook(long webHookID) {
		try {
			HttpResponse<JsonNode> response = Unirest
					.get("https://discordapp.com/api/v6/webhooks/" + webHookID)
					.header("authorization", WizardryBot.API.getToken())
					.header("Content-Type", "application/json")
					.asJson();
			JsonElement element = new JsonParser().parse(response.getBody().getObject().toString());
			fromJson(element.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public WebHook(long id, String username, String json) {
		fromJson(json);
	}

	public void fromJson(String json) {
		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(json);

		if (!element.isJsonObject()) return;
		JsonObject object = element.getAsJsonObject();

		if (object.has("name") && object.get("name").isJsonPrimitive())
			webHookName = object.getAsJsonPrimitive("name").getAsString();

		if (object.has("channel_id") && object.get("channel_id").isJsonPrimitive())
			channelID = object.getAsJsonPrimitive("channel_id").getAsString();

		if (object.has("token") && object.get("token").isJsonPrimitive())
			token = object.getAsJsonPrimitive("token").getAsString();

		if (object.has("avatar") && object.get("avatar").isJsonPrimitive())
			avatar = object.getAsJsonPrimitive("avatar").getAsString();

		if (object.has("guild_id") && object.get("guild_id").isJsonPrimitive())
			serverID = object.getAsJsonPrimitive("guild_id").getAsString();

		if (object.has("id") && object.get("id").isJsonPrimitive())
			webHookID = object.getAsJsonPrimitive("id").getAsString();

		if (object.has("user") && object.get("user").isJsonObject()) {
			JsonObject gson = object.getAsJsonObject("user");
			JSONObject javaJson = new JSONObject(gson.toString());

			com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
			try {
				sender = new ImplUser((ImplDiscordApi) WizardryBot.API, mapper.readTree(javaJson.toString()));
			} catch (IOException e) {
			}
		}
	}

	public String toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("name", webHookName);
		object.addProperty("channel_id", channelID);
		object.addProperty("token", token);
		object.addProperty("avatar", avatar);
		object.addProperty("guild_id", serverID);
		object.addProperty("id", webHookID);

		JsonObject userObject = new JsonObject();
		userObject.addProperty("username", sender.getName());
		userObject.addProperty("discriminator", sender.getDiscriminator());
		userObject.addProperty("id", sender.getId());
		userObject.addProperty("avatar", String.valueOf(sender.getAvatar().getUrl()));
		object.add("user", userObject);

		return object.toString();
	}

	public String getToken() {
		return token;
	}

	public String getAvatar() {
		return avatar;
	}

	public WebHook setAvatar(String avatar) {
		JsonObject object = new JsonObject();
		object.addProperty("avatar_url", avatar);

		try {
			HttpResponse<JsonNode> response = Unirest
					.patch("https://discordapp.com/api/v6/webhooks/" + webHookID)
					.header("authorization", WizardryBot.API.getToken())
					.header("Content-Type", "application/json")
					.body(object.toString())
					.asJson();
			//((ImplDiscordAPI) WizardryBot.API).checkRateLimit(response, RateLimitType.UNKNOWN, ImplServer.this, null);
			JsonElement element = new JsonParser().parse(response.getBody().getObject().toString());
			fromJson(element.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	public String getWebHookName() {
		return webHookName;
	}

	public WebHook setWebHookName(String webHookName) {
		JsonObject object = new JsonObject();
		object.addProperty("name", webHookName);

		try {
			HttpResponse<JsonNode> response = Unirest
					.patch("https://discordapp.com/api/v6/webhooks/" + webHookID)
					.header("authorization", WizardryBot.API.getToken())
					.header("Content-Type", "application/json")
					.body(object.toString())
					.asJson();
			JsonElement element = new JsonParser().parse(response.getBody().getObject().toString());
			fromJson(element.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	public User getSender() {
		return sender;
	}

	public String getChannelID() {
		return channelID;
	}

	public String getWebHookID() {
		return webHookID;
	}

	public String getServerID() {
		return serverID;
	}

	public WebHook execute(@NotNull String content, @Nullable String username, @Nullable String avatarUrl) {
		if (content.length() > 2000) return this;

		JsonObject object = new JsonObject();
		object.addProperty("content", content);
		if (username != null)
			object.addProperty("username", username);
		if (avatarUrl != null)
			object.addProperty("avatar_url", avatarUrl);

		try {
			HttpResponse<JsonNode> response = Unirest
					.post("https://discordapp.com/api/v6/webhooks/" + webHookID + "/" + token)
					.header("authorization", WizardryBot.API.getToken())
					.header("Content-Type", "application/json")
					.body(object.toString())
					.asJson();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	public WebHook delete() {
		try {
			HttpResponse<JsonNode> response = Unirest
					.delete("https://discordapp.com/api/v6/webhooks/" + webHookID)
					.header("authorization", WizardryBot.API.getToken())
					.asJson();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	public WebHook setNameAndAvatar(String avatar, String webHookName) {
		JsonObject object = new JsonObject();
		object.addProperty("name", webHookName);
		object.addProperty("avatar_url", avatar);

		try {
			HttpResponse<JsonNode> response = Unirest
					.patch("https://discordapp.com/api/v6/webhooks/" + webHookID)
					.header("authorization", WizardryBot.API.getToken())
					.header("Content-Type", "application/json")
					.body(object.toString())
					.asJson();
			//((ImplDiscordAPI) WizardryBot.API).checkRateLimit(response, RateLimitType.UNKNOWN, ImplServer.this, null);
			JsonElement element = new JsonParser().parse(response.getBody().getObject().toString());
			fromJson(element.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}
}
