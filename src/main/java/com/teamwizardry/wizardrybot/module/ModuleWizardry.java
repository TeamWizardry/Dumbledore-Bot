package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.teamwizardry.wizardrybot.api.*;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;

public class ModuleWizardry extends Module implements ICommandModule {

	@Override
	public boolean overrideResponseCheck() {
		return false;
	}

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public String getName() {
		return "Wizardry";
	}

	@Override
	public String getDescription() {
		return "Ask questions about wizardry";
	}

	@Override
	public String getUsage() {
		return null;
	}

	@Override
	public String getExample() {
		return null;
	}

	@Override
	public String getActionID() {
		return null;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"deal with them", "deal with her", "deal with him", "help them", "help him", "help her"};
	}

	@Override
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {

		final boolean[] helped = {false};
		message.getChannel().getMessagesAsStream().limit(5).filter(message12 -> {
			boolean[] b = new boolean[]{true};
			message12.getUserAuthor().ifPresent(user -> {
				if (user.isBot() || user.getId() == message12.getAuthor().getId()) b[0] = false;
			});
			return b[0];
		}).forEach(message1 -> {
			String content = message1.getContent();
			if (content.length() >= 255) content = content.substring(0, 254);

			Result result1 = WizardryAI.INSTANCE.think(content);
			if (result1 == null) return;

			if (result1.getScore() < 0.3) return;
			helped[0] = true;

			switch (result1.getAction()) {
				case "input.crash":
					if (result1.getScore() >= 0.9)
						message1.getChannel().sendMessage("You're experiencing crashes it seems. Hm...");
					else if (result1.getScore() >= 0.5)
						message1.getChannel().sendMessage("I'm not entirely sure, but I think you're saying you're experiencing crashes. Hm...");
					else {
						message1.getChannel().sendMessage("I don't understand what you said. I think you're experiencing crashes but I got no clue. Rephrase what you said please.");
						return;
					}

					message1.getChannel().sendMessage("Please give us your crash report. Go to your crashes folder and give us the latest txt file here.");
					message1.getChannel().sendMessage("In any case, be sure you UPDATED both Wizardry AND LibrarianLib to the latest version!");
					Statistics.INSTANCE.addToStat("wizardry_crashes_helped_with");
					break;
				case "input.problems":
					if (result1.getScore() >= 0.9)
						message1.getChannel().sendMessage("You're experiencing problems with wizardry it seems. Hm...");
					else if (result1.getScore() >= 0.5)
						message1.getChannel().sendMessage("I'm not entirely sure, but I think you're saying you're having problems with wizardry. Hm...");
					else {
						message1.getChannel().sendMessage("I don't understand what you said. I think you're having problems with wizardry but I got no clue. Rephrase what you said please.");
						return;
					}

					message1.getChannel().sendMessage("Please tell me what your problem in a short and clear way. Are you experiencing crashes? Having trouble crafting spells? Can't figure out your mana? Can't cast spells? Ask away.");
					Statistics.INSTANCE.addToStat("wizardry_problems_asked_about");
					break;
				case "input.cant_build_structure":

					if (result1.getScore() >= 0.9)
						message1.getChannel().sendMessage("You're having trouble with the structures it seems. Hm...");
					else if (result1.getScore() >= 0.5)
						message1.getChannel().sendMessage("I'm not entirely sure, but I think you're saying you're having trouble with the structures. Hm...");
					else {
						message1.getChannel().sendMessage("I don't understand what you said. I think you're having trouble with the structures but I got no clue. Rephrase what you said please.");
						return;
					}

					message1.getChannel().sendMessage("- The white blocks in the structures are quartz blocks. We might be changing the quartz to Nacre blocks in future version of the mod, but for now, the blocks are all pure vanilla QUARTZ.");
					message1.getChannel().sendMessage("- If you don't know how to build or finish the structure, right click the main block (crafting plate or mana battery, etc) and you will see the structure's highlight in the world clearly.");
					message1.getChannel().sendMessage("- If you see red dots, that means the block where the red dot is is NOT correct. You placed the WRONG block there.");
					Statistics.INSTANCE.addToStat("wizardry_structure_building_problems_helped_with");
					break;
				case "input.no_mana":
					if (result1.getScore() >= 0.9)
						message1.getChannel().sendMessage("You're having trouble with your mana it seems. Hm...");
					else if (result1.getScore() >= 0.5)
						message1.getChannel().sendMessage("I'm not entirely sure, but I think you're saying you're having trouble with your mana. Hm...");
					else {
						message1.getChannel().sendMessage("I don't understand what you said. I think you're having trouble with your mana but I got no clue. Rephrase what you said please.");
						return;
					}

					message1.getChannel().sendMessage("- Be sure you are wearing a Halo or Crude Halo to enable you to cast spells and store mana. Your bar will NOT appear without a Halo.");
					message1.getChannel().sendMessage("- If your mana bar is empty, inject yourself with mana syringes to fill yourself up.");
					message1.getChannel().sendMessage("- If your mana depletes too fast, then UPDATE THE MOD to the LATEST version. Spell costs have been heavily reduced.");
					message1.getChannel().sendMessage("- If you hate having to inject yourself with mana so much, craft yourself a real Halo. It's recipe is TEMPORARY and will be removed in future version but it will automatically regenerate mana.");
					message1.getChannel().sendMessage("wizardry_mana_problems_helped_with");
					Statistics.INSTANCE.addToStat("wizardry_crashes_helped_with");
					break;
				case "input.mana_orbs_shattering":
					if (result1.getScore() >= 0.9)
						message1.getChannel().sendMessage("You're wondering why your mana orbs keep shattering seems.");
					else if (result1.getScore() >= 0.5)
						message1.getChannel().sendMessage("I'm not entirely sure, but I think you're wondering why your mana orbs keep shattering.");
					else {
						message1.getChannel().sendMessage("I don't understand what you said. I think you're wondering why your mana orbs keep shattering but I got no clue. Rephrase what you said please.");
						return;
					}

					message1.getChannel().sendMessage("- The mana orbs are SUPPOSED to shatter in the battery. It means the mana was successfully sucked into the Mana Battery!");
					message1.getChannel().sendMessage("- If the orbs no longer shatter and the Mana Battery begins violently shaking, it means that your battery is completely full of mana.");
					Statistics.INSTANCE.addToStat("wizardry_mana_orb_problems_helped_with");
					break;
				case "input.spell_not_working":
					if (result1.getScore() >= 0.9)
						message1.getChannel().sendMessage("You're spells aren't working it seems. Hm...");
					else if (result1.getScore() >= 0.5)
						message1.getChannel().sendMessage("I'm not entirely sure, but I think you're spells aren't working. Hm...");
					else {
						message1.getChannel().sendMessage("I don't understand what you said. I think you're spells aren't working but I got no clue. Rephrase what you said please.");
						return;
					}

					message1.getChannel().sendMessage("- Be sure you are wearing a Halo or Crude Halo to enable you to cast spells and store mana. Your bar will NOT appear without a Halo.");
					message1.getChannel().sendMessage("- If your mana bar is empty, inject yourself with mana syringes to fill yourself up.");
					message1.getChannel().sendMessage("- If your mana depletes too fast, then UPDATE THE MOD to the LATEST version. Spell costs have been heavily reduced.");
					message1.getChannel().sendMessage("- Be sure you're wearing a cape! It will reduce spell costs the longer you wear it.");
					message1.getChannel().sendMessage("- If all the above aren't fixing it, then your spell might be too expensive to cast simply.");
					message1.getChannel().sendMessage("- If you're 100% POSITIVE you can afford your spell, take a screenshot of your staff's tooltip and post it here.");

					Statistics.INSTANCE.addToStat("wizardry_dysfunctional_spells_helped_with");
					break;
				case "input.spell_not_crafting":
					if (result1.getScore() >= 0.9)
						message1.getChannel().sendMessage("You're spells aren't crafting it seems. Hm...");
					else if (result1.getScore() >= 0.5)
						message1.getChannel().sendMessage("I'm not entirely sure, but I think you're spells aren't crafting. Hm...");
					else {
						message1.getChannel().sendMessage("I don't understand what you said. I think you're spells aren't crafting but I got no clue. Rephrase what you said please.");
						return;
					}

					message1.getChannel().sendMessage("- Be sure you put MANA ORBS in the PEARL HOLDERS in your structures. Do NOT put pearls.");
					message1.getChannel().sendMessage("- If mana isn't flowing from your Mana Battery to your Crafting Altar, add more mana orbs to your Mana Battery.");
					message1.getChannel().sendMessage("- Be sure to throw a Nacre Pearl once you finish adding all the spell recipe items required to begin the crafting.");
					message1.getChannel().sendMessage("- If crafting just pauses or stops, add more mana to your altar or battery via mana orbs in the pearl holders.");
					message1.getChannel().sendMessage("- If the orbs no longer shatter and the Mana Battery begins violently shaking, it means that your battery is completely full of mana.");
					Statistics.INSTANCE.addToStat("wizardry_spell_craftings_helped_with");
					break;
				default:
					message1.getChannel().sendMessage("I have no idea what you're problem is.");
					Statistics.INSTANCE.addToStat("wizardry_no_idea_about_problems");
					break;
			}
		});
		if (!helped[0])
			message.getChannel().sendMessage("I don't see anyone to help here.");
	}
}
