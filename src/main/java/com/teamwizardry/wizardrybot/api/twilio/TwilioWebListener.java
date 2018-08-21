package com.teamwizardry.wizardrybot.api.twilio;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.teamwizardry.wizardrybot.api.WhatsappDiscordAdapter;
import com.twilio.Twilio;
import com.twilio.base.ResourceSet;
import com.twilio.rest.api.v2010.account.message.Media;
import com.twilio.twiml.MessagingResponse;
import com.twilio.type.PhoneNumber;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.message.Message;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static spark.Spark.get;
import static spark.Spark.post;

public class TwilioWebListener {

	private static String TwilioSID = "AC75d9becf673eab453e2879cc1a70b34a";
	private static String TwilioAuth = "8b831330db5ec28711432513eb9c819a";

	public TwilioWebListener(DiscordApi api, Channel channel) {
		Twilio.init(TwilioSID, TwilioAuth);

		get("/", (req, res) -> "Sup.");

		// REPLY
		post("/", (req, res) -> {
			channel.asTextChannel().ifPresent(textChannel -> {
				try {
					JsonObject obj = paramJson(req.body());
					if (obj.has("From") && obj.has("Body")) {
						String body = URLDecoder.decode(obj.getAsJsonPrimitive("Body").getAsString(), "UTF-8");
						String from = URLDecoder.decode(obj.getAsJsonPrimitive("From").getAsString(), "UTF-8").replace("whatsapp:", "");

						if (!from.isEmpty() && !body.isEmpty())
							textChannel.sendMessage("<WHATSAPP=" + from + ">" + body);
					}

					if (obj.has("NumMedia") && obj.has("MessageSid")) {
						System.out.println("!!-!!");
						String numMedia = obj.getAsJsonPrimitive("NumMedia").getAsString();
						int num = Integer.parseInt(numMedia);
						if (num == 0) return;

						ResourceSet<Media> mediaList = Media.reader(obj.getAsJsonPrimitive("MessageSid").getAsString()).read();

						for (Media media : mediaList) {
							textChannel.sendMessage("<WHATSAPP=>https://api.twilio.com" + media.getUri());
						}
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			});
			res.type("application/xml");
			MessagingResponse twiml = new MessagingResponse
					.Builder()
					.build();
			return twiml.toXml();
		});
	}

	public static JsonObject paramJson(String paramIn) {
		paramIn = paramIn.replaceAll("=", "\":\"");
		paramIn = paramIn.replaceAll("&", "\",\"");
		paramIn = "{\"" + paramIn + "\"}";

		return new JsonParser().parse(paramIn).getAsJsonObject();
	}

	public static void sendMessage(String number, Message message) {
		com.twilio.rest.api.v2010.account.Message.creator(
				TwilioSID,
				new PhoneNumber("whatsapp:" + number),
				new PhoneNumber("whatsapp:+14155238886"),
				WhatsappDiscordAdapter.convertMessage(message))
				.create();
	}
}
