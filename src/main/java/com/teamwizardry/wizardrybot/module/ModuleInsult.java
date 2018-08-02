package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.ICommandModule;
import com.teamwizardry.wizardrybot.api.Module;
import com.teamwizardry.wizardrybot.api.Statistics;
import com.vdurmont.emoji.EmojiManager;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;

import java.util.Random;

public class ModuleInsult extends Module implements ICommandModule {

	@Override
	public int getPriority() {
		return -1;
	}

	@Override
	public String getName() {
		return "Insult";
	}

	@Override
	public String getDescription() {
		return "Dumbledore will respond to insults.";
	}

	@Override
	public String getUsage() {
		return "hey albus, <insult>";
	}

	@Override
	public String getExample() {
		return "'hey albus, fuck you' or 'hey albus, one punch bot is better than you!'";
	}

	@Override
	public String getActionID() {
		return "input.insult";
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {
		if (!result.getParameters().containsKey("curse")) return;

		String insult = result.getStringParameter("curse").toLowerCase().trim();
		String any = result.getStringParameter("any").toLowerCase().trim();
		if (insult.isEmpty()) return;

		Random rand = new Random();

		if (insult.contains("suck") && insult.contains("dick")) {
			int random = rand.nextInt(4);
			switch (random) {
				case 0: {
					message.getChannel().sendMessage("Ok. ( ͡° ͜ʖ ͡°)");
					Statistics.INSTANCE.addToStat("insulted_tastefully");
					break;
				}
				case 1: {
					message.getChannel().sendMessage("Gladly. ( ͡° ͜ʖ ͡°)");
					Statistics.INSTANCE.addToStat("insulted_tastefully");
					break;
				}
				case 2: {
					message.getChannel().sendMessage("Sure. ( ͡° ͜ʖ ͡°)");
					Statistics.INSTANCE.addToStat("insulted_tastefully");
					break;
				}
				case 4: {
					message.getChannel().sendMessage("( ͡° ͜ʖ ͡°)");
					Statistics.INSTANCE.addToStat("insulted_tastefully");
					break;
				}
			}
		} else if (any.contains("opb") || any.contains("opm") || (any.contains("one") && any.contains("punch") && any.contains("bot")) || any.contains("244168517396463616")) {
			int random = rand.nextInt(8);
			switch (random) {
				case 0: {
					message.getChannel().sendMessage("I'm so much better than it and you know it.");
					Statistics.INSTANCE.addToStat("insulted");
					break;
				}
				case 1: {
					message.getChannel().sendMessage("I'm better than that piece of junk. Screw you.");
					Statistics.INSTANCE.addToStat("insulted");
					break;
				}
				case 2: {
					message.getChannel().sendMessage("That's some low hanging fruit right there.");
					Statistics.INSTANCE.addToStat("insulted");
					break;
				}
				case 3: {
					message.getChannel().sendMessage("Boy, who do you think you are to say that?");
					Statistics.INSTANCE.addToStat("insulted");
					break;
				}
				case 4: {
					message.getChannel().sendMessage("It may be able to one punch, but I'm a god damned wizard.");
					Statistics.INSTANCE.addToStat("insulted");
					break;
				}
				case 5: {
					message.getChannel().sendMessage("HA. As if.");
					Statistics.INSTANCE.addToStat("insulted");
					break;
				}
				case 6: {
					message.getChannel().sendMessage("Does opb use artificial intelligence like myself? NOPE.");
					Statistics.INSTANCE.addToStat("insulted");
					break;
				}
				case 7: {
					message.getChannel().sendMessage("By the definition of AI, I'm objectively smarter than it. So screw you.");
					Statistics.INSTANCE.addToStat("insulted");
					break;
				}
				case 8: {
					message.getChannel().sendMessage("At least I'm not an automatic shitpost maker.");
					Statistics.INSTANCE.addToStat("insulted");
				}
			}
		} else {
			int random = rand.nextInt(11);
			switch (random) {
				case 0: {
					message.getChannel().sendMessage("Respect your elders you little shit.");
					Statistics.INSTANCE.addToStat("insulted");
					break;
				}
				case 1: {
					message.getChannel().sendMessage("I have feelings too you know. \nPiece of shit...");
					Statistics.INSTANCE.addToStat("insulted");
					break;
				}
				case 2: {
					message.getChannel().sendMessage("You're shit-talking a bot on the internet. You can do better.");
					Statistics.INSTANCE.addToStat("insulted");
					break;
				}
				case 3: {
					message.getChannel().sendMessage(EmojiManager.getForAlias("middle_finger").getUnicode());
					Statistics.INSTANCE.addToStat("insulted");
					break;
				}
				case 4: {
					message.getChannel().sendMessage("Fuck off.");
					Statistics.INSTANCE.addToStat("insulted");
					break;
				}
				case 5: {
					message.getChannel().sendMessage("no u");
					Statistics.INSTANCE.addToStat("insulted");
					break;
				}
				case 6: {
					message.getChannel().sendMessage("Oh please, like you could do better...");
					Statistics.INSTANCE.addToStat("insulted");
					break;
				}
				case 7: {
					message.getChannel().sendMessage("I don't see you trying.");
					Statistics.INSTANCE.addToStat("insulted");
					break;
				}
				case 8: {
					message.getChannel().sendMessage("Are you trying to ruin my day? I'm a bot, good luck with that.");
					Statistics.INSTANCE.addToStat("insulted");
					break;
				}
				case 9: {
					message.getChannel().sendMessage("I'm trying, ok?!");
					Statistics.INSTANCE.addToStat("insulted");
					break;
				}
				case 10: {
					message.getChannel().sendMessage("Are you not fucking entertained?!");
					Statistics.INSTANCE.addToStat("insulted");
					break;
				}
				case 11: {
					message.getChannel().sendMessage("I'm a fucking bot. What do you expect of me?!");
					Statistics.INSTANCE.addToStat("insulted");
					break;
				}
			}
		}
	}
}
