package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.teamwizardry.wizardrybot.api.*;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;

import java.util.Optional;
import java.util.Random;

public class ModuleEmotes extends Module implements ICommandModule {

	private String username = null;

	@Override
	public String getActionID() {
		return null;
	}

	@Override
	public String getName() {
		return "Emote";
	}

	@Override
	public String getDescription() {
		return "Will delete your message and display the emote by albus.";
	}

	@Override
	public String getUsage() {
		return "'hey alby, <emote> [me] [message]'";
	}

	@Override
	public String getExample() {
		return "'hey albus, disapprove' or 'sup albie, ayyy me' or 'hey albus, magic wizardry is cool'";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"disapprove", "happy-walk", "why", "ayyy", "disapproval", "magic", "tableflip", "lenny", "sparkle", "shrug", "gib"};
	}

	@Override
	public boolean onCommand(DiscordApi api, Message message, Command command, Result result, boolean whatsapp) {
		if (command.getCommand() == null || command.getCommand().isEmpty()) return true;
		if (command.getCommand().toLowerCase().equals(getName().toLowerCase())) return false;

		String emote = "";
		switch (command.getCommand()) {
			case "why":
				emote = "ლ(ಠ益ಠლ)";
				break;
			case "ayyy":
				emote = "(☞ﾟ∀ﾟ)☞";
				break;
			case "disapprove":
			case "disapproval":
				emote = "ಠ_ಠ";
				break;
			case "tableflip":
				emote = "(ノಠ益ಠ)ノ彡┻━┻";
				break;
			case "lenny":
				emote = "( ͡° ͜ʖ ͡°)";
				break;
			case "shrug":
				emote = "¯\\\\_(ツ)\\_/¯";
				break;
			case "gib":
				emote = "༼ つ ◕\\_◕ ༽つ";
				break;
			case "happy-walk":
				emote = "ᕕ( ᐛ )ᕗ";
				break;
			case "sparkle":
			case "magic": {
				int r = new Random().nextInt(10);
				switch (r) {
					case 0:
						emote = "(ﾉ≧∀≦)ﾉ・‥…━━━★";
						break;
					case 1:
						emote = "(*’▽’)ノ＾—==ΞΞΞ☆";
						break;
					case 2:
						emote = "✩°｡⋆⸜(ू˙꒳˙)";
						break;
					case 3:
						emote = "╰(•̀ 3 •́)━☆ﾟ.*･｡ﾟ";
						break;
					case 4:
						emote = "(っ・ω・）っ≡≡≡≡≡≡☆";
						break;
					case 5:
						emote = "彡ﾟ◉ω◉ )つー☆*";
						break;
					case 6:
						emote = "༼∩ •́ ヮ •̀ ༽⊃━☆ﾟ. * ･ ｡ﾟ";
						break;
					case 7:
						emote = "(∩ ͡° ͜ʖ ͡°)⊃━☆ﾟ";
						break;
					case 8:
						emote = "༼∩✿ل͜✿༽⊃━☆ﾟ. * ･ ｡ﾟ";
						break;
					case 9:
						emote = "(○´･∀･)o<･。:*ﾟ;+．";
						break;
					case 10:
						emote = "(つ˵•́ω•̀˵)つ━☆ﾟ.*･｡ﾟ҉̛༽̨҉҉ﾉ";
						break;
					default:
						emote = "(੭•̀ω•́)੭̸*✩⁺˚";
						break;
				}
			}
		}

		if (emote.isEmpty()) return false;
		if (command.getArguments().startsWith("me")) {
			message.delete();

			String finalEmote = emote;
			message.getAuthor().asUser().ifPresent(user -> {

				String s = command.getArguments().replace("me", "").trim();

				message.getServer().ifPresent(server -> {
					Optional<String> nick = server.getNickname(user);
					username = nick.orElseGet(() -> user.getDisplayName(server));
				});

				message.getServerTextChannel().ifPresent(serverTextChannel -> serverTextChannel
						.createWebhookBuilder()
						.setAvatar(message.getAuthor().getAvatar())
						.setName(username)
						.create()
						.whenComplete((webhook, throwable) -> {
							Utils.sendWebhookMessage(webhook, finalEmote + " " + s, username, message.getAuthor().getAvatar().getUrl().toString());
							webhook.delete();
						}));
				Statistics.INSTANCE.addToStat("emotes_used");
			});

		} else {
			message.delete();
			message.getChannel().sendMessage(emote + " " + command.getArguments());

			Statistics.INSTANCE.addToStat("emotes_used");
		}

		return true;
	}
}
