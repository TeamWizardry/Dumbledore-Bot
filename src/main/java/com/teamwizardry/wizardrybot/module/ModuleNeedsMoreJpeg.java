package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.teamwizardry.wizardrybot.api.*;
import com.teamwizardry.wizardrybot.api.imgur.ImgurUploader;
import de.btobastian.javacord.DiscordApi;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageHistory;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ModuleNeedsMoreJpeg extends Module implements ICommandModule {

	@Override
	public boolean overrideResponseCheck() {
		return true;
	}

	@Override
	public int getPriority() {
		return -1;
	}

	@Override
	public String getName() {
		return "Needs More Jpeg";
	}

	@Override
	public String getDescription() {
		return "Add more jpeg to the last image shared within 20 messages.";
	}

	@Override
	public String getUsage() {
		return "'<ask for more jpeg>'";
	}

	@Override
	public String getExample() {
		return "'needs more jpeg' or 'add more jpeg' or 'more jpeg pls'";
	}

	@Override
	public String getActionID() {
		return "input.jpeg";
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {
		ThreadManager.INSTANCE.addThread(new Thread(() -> {
			try {
				MessageHistory messageHistory = message.getChannel().getHistory(20).get();
				ArrayList<Message> msges = (ArrayList<Message>) messageHistory.getMessages();
				Collections.reverse(msges);

				URL url = null;
				for (Message msg : msges) {
					String matchedURL = Utils.findURLInString(msg.getContent());
					if (matchedURL != null && !matchedURL.isEmpty() && Domains.INSTANCE.isLinkWhitelisted(matchedURL)) {
						url = new URL(matchedURL);
						break;
					}
				}
				if (url == null) {
					System.out.println("Needs more jpeg: Couldn't find any image");
					return;
				}

				UUID uuid = UUID.randomUUID();
				URLConnection openConnection = url.openConnection();
				openConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (iPad; U; CPU OS 3_2_1 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Mobile/7B405");

				BufferedImage buffer = ImageIO.read(openConnection.getInputStream());

				System.out.println("Needs more jpeg: compressing...");

				File compressedImageFile = new File(uuid.toString() + ".jpg");
				OutputStream outputStream = new FileOutputStream(compressedImageFile);

				Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
				ImageWriter writer = writers.next();
				ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
				writer.setOutput(ios);
				ImageWriteParam param = writer.getDefaultWriteParam();
				param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				param.setCompressionQuality(0.1f);
				writer.write(null, new IIOImage(buffer, null, null), param);

				outputStream.close();
				ios.close();
				writer.dispose();

				System.out.println("Needs more jpeg: uploading...");
				JsonElement element = new JsonParser().parse(ImgurUploader.upload(compressedImageFile));
				if (element.isJsonObject()) {
					JsonObject imgur = element.getAsJsonObject();
					if (imgur.has("data") && imgur.get("data").isJsonObject()) {
						JsonObject data = imgur.getAsJsonObject("data");
						if (data.has("link") && data.get("link").isJsonPrimitive()) {
							message.getChannel().sendMessage(data.getAsJsonPrimitive("link").getAsString().replace("\\", ""));
						}
					}
				}
				compressedImageFile.delete();
			} catch (IOException | InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}));
	}
}
