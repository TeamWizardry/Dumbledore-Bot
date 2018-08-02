package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.teamwizardry.wizardrybot.api.*;
import com.teamwizardry.wizardrybot.api.imgur.ImgurUploader;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class ModuleNeedsMoreJpeg extends Module implements ICommandModule {

	@Override
	public boolean overrideResponseCheck() {
		return true;
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

	public static BufferedImage resize(BufferedImage img, int newW, int newH) {
		Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
		BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();

		return dimg;
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public String getActionID() {
		return "input.needs_more_jpeg";
	}

	@Override
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {
		ThreadManager.INSTANCE.addThread(new Thread(() -> {
			try {

				List<BufferedImage> images = Utils.stupidVerboseImageSearch(message);
				if (images.isEmpty()) return;
				UUID uuid = UUID.randomUUID();

				BufferedImage buffer = images.get(0);
				int originalHeight = buffer.getHeight();
				int originalWidth = buffer.getWidth();

				buffer = resize(buffer, 1280, originalHeight * 720 / originalWidth);

				System.out.println("Needs more jpeg: compressing...");

				File compressedImageFile = new File(uuid.toString() + ".jpeg");
				OutputStream outputStream = new FileOutputStream(compressedImageFile);

				Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
				ImageWriter writer = writers.next();
				ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
				writer.setOutput(ios);
				ImageWriteParam param = writer.getDefaultWriteParam();
				param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				param.setCompressionQuality(0f);
				writer.write(null, new IIOImage(buffer, null, null), param);

				outputStream.close();
				ios.close();
				writer.dispose();

				BufferedImage image = ImageIO.read(compressedImageFile);
				image = resize(image, originalWidth, originalHeight);
				ImageIO.write(image, "jpeg", compressedImageFile);

				System.out.println("Needs more jpeg: uploading...");
				JsonElement element = new JsonParser().parse(ImgurUploader.upload(compressedImageFile));
				if (element.isJsonObject()) {
					JsonObject imgur = element.getAsJsonObject();
					if (imgur.has("data") && imgur.get("data").isJsonObject()) {
						JsonObject data = imgur.getAsJsonObject("data");
						if (data.has("link") && data.get("link").isJsonPrimitive()) {
							message.getChannel().sendMessage(data.getAsJsonPrimitive("link").getAsString().replace("\\", ""));
							Statistics.INSTANCE.addToStat("jpeged_images");
						}
					}
				}
				compressedImageFile.delete();
			} catch (Exception e) {
				e.printStackTrace();
				message.getChannel().sendMessage("That's enough jpeg...");
			}
		}));
	}
}
