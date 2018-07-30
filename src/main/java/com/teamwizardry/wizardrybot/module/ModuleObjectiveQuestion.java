package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.teamwizardry.wizardrybot.Keys;
import com.teamwizardry.wizardrybot.api.*;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.json.JSONObject;
import org.json.XML;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class ModuleObjectiveQuestion extends Module implements ICommandModule {

	@Override
	public String getActionID() {
		return "input.question";
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public String getName() {
		return "Objective Question";
	}

	@Override
	public String getDescription() {
		return "Look something up on wikipedia/merriam webster/the web";
	}

	@Override
	public String getUsage() {
		return "hey albus, <question>";
	}

	@Override
	public String getExample() {
		return "hey albus, what's a cookie?";
	}

	public static void runLookup(Message message, final String anyString) {
		ThreadManager.INSTANCE.addThread(new Thread(() -> {

			String txt = anyString.replace(" ", "_");

			if (!tryWikipediaSearch(message, txt)) {
				if (!tryMerriamSearch(message, txt)) {

					try (LanguageServiceClient language = LanguageServiceClient.create()) {

						Document doc = Document.newBuilder().setContent(anyString).setType(Document.Type.PLAIN_TEXT).build();

						List<Entity> entities = language.analyzeEntities(doc, EncodingType.UTF8).getEntitiesList();
						for (Entity entity : entities) {
							message.getChannel().sendMessage("entity: " + entity.getName());
							if (!tryWikipediaSearch(message, entity.getName())) {
								tryMerriamSearch(message, entity.getName());
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}));
	}

	private static boolean tryMerriamSearch(Message message, String entity) {
		try {
			ArrayList<MerriamInterpretter.MerriamResult> results;
			ArrayList<String> outputSuggestions;

			HttpResponse<String> dictResponse = Unirest
					.get("http://www.dictionaryapi.com/api/v1/references/collegiate/xml/" + entity + "?key=" + Keys.MERRIAM_DICTIONARY_KEY)
					.asString();
			{
				JSONObject xmlToJson = XML.toJSONObject(dictResponse.getBody());
				JsonElement element = new JsonParser().parse(xmlToJson.toString());
				System.out.println(element);
				MerriamInterpretter interpretter = new MerriamInterpretter(element);
				results = interpretter.results;
				outputSuggestions = interpretter.outputSuggestions;
			}
			if (!outputSuggestions.isEmpty()) {
				//EmbedBuilder embed = new EmbedBuilder().setTitle(entity.toUpperCase()).setColor(Color.MAGENTA);
				//embed.setDescription("Could not find the word you're looking for. Here are some suggestions you can try:");
				//StringBuilder sugg = new StringBuilder();
				//for (int i = 0; i < outputSuggestions.size(); i++) {
				//	sugg.append(outputSuggestions.get(i)).append(i == outputSuggestions.size() - 1 ? "" : ", ");
				//}
				//embed.addField("Suggestions", sugg.toString(), false);
				//message.getChannel().sendMessage("", embed);
				return false;
			}

			if (results.isEmpty()) return false;

			int otherCount = 0;
			boolean anyMeaningOverLimit = false;

			int x = 0;
			for (MerriamInterpretter.MerriamResult merriam : results) {
				x++;
				otherCount += ("**Meaning " + x + "**: **__" + merriam.id + "__** - " + merriam.partOfSpeech + " [" + merriam.date + "]").length();
				otherCount += merriam.finalFormattedDefinition.length();
				if (merriam.finalFormattedDefinition.length() > 1000) {
					anyMeaningOverLimit = true;
				}
			}

			int characterCount = (otherCount
					+ "Definitions".length()
					+ "Synonyms".length()
					+ "Part of Speech".length());

			if (characterCount < 6000 && !anyMeaningOverLimit) {
				EmbedBuilder embed = new EmbedBuilder().setTitle(entity.toUpperCase()).setColor(Color.MAGENTA);

				int i = 0;
				for (MerriamInterpretter.MerriamResult merriam : results) {
					i++;
					embed.addField("**Meaning " + i + "**: **__" + merriam.id + "__** - " + merriam.partOfSpeech + " [" + merriam.date + "]", merriam.finalFormattedDefinition.toString(), false);
				}

				message.getChannel().sendMessage("", embed);

				return true;
			} else {
				ThreadManager.INSTANCE.addThread(new Thread(() -> {
					try {
						int i = 0;
						for (MerriamInterpretter.MerriamResult merriam : results) {
							i++;
							Thread.sleep(100);
							message.getChannel().sendMessage("", new EmbedBuilder()
									.setTitle("**Meaning " + i + "**: **__" + merriam.id + "__** - " + merriam.partOfSpeech + " [" + merriam.date + "]").setColor(Color.MAGENTA)
									.setDescription(merriam.finalFormattedDefinition.toString()));
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}));

				return true;
			}
		} catch (UnirestException e) {
			return false;
		}
	}

	private static boolean tryWikipediaSearch(Message message, String entity) {
		try {
			HttpResponse<JsonNode> response = Unirest
					.get("https://en.wikipedia.org/api/rest_v1/page/summary/" + entity)
					.header("Accept", "application/json")
					.header("User-Agent", "saadodi44@gmail.com")
					.asJson();
			JsonElement element = new JsonParser().parse(response.getBody().getObject().toString());
			if (element.isJsonObject()) {
				JsonObject object = element.getAsJsonObject();
				String title = null;
				String extract = null;
				String description = null;
				String sourceImage = null;
				if (object.has("title") && object.get("title").isJsonPrimitive()) {
					title = object.getAsJsonPrimitive("title").getAsString();
				}
				if (object.has("extract") && object.get("extract").isJsonPrimitive()) {
					extract = object.getAsJsonPrimitive("extract").getAsString();
					if (extract.length() > 1000) {
						extract = extract.substring(0, 1000) + "...";
					}
				}
				if (object.has("description") && object.get("description").isJsonPrimitive()) {
					description = object.getAsJsonPrimitive("description").getAsString();
				}
				if (object.has("originalimage") && object.get("originalimage").isJsonObject()) {
					JsonObject originalImage = object.getAsJsonObject("originalimage");
					if (originalImage.has("source") && originalImage.get("source").isJsonPrimitive())
						sourceImage = originalImage.getAsJsonPrimitive("source").getAsString();
				}

				if (title == null || extract == null) {
					return false;

				} else {
					EmbedBuilder builder = new EmbedBuilder()
							.setTitle(title)
							.addField("Extract", extract, false);

					if (sourceImage != null) builder.setImage(sourceImage);
					if (description != null) builder.setDescription(description);
					message.getChannel().sendMessage(builder);
					return true;
				}
			}

		} catch (Exception e) {
			return false;
		}
		return false;
	}

	@Override
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {
		ThreadManager.INSTANCE.addThread(new Thread(() -> {

			String any = result.getStringParameter("any");

			String txt = any.replace(" ", "_");

			boolean anySuccess = false;
			if (command.getCommandArguments().contains("mean") || command.getCommandArguments().contains("defin")) {
				if (tryMerriamSearch(message, txt)) {
					anySuccess = true;
				} else if (tryWikipediaSearch(message, txt)) {
					anySuccess = true;
				}
			} else {
				if (!tryWikipediaSearch(message, txt)) {
					if (!tryMerriamSearch(message, txt)) {

						try (LanguageServiceClient language = LanguageServiceClient.create()) {

							Document doc = Document.newBuilder().setContent(any).setType(Document.Type.PLAIN_TEXT).build();

							List<Entity> entities = language.analyzeEntities(doc, EncodingType.UTF8).getEntitiesList();
							for (Entity entity : entities) {
								message.getChannel().sendMessage("entity: " + entity.getName());

								if (tryWikipediaSearch(message, entity.getName())) {
									anySuccess = true;
								} else if (tryMerriamSearch(message, entity.getName())) {
									anySuccess = true;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						anySuccess = true;
					}
				} else {
					anySuccess = true;
				}

			}

			if (!anySuccess) {
				message.getChannel().sendMessage("I don't know what `" + any + "` is. Ask something similar.");
			}

		}));
	}
}
