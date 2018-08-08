package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.ICommandModule;
import com.teamwizardry.wizardrybot.api.Module;
import com.teamwizardry.wizardrybot.api.Statistics;
import com.vdurmont.emoji.EmojiManager;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;

public class ModuleAboutSelf extends Module implements ICommandModule {

	@Override
	public String getActionID() {
		return "input.about_self";
	}

	@Override
	public String getName() {
		return "About";
	}

	@Override
	public String getDescription() {
		return "Information about Professor Albus Dumbledore, the robotic wise wizard.";
	}

	@Override
	public String getUsage() {
		return "'hey albus, <ask about him>'";
	}

	@Override
	public String getExample() {
		return "'hey albus, tell me about yourself'";
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {
		EmbedBuilder embed = new EmbedBuilder().setTitle("About Me").setColor(Color.GREEN)
				.setDescription("I am Professor Albus Dumbledore, the wisest robotic wizard in all of the discords!\n"
						+ "My brain is powered by the artificially intelligent systems of Dialogflow, Google, as well as Microsoft/Bing.\n"
						+ "I was made with " + EmojiManager.getForAlias("heart").getUnicode() + " by my creator, Demoniaque.\n"
						+ "My owner made me not store any usernames or user related information in plain text! Any usernames stored have been hashed and salted securely and are not stored as is.\n"
						+ "Ask me 'hey albus, what can you do?' so I can tell you about all my abilities, young wizard!\n"
						+ "Some things I can do: I can analyze images and tell you what's or who's in them, I can define words, I can look something up on wikipedia, I can remind you of something, I can even draw functions for you, and so much more!\n"
						+ "Take a look at my brain here: [Github Link](https://github.com/TeamWizardry/Dumbledore-Bot)\n"
						+ "You can invite me with this link to your server: [Invite Link](https://discordapp.com/oauth2/authorize?client_id=348507550058283019&scope=bot&permissions=2080767089\n"
						+ "And contact my maker here: [Team Wizardry Discord Invite](https://discord.gg/wsk2PBR)");
		message.getChannel().sendMessage("", embed);
		Statistics.INSTANCE.addToStat("times_asked_about_self");
	}
}
