package com.teamwizardry.wizardrybot.module.correct;

import ai.api.model.Result;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.teamwizardry.wizardrybot.Keys;
import com.teamwizardry.wizardrybot.api.*;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;

import java.util.Optional;


public class ModuleAutoCorrect extends Module implements ICommandModule {

	private String username = null;

	@Override
	public String getActionID() {
		return null;
	}

	@Override
	public String getName() {
		return "Auto Correct";
	}

	@Override
	public String getDescription() {
		return "Albus will correct your sentence.";
	}

	@Override
	public String getUsage() {
		return "hey albus, autocorrect <sentence to autocorrect>";
	}

	@Override
	public String getExample() {
		return "'hey albus, autocorrect teting thes ting'";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"autocorrect", "correct", "ac"};
	}

	@Override
	public boolean onCommand(DiscordApi api, Message message, Command command, Result result, boolean whatsapp) {
		if (!command.getArguments().isEmpty()) {

			try {
				HttpResponse<JsonNode> response = Unirest
						.post("https://api.cognitive.microsoft.com/bing/v5.0/spellcheck?mode=proof&mkt=en-us")
						.header("Ocp-Apim-Subscription-Key", Keys.BING_SPELL_CHECK_API)
						.field("text", command.getArguments())
						.asJson();
				JsonElement element = new JsonParser().parse(response.getBody().getObject().toString());
				if (!element.isJsonObject()) return true;

				String string = Utils.processMentions(command.getArguments());
				JsonObject object = element.getAsJsonObject();
				if (object.has("flaggedTokens") && object.get("flaggedTokens").isJsonArray()) {
					JsonArray array = object.getAsJsonArray("flaggedTokens");
					for (JsonElement element1 : array) {
						if (element1.isJsonObject()) {
							JsonObject object1 = element1.getAsJsonObject();
							if (object1.has("token") && object1.get("token").isJsonPrimitive()
									&& object1.has("suggestions") && object1.get("suggestions").isJsonArray()) {

								String missspelledToken = object1.getAsJsonPrimitive("token").getAsString();
								JsonArray suggestions = object1.getAsJsonArray("suggestions");
								String bestSuggestion = null;
								float bestScore = 0;

								if (suggestions.size() <= 0) continue;
								for (JsonElement suggestion : suggestions) {
									if (!suggestion.isJsonObject()) continue;
									JsonObject guess = suggestion.getAsJsonObject();
									if (guess.has("score") && guess.get("score").isJsonPrimitive()
											&& guess.has("suggestion") && guess.get("suggestion").isJsonPrimitive()) {
										float score = guess.getAsJsonPrimitive("score").getAsFloat();
										String guessSuggestion = guess.getAsJsonPrimitive("suggestion").getAsString();
										if (bestScore < score) {
											bestScore = score;
											bestSuggestion = guessSuggestion;
										}
									}
								}

								if (bestScore >= 0.8) {
									string = string.replace(missspelledToken, bestSuggestion);
								}
							}
						}
					}
				}

				message.delete();

				Optional<User> probablyUser = message.getAuthor().asUser();
				if (!probablyUser.isPresent()) return true;
				User user = probablyUser.get();

				String s = Utils.processMentions(command.getArguments().replace("me", "").trim());
				message.getServer().ifPresent(server -> {
					Optional<String> nick = server.getNickname(user);
					username = nick.orElseGet(() -> user.getDisplayName(server));
				});

				String finalString = string;
				message.getServerTextChannel().ifPresent(serverTextChannel -> serverTextChannel
						.createWebhookBuilder()
						.setAvatar(message.getAuthor().getAvatar())
						.setName(username)
						.create()
						.whenComplete((webhook, throwable) -> {
							Utils.sendWebhookMessage(webhook, finalString, username, message.getAuthor().getAvatar().getUrl().toString());
							webhook.delete();
						}));

				Statistics.INSTANCE.addToStat("auto_corrections_made");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}
}
