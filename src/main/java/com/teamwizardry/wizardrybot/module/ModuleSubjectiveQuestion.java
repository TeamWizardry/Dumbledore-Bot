package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.ICommandModule;
import com.teamwizardry.wizardrybot.api.Module;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;

import java.util.ArrayList;
import java.util.Random;

public class ModuleSubjectiveQuestion extends Module implements ICommandModule {

	private static ArrayList<String> responses = new ArrayList<>();
	private static ArrayList<String> opbResponses = new ArrayList<>();
	private static ArrayList<String> yesNoResponses = new ArrayList<>();

	static {
		responses.add("It sucks");
		responses.add("I Like'm");
		responses.add("Very childish, I must say.");
		responses.add("It's great, BELIEVE me.");
		responses.add("Many jobs being lost!");
		responses.add("Fake news. Believe me.");
		responses.add("They'll will never be satisfied... Truly bad people!");
		responses.add("One of the worst. I'd change it fast!");
		responses.add("There is no place for this kind of violence on Discord.");
		responses.add("It's great. I know a lot of great things, but this is one of the greatest. Believe me.");
		responses.add("Hopefully they will find another path.");
		responses.add("We must repeal and replace it immediately.");
		responses.add("Hopefully we will never have to use it's power.");
		responses.add("It will never change...");
		responses.add("It's totally inept!");
		responses.add("Over one billion dollars in cost to it. Sad.");

		opbResponses.add("OPB sucks.");
		opbResponses.add("Oh please... OPB is terrible.");
		opbResponses.add("OPB can suck it.");
		opbResponses.add("That's a joke, right?");
		opbResponses.add("Ha. You're kidding, right?");
		opbResponses.add("Seriously? OPB? Lmao!");
		opbResponses.add("OPB? No. Just no...");

		yesNoResponses.add("Yes. Definitely.");
		yesNoResponses.add("Perhaps...");
		yesNoResponses.add("Nope.");
		yesNoResponses.add("No");
		yesNoResponses.add("Absolutely not!");
		yesNoResponses.add("Yup!");
		yesNoResponses.add("Maybe... I'm unsure.");
		yesNoResponses.add("I don't see why not.");

	}

	@Override
	public String getActionID() {
		return "input.opinion";
	}

	@Override
	public String getName() {
		return "Opinion";
	}

	@Override
	public String getDescription() {
		return "Ask dumbledore about his opinion on something";
	}

	@Override
	public String getUsage() {
		return "hey albus, <any question ever>";
	}

	@Override
	public String getExample() {
		return "'hey albus, whats ur opinion on elad's brain?', 'hey albus, what do you think about donald trump being the president?', or 'hey albus, do you like soap?'";
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {
		boolean yesNoQuestion = false;
		String any = result.getStringParameter("any");

		String opinion = result.getStringParameter("opinion");
		if (opinion.equals("yes/no")) yesNoQuestion = true;

		if (yesNoQuestion) {
			Random rand = new Random(message.getContent().hashCode());
			int random = rand.nextInt(yesNoResponses.size() - 1);
			message.getChannel().sendMessage(yesNoResponses.get(random));
		} else if (any != null && (any.contains("opb") || any.contains("opm") || (any.contains("one") && any.contains("punch") && any.contains("bot")) || any.contains("244168517396463616"))) {
			Random rand = new Random(message.getContent().hashCode());
			int random = rand.nextInt(opbResponses.size() - 1);
			message.getChannel().sendMessage(opbResponses.get(random));
		} else {
			Random rand = new Random(message.getContent().hashCode());
			int random = rand.nextInt(responses.size() - 1);
			message.getChannel().sendMessage(responses.get(random));
		}
	}
}
