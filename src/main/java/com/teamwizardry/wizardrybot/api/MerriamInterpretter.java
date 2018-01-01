package com.teamwizardry.wizardrybot.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MerriamInterpretter {

	public ArrayList<MerriamResult> results = new ArrayList<>();
	public ArrayList<String> outputSuggestions = new ArrayList<>();
	private ArrayList<JsonObject> entries = new ArrayList<>();

	public MerriamInterpretter(JsonElement element) {
		getAllEntries(element);
		processEntries();
	}

	private void getAllEntries(JsonElement element) {
		if (element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if (object.has("entry_list") && object.get("entry_list").isJsonObject()) {
				JsonObject entryList = object.getAsJsonObject("entry_list");
				if (entryList.has("entry")) {
					JsonElement entry = entryList.get("entry");

					if (entry.isJsonObject()) {
						entries.add(entry.getAsJsonObject());
					} else if (entry.isJsonArray()) {

						for (JsonElement actualEntry : entry.getAsJsonArray()) {
							if (actualEntry.isJsonObject())
								entries.add(actualEntry.getAsJsonObject());
						}
					}
				} else if (entryList.has("suggestion")) {
					JsonElement suggestions = entryList.get("suggestion");

					if (suggestions.isJsonPrimitive()) {
						outputSuggestions.add(suggestions.getAsString());
					} else if (suggestions.isJsonArray()) {

						for (JsonElement suggestion : suggestions.getAsJsonArray()) {
							if (suggestion.isJsonPrimitive())
								outputSuggestions.add(suggestion.getAsString());
						}
					}
				}
			}
		}
	}

	private void processEntries() {
		for (JsonObject entry : entries) {
			MerriamResult result = new MerriamResult(entry);
			if (!result.finalFormattedDefinition.toString().isEmpty())
				results.add(result);
		}
	}

	private String interpretDefinition(JsonElement element) {
		if (element.isJsonPrimitive()) {
			return element.getAsString();
		} else if (element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();

			String definition = null;
			Set<String> alsoCalled = new HashSet<>();
			Set<String> examples = new HashSet<>();

			if (object.has("it") && object.get("it").isJsonPrimitive()) {
				if (object.has("content") && object.get("content").isJsonArray()) {
					String betweened = object.get("it").getAsString();
					JsonArray content = object.get("content").getAsJsonArray();
					if (content.size() >= 1 && content.get(0).isJsonPrimitive()) {
						String part1 = content.get(0).getAsString().trim();

						definition = part1 + " " + betweened.trim() + " " + ((content.size() >= 2) ? content.get(1).getAsString().replace(":", "") : "");
					}
				}
			}

			String word = null;
			if (object.has("fw") && object.get("fw").isJsonPrimitive()) {
				word = object.getAsJsonPrimitive("fw").getAsString();
			}

			if (object.has("vi")) {
				if (object.get("vi").isJsonObject()) {
					JsonObject vi = object.getAsJsonObject("vi");
					if (vi.has("it") && vi.get("it").isJsonPrimitive()) {
						if (vi.has("content")) {
							if (vi.get("content").isJsonArray()) {
								String betweened = vi.get("it").getAsString();
								JsonArray content = vi.get("content").getAsJsonArray();
								if (content.size() >= 1 && content.get(0).isJsonPrimitive()) {
									String part1 = content.get(0).getAsString().trim();

									examples.add(StringUtils.capitalize(part1 + " " + betweened.trim() + " " + ((content.size() >= 2) ? content.get(1).getAsString().replace(":", "") : "")));
								}
							} else if (vi.get("content").isJsonPrimitive()) {
								String betweened = vi.get("it").getAsString();
								examples.add(StringUtils.capitalize(vi.getAsJsonPrimitive("content").getAsString().trim() + " " + betweened.trim()));
							}
						}
					}
				} else if (object.get("vi").isJsonArray()) {
					JsonArray vi = object.getAsJsonArray("vi");
					for (JsonElement subViElement : vi) {
						if (!subViElement.isJsonObject()) continue;
						JsonObject subVi = subViElement.getAsJsonObject();
						if (subVi.has("it") && subVi.get("it").isJsonPrimitive()) {
							if (subVi.has("content")) {
								if (subVi.get("content").isJsonArray()) {
									String betweened = subVi.get("it").getAsString();
									JsonArray content = subVi.get("content").getAsJsonArray();
									if (content.size() >= 1 && content.get(0).isJsonPrimitive()) {
										String part1 = content.get(0).getAsString().trim();

										examples.add(StringUtils.capitalize(part1 + " " + betweened.trim() + " " + ((content.size() >= 2) ? content.get(1).getAsString().replace(":", "") : "")));
									}
								} else if (subVi.get("content").isJsonPrimitive()) {
									String betweened = subVi.get("it").getAsString();
									examples.add(StringUtils.capitalize(subVi.getAsJsonPrimitive("content").getAsString().trim() + " " + betweened.trim()));
								}
							}
						}
					}
				}
			}

			if (object.has("sx")) {
				if (object.get("sx").isJsonPrimitive() && !object.getAsJsonPrimitive("sx").getAsString().trim().isEmpty()) {
					alsoCalled.add(StringUtils.capitalize(object.getAsJsonPrimitive("sx").getAsString()));
				} else if (object.get("sx").isJsonArray()) {
					JsonArray sx = object.getAsJsonArray("sx");
					for (JsonElement called : sx) {
						if (!called.isJsonPrimitive()) continue;
						alsoCalled.add(called.getAsString().trim());
					}
				}
			}

			if (object.has("d_link") && object.get("d_link").isJsonPrimitive()) {
				word = object.getAsJsonPrimitive("d_link").getAsString();
			}

			if (object.has("content")) {
				if (object.get("content").isJsonArray()) {
					JsonArray content = object.get("content").getAsJsonArray();
					if (content.size() >= 1 && content.get(0).isJsonPrimitive()) {
						String part1 = content.get(0).getAsString().trim();

						definition = part1 + " " + ((word != null) ? word.trim() + " " : "") + ((content.size() >= 2) ? content.get(1).getAsString().replace(":", "") : "");
					}
				} else if (object.get("content").isJsonPrimitive()) {
					if (object.getAsJsonPrimitive("content").getAsString().trim().equals(":")) {
						definition = "<implied>";
					} else
						definition = ((word != null) ? word.trim() + " " : "") + object.getAsJsonPrimitive("content").getAsString().replace(":", "").trim();
				}
			}
			if (definition == null) return null;

			if (object.has("ca") && object.get("ca").isJsonObject()) {
				JsonObject ca = object.getAsJsonObject("ca");
				if (ca.has("cat")) {
					if (ca.get("cat").isJsonArray()) {
						JsonArray cat = ca.getAsJsonArray("cat");
						for (JsonElement also : cat) {
							if (also.isJsonPrimitive() && !also.getAsString().trim().isEmpty()) {
								alsoCalled.add(StringUtils.capitalize(also.getAsString()));
							}
						}
					} else if (ca.get("cat").isJsonPrimitive() && !ca.get("cat").getAsString().trim().isEmpty())
						alsoCalled.add(StringUtils.capitalize(ca.getAsJsonPrimitive("cat").getAsString()));
				}
			}

			StringBuilder alsoCalledBuilder = new StringBuilder();
			if (!alsoCalled.isEmpty()) {
				alsoCalledBuilder.append(StringUtils.repeat(" ", 5)).append("__Also Called__:\n");
				for (String also : alsoCalled) {
					alsoCalledBuilder.append(StringUtils.repeat(" ", 10)).append("| ").append(also).append("\n");
				}
			}
			StringBuilder exampleBuilder = new StringBuilder();
			if (!examples.isEmpty()) {
				exampleBuilder.append(StringUtils.repeat(" ", 5)).append("__Examples__:\n");
				for (String example : examples) {
					exampleBuilder.append(StringUtils.repeat(" ", 10)).append("| ").append(example).append("\n");
				}
			}
			return ":" + definition.toLowerCase().replace(":", "").replace(" :", "").trim() + "\n" + exampleBuilder + alsoCalledBuilder;
		} else return null;
	}

	public class MerriamResult {

		public String id;
		public String partOfSpeech;
		public String date;
		public ArrayList<String> definitions = new ArrayList<>();
		public StringBuilder finalFormattedDefinition = new StringBuilder();

		public MerriamResult(@NotNull JsonObject entry) {
			if (entry.has("id") && entry.get("id").isJsonPrimitive()) {
				id = WordUtils.capitalizeFully(entry.getAsJsonPrimitive("id").getAsString());
			}
			if (entry.has("fl") && entry.get("fl").isJsonPrimitive()) {
				partOfSpeech = entry.getAsJsonPrimitive("fl").getAsString();
			}

			if (entry.has("def") && entry.get("def").isJsonObject()) {
				JsonObject def = entry.getAsJsonObject("def");

				if (def.has("date") && def.get("date").isJsonPrimitive()) {
					date = def.getAsJsonPrimitive("date").getAsString();
				}

				if (def.has("dt")) {
					JsonElement definition = def.get("dt");

					if (definition.isJsonPrimitive() || definition.isJsonObject()) {
						String finalDef = interpretDefinition(definition);
						if (finalDef != null && !finalDef.isEmpty())
							definitions.add(finalDef);
					} else if (definition.isJsonArray()) {
						JsonArray definitionObjects = definition.getAsJsonArray();
						for (JsonElement definitionObject : definitionObjects) {
							String finalDef = interpretDefinition(definitionObject);
							if (finalDef != null && !finalDef.isEmpty())
								definitions.add(finalDef);
						}
					}
				}
			}

			for (String definition : definitions) {
				finalFormattedDefinition.append(definition).append("\n");
			}
		}
	}
}
