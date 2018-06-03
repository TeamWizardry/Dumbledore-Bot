package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.ICommandModule;
import com.teamwizardry.wizardrybot.api.Module;
import com.teamwizardry.wizardrybot.api.ThreadManager;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;

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

				message.getChannel().sendMessage("TODO");

				//MessageHistory messageHistory = message.getChannel().getHistory(20).get();
				//ArrayList<Message> msges = (ArrayList<Message>) messageHistory.getMessages();
				//Collections.reverse(msges);
//
				//URL url = null;
				//for (Message msg : msges) {
				//	String matchedURL = Utils.findURLInString(msg.getContent());
				//	if (matchedURL != null && !matchedURL.isEmpty() && Domains.INSTANCE.isLinkWhitelisted(matchedURL)) {
				//		url = new URL(matchedURL);
				//		break;
				//	}
				//}
				//if (url == null) {
				//	System.out.println("Needs more jpeg: Couldn't find any image");
				//	return;
				//}
//
				//UUID uuid = UUID.randomUUID();
				//URLConnection openConnection = url.openConnection();
				//openConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (iPad; U; CPU OS 3_2_1 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Mobile/7B405");
//
				//BufferedImage buffer = ImageIO.read(openConnection.getInputStream());
//
				//System.out.println("Needs more jpeg: compressing...");
//
				//File compressedImageFile = new File(uuid.toString() + ".jpg");
				//OutputStream outputStream = new FileOutputStream(compressedImageFile);
//
				//Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
				//ImageWriter writer = writers.next();
				//ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
				//writer.setOutput(ios);
				//ImageWriteParam param = writer.getDefaultWriteParam();
				//param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				//param.setCompressionQuality(0.1f);
				//writer.write(null, new IIOImage(buffer, null, null), param);
//
				//outputStream.close();
				//ios.close();
				//writer.dispose();
//
				//System.out.println("Needs more jpeg: uploading...");
				//JsonElement element = new JsonParser().parse(ImgurUploader.upload(compressedImageFile));
				//if (element.isJsonObject()) {
				//	JsonObject imgur = element.getAsJsonObject();
				//	if (imgur.has("data") && imgur.get("data").isJsonObject()) {
				//		JsonObject data = imgur.getAsJsonObject("data");
				//		if (data.has("link") && data.get("link").isJsonPrimitive()) {
				//			message.getChannel().sendMessage(data.getAsJsonPrimitive("link").getAsString().replace("\\", ""));
				//		}
				//	}
				//}
				//compressedImageFile.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}));
	}
}
