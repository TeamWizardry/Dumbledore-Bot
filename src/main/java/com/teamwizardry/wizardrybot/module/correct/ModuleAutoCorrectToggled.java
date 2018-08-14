package com.teamwizardry.wizardrybot.module.correct;

import ai.api.model.Result;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.teamwizardry.wizardrybot.Keys;
import com.teamwizardry.wizardrybot.api.*;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;


public class ModuleAutoCorrectToggled extends Module implements ICommandModule {

	private String username = null;

	@Override
	public boolean overrideResponseCheck() {
		return true;
	}

	@Override
	public String getName() {
		return "Auto Correct Toggle";
	}

	@Override
	public String getDescription() {
		return "Albus will correct your sentences automatically.";
	}

	@Override
	public String getUsage() {
		return "hey albus, autocorrectme";
	}

	@Override
	public String getExample() {
		return "'hey albus, autocorrect teting thes ting'";
	}

	@Override
	public String getActionID() {
		return null;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"autocorrectme", "correctme", "acm"};
	}

	@Override
	public boolean onCommand(DiscordApi api, Message message, Command command, Result result) {
		if (!command.hasSaidHey()) return true;

		File file = new File("autocorrect.json");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				message.getChannel().sendMessage("I'm experiencing difficulty rn. Sorry.");
			}
		}
		if (file.exists()) {
			try {
				JsonElement element = new JsonParser().parse(new FileReader(file));
				if (!element.isJsonArray()) element = new JsonArray();

				JsonArray array = element.getAsJsonArray();
				User autocorrectUser = null;

				JsonObject userObject = null;
				for (JsonElement object : array) {
					if (object.isJsonObject()) {
						User user = Utils.lookupUserFromHash(object.getAsJsonObject(), api);
						if (user == null) continue;
						userObject = object.getAsJsonObject();
						autocorrectUser = user;
						break;
					}
				}
				if (autocorrectUser == null) {
					array.add(Utils.encryptString(message.getAuthor().getIdAsString()));
					try (JsonWriter writer = new JsonWriter(Files.newBufferedWriter(file.toPath()))) {
						Streams.write(array, writer);
						message.getChannel().sendMessage("You will now be autocorrected");
					} catch (IOException e) {
						e.printStackTrace();
						message.getChannel().sendMessage("Something went wrong...");
						message.getChannel().sendMessage("```" + Arrays.toString(e.getStackTrace()) + "```");
					}
				} else {
					array.remove(userObject);
					try (JsonWriter writer = new JsonWriter(Files.newBufferedWriter(file.toPath()))) {
						Streams.write(array, writer);
						message.getChannel().sendMessage("You will no longer be autocorrected");
					} catch (IOException e) {
						e.printStackTrace();
						message.getChannel().sendMessage("Something went wrong...");
						message.getChannel().sendMessage("```" + Arrays.toString(e.getStackTrace()) + "```");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return true;
	}

	@Override
	public void onMessage(DiscordApi api, Message message, Result result, Command command) {
		if (command.hasSaidHey()) return;

		File file = new File("autocorrect.json");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException ignored) {
			}
		}
		boolean autoCorrect = false;
		if (file.exists()) {
			try {
				JsonElement element = new JsonParser().parse(new FileReader(file));
				if (!element.isJsonArray()) element = new JsonArray();

				JsonArray array = element.getAsJsonArray();

				for (JsonElement object : array) {
					if (object.isJsonObject()) {
						User user = Utils.lookupUserFromHash(object.getAsJsonObject(), api);
						if (user == null) continue;
						autoCorrect = true;
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (!autoCorrect) return;

		try {
			HttpResponse<JsonNode> response = Unirest
					.post("https://api.cognitive.microsoft.com/bing/v5.0/spellcheck?mode=proof&mkt=en-us")
					.header("Ocp-Apim-Subscription-Key", Keys.BING_SPELL_CHECK_API)
					.field("text", command.getArguments())
					.asJson();
			JsonElement element = new JsonParser().parse(response.getBody().getObject().toString());
			if (!element.isJsonObject()) return;

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
			if (!probablyUser.isPresent()) return;
			User user = probablyUser.get();

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
}
