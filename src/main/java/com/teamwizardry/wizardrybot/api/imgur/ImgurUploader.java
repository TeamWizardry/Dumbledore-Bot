package com.teamwizardry.wizardrybot.api.imgur;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.teamwizardry.wizardrybot.Keys;
import com.teamwizardry.wizardrybot.api.Statistics;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Controls all interface with the web and the Imgur API.
 *
 * @author DV8FromTheWorld (Austin Keener)
 * @version v1.0.0  July 16, 2014
 */
public class ImgurUploader {
	public static final String UPLOAD_API_URL = "https://api.imgur.com/3/image";
	public static final String ALBUM_API_URL = "https://api.imgur.com/3/album";
	public static final int MAX_UPLOAD_ATTEMPTS = 3;

	//CHANGE TO @CLIENT_ID@ and replace with buildscript.
	private final static String CLIENT_ID = Keys.IMGUR;

	/**
	 * Takes a url and uploads it to Imgur.
	 * Does not check to see if the url is an image, this should be done
	 * before the url is passed to this method.
	 *
	 * @param file The image to be uploaded to Imgur.
	 * @return Link
	 */
	@Nullable
	public static String upload(File file) {
		HttpURLConnection conn = getHttpConnection(UPLOAD_API_URL);
		writeToConnection(conn, "image=" + toBase64(file));
		String response = getResponse(conn);

		JsonElement element = new JsonParser().parse(response);
		if (element.isJsonObject()) {
			JsonObject imgur = element.getAsJsonObject();
			if (imgur.has("data") && imgur.get("data").isJsonObject()) {
				JsonObject data = imgur.getAsJsonObject("data");
				if (data.has("link") && data.get("link").isJsonPrimitive()) {
					Statistics.INSTANCE.addToStat("images_uploaded");
					return data.getAsJsonPrimitive("link").getAsString().replace("\\", "");
				}
			}
		}
		return null;
	}

	/**
	 * Takes a url and uploads it to Imgur.
	 * Does not check to see if the url is an image, this should be done
	 * before the url is passed to this method.
	 *
	 * @param URL The image to be uploaded to Imgur.
	 * @return The JSON response from Imgur.
	 */
	@Nullable
	public static String upload(String URL) {
		HttpURLConnection conn = getHttpConnection(UPLOAD_API_URL);
		writeToConnection(conn, "image=" + URL);
		String response = getResponse(conn);

		JsonElement element = new JsonParser().parse(response);
		if (element.isJsonObject()) {
			JsonObject imgur = element.getAsJsonObject();
			if (imgur.has("data") && imgur.get("data").isJsonObject()) {
				JsonObject data = imgur.getAsJsonObject("data");
				if (data.has("link") && data.get("link").isJsonPrimitive()) {
					Statistics.INSTANCE.addToStat("images_uploaded");
					return data.getAsJsonPrimitive("link").getAsString().replace("\\", "");
				}
			}
		}
		return null;
	}

	@Nullable
	public static JsonObject uploadWithJson(File file) {
		HttpURLConnection conn = getHttpConnection(UPLOAD_API_URL);
		writeToConnection(conn, "image=" + toBase64(file));
		String response = getResponse(conn);

		JsonElement element = new JsonParser().parse(response);
		if (element.isJsonObject()) {
			return element.getAsJsonObject();
		}
		return null;
	}

	@Nullable
	public static JsonObject uploadWithJson(String URL) {
		HttpURLConnection conn = getHttpConnection(UPLOAD_API_URL);
		writeToConnection(conn, "image=" + URL);
		String response = getResponse(conn);

		JsonElement element = new JsonParser().parse(response);
		if (element.isJsonObject()) {
			return element.getAsJsonObject();
		}
		return null;
	}
	/**
	 * Creates an album on Imgur.
	 * Does not check if imageIds are valid images on Imgur.
	 *
	 * @param imageIds A list of ids of images on Imgur.
	 * @return The JSON response from Imgur.
	 */
	public static String createAlbum(List<String> imageIds) {
		HttpURLConnection conn = getHttpConnection(ALBUM_API_URL);
		String ids = "";
		for (String id : imageIds) {
			if (!ids.equals("")) {
				ids += ",";
			}
			ids += id;
		}
		writeToConnection(conn, "ids=" + ids);
		return getResponse(conn);
	}

	/**
	 * Converts a url to a Base64 String.
	 *
	 * @param file The url to be converted.
	 * @return The url as a Base64 String.
	 */
	private static String toBase64(File file) {
		try {
			byte[] b = new byte[(int) file.length()];
			FileInputStream fs = new FileInputStream(file);
			fs.read(b);
			fs.close();
			return URLEncoder.encode(DatatypeConverter.printBase64Binary(b), "UTF-8");
		} catch (IOException e) {
			throw new WebException(StatusCode.UNKNOWN_ERROR, e);
		}
	}

	/**
	 * Creates and sets up an HttpURLConnection for use with the Imgur API.
	 *
	 * @param url The URL to connect to. (check Imgur API for correct URL).
	 * @return The newly created HttpURLConnection.
	 */
	private static HttpURLConnection getHttpConnection(String url) {
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Authorization", "Client-ID " + CLIENT_ID);
			conn.setRequestProperty("User-Agent", "Google Chrome Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36.");
			conn.setReadTimeout(100000);
			conn.connect();
			return conn;
		} catch (UnknownHostException e) {
			throw new WebException(StatusCode.UNKNOWN_HOST, e);
		} catch (IOException e) {
			throw new WebException(StatusCode.UNKNOWN_ERROR, e);
		}
	}

	/**
	 * Sends the provided message to the connection as uploaded data.
	 *
	 * @param conn    The connection to send the data to.
	 * @param message The data to upload.
	 */
	private static void writeToConnection(HttpURLConnection conn, String message) {
		OutputStreamWriter writer;
		try {
			writer = new OutputStreamWriter(conn.getOutputStream());
			writer.write(message);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw new WebException(StatusCode.UNKNOWN_ERROR, e);
		}
	}

	/**
	 * Gets the response from the connection, Usually in the format of a JSON string.
	 *
	 * @param conn The connection to listen to.
	 * @return The response, usually as a JSON string.
	 */
	private static String getResponse(HttpURLConnection conn) {
		StringBuilder str = new StringBuilder();
		BufferedReader reader;
		try {
			if (conn.getResponseCode() != StatusCode.SUCCESS.getHttpCode()) {
				System.out.println(conn.getResponseMessage());
				throw new WebException(conn.getResponseCode());
			}
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				str.append(line);
			}
			reader.close();
		} catch (IOException e) {
			throw new WebException(StatusCode.UNKNOWN_ERROR, e);
		}
		if (str.toString().equals("")) {
			throw new WebException(StatusCode.UNKNOWN_ERROR);
		}
		return str.toString();
	}
}