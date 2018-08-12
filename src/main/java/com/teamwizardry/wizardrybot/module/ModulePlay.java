package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;
import com.teamwizardry.wizardrybot.Keys;
import com.teamwizardry.wizardrybot.WizardryBot;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.ICommandModule;
import com.teamwizardry.wizardrybot.api.Module;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

public class ModulePlay extends Module implements ICommandModule {

	@Override
	public String getName() {
		return "Play";
	}

	@Override
	public String getDescription() {
		return "Play a song from youtube";
	}

	@Override
	public String getUsage() {
		return "<hey albus or alexa> play <song>";
	}

	@Override
	public String getExample() {
		return "this is so sad.. alexa play despacito 2";
	}

	@Override
	public boolean overrideResponseCheck() {
		return true;
	}

	@Override
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {
		Thread thread = new Thread(() -> {
			if (command.hasSaidHey() || message.getContent().toLowerCase(Locale.getDefault()).contains("alexa")) {

				String songName = result.getStringParameter("any");
				if (songName == null || songName.isEmpty()) return;

				String videoId = null;
				String title = null;
				try {
					HttpResponse<JsonNode> response = Unirest.get("https://www.googleapis.com/youtube/v3/search?q=" + URLEncoder.encode(songName, "UTF-8") +
							"&maxResults=5" +
							"&part=snippet" +
							"&key=" + Keys.YOUTUBE)
							.asJson();

					JsonElement element = new JsonParser().parse(response.getBody().toString());
					if (!element.isJsonObject()) return;
					JsonObject object = element.getAsJsonObject();

					System.out.println(object.toString());

					if (object.has("items") && object.get("items").isJsonArray()) {
						for (JsonElement itemElement : object.getAsJsonArray("items")) {
							if (!itemElement.isJsonObject()) continue;
							JsonObject itemObj = itemElement.getAsJsonObject();

							if (itemObj.has("id") && itemObj.get("id").isJsonObject()) {
								JsonObject idObj = itemObj.getAsJsonObject("id");

								if (idObj.has("videoId") && idObj.get("videoId").isJsonPrimitive()) {
									videoId = idObj.getAsJsonPrimitive("videoId").getAsString();
								}
							}

							if (itemObj.has("snippet") && itemObj.get("snippet").isJsonObject()) {
								JsonObject snippetObj = itemObj.getAsJsonObject("snippet");

								if (snippetObj.has("title") && snippetObj.get("title").isJsonPrimitive()) {
									title = snippetObj.getAsJsonPrimitive("title").getAsString();
								}
							}

							break;
						}
					}

				} catch (UnirestException | UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				if (videoId == null || title == null) {
					message.getChannel().sendMessage("No video found.");
					return;
				}

				if (WizardryBot.ffmpegExe == null || WizardryBot.ffProbe == null) {
					message.getChannel().sendMessage("Ffmpeg could not be found. Yell at my maker.");
					return;
				}

				try {
					String url = "https://www.youtube.com/watch?v=" + videoId;
					File downloadDir = new File("downloads/");
					if (!downloadDir.exists()) downloadDir.mkdirs();

					File finishedMp3 = new File(downloadDir, title + ".mp3");
					if (finishedMp3.exists()) {
						message.getChannel().sendMessage(finishedMp3);
						return;
					}

					File audio = new File(downloadDir, title + ".webm");

					if (!audio.exists()) {
						System.out.println("Downloading " + title);

						File bin = new File("bin/youtube-dl.exe");
						YoutubeDL.setExecutablePath(bin.getAbsolutePath());
						YoutubeDLRequest request = new YoutubeDLRequest(url, downloadDir.getPath());
						request.setOption("format", "bestaudio");

						YoutubeDLResponse response = YoutubeDL.execute(request);
						String stdOut = response.getOut();

						System.out.println(stdOut);
						System.out.println("Finished downloading " + title);
					}

					File findAudio = findFileContainingName(downloadDir, title);

					if (findAudio == null) {
						message.getChannel().sendMessage("Something went wrong. Yell at my maker.");
						return;
					}
					if (!audio.getName().equals(findAudio.getName())) {
						findAudio.renameTo(audio);
					}

					FFmpeg ffmpeg = new FFmpeg(WizardryBot.ffmpegExe.getPath());
					FFprobe ffprobe = new FFprobe(WizardryBot.ffProbe.getPath());
					if (!audio.exists()) {
						message.getChannel().sendMessage("Couldn't download the video");
						return;
					}

					FFmpegProbeResult ffmpegResult = ffprobe.probe(audio.getPath());
					ffmpegResult.format.bit_rate = 96_000;

					FFmpegBuilder builder = new FFmpegBuilder()
							.setInput(ffmpegResult)
							.overrideOutputFiles(true)
							.addOutput("downloads/" + title + ".mp3")
							.setFormat("mp3")
							.done();

					FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

					executor.createJob(builder).run();

					audio.delete();
					File mp3 = new File(downloadDir, title + ".mp3");

					if (!mp3.exists()) {
						message.getChannel().sendMessage("Couldn't compress video");
						return;
					}

					message.getChannel().sendMessage(mp3);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

			}
		});
		thread.start();
	}

	@Override
	public String getActionID() {
		return "input.play";
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Nullable
	private File findFileContainingName(File dir, String substring) {

		if (dir.exists() && dir.isDirectory()) {
			File[] files = dir.listFiles(); //get the files in String format.
			for (File file : files) {
				if (file.getName().contains(substring))
					return file;
			}
		}

		return null;
	}
}
