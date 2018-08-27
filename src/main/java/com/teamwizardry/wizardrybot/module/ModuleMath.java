package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.ICommandModule;
import com.teamwizardry.wizardrybot.api.Module;
import com.teamwizardry.wizardrybot.api.Statistics;
import com.teamwizardry.wizardrybot.api.imgur.ImgurUploader;
import com.teamwizardry.wizardrybot.api.math.Vec2d;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.Function;
import org.mariuszgromada.math.mxparser.parsertokens.KeyWord;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ModuleMath extends Module implements ICommandModule {

	@Override
	public String getActionID() {
		return null;
	}

	@Override
	public String getName() {
		return "Math";
	}

	@Override
	public String getDescription() {
		return "Will calculate the given equation or graph it";
	}

	@Override
	public String getUsage() {
		return "hey albus, calculate <equation>";
	}

	@Override
	public String getExample() {
		return "'hey albus, math 42+69' or hey albus, calc 99^34' or 'hey alby, escapee sin(x^2) * cos(x/2)'";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"calc", "calculate", "escapee", "math", "graph"};
	}

	@Override
	public boolean onCommand(DiscordApi api, Message message, Command command, Result result, boolean whatsapp) {

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		String tEquation = command.getArguments().toLowerCase();
		if (tEquation.isEmpty()) {
			return false;
		}

		double tInputScale = 100;
		double tInputPrecision = 0.01;
		if (tEquation.contains(";")) {
			String[] parts = tEquation.split(";");
			if (parts.length <= 1) {
				return false;
			}
			if (parts.length == 2) {
				tInputScale = Math.abs(Double.parseDouble(parts[0].trim()));
				tEquation = parts[1].trim();
			} else if (parts.length == 3) {
				tInputScale = Math.abs(Double.parseDouble(parts[0].trim()));
				tInputPrecision = Math.abs(Double.parseDouble(parts[1].trim()));
				tEquation = parts[2].trim();
			}
		}

		final double inputScale = tInputScale;
		final double inputPrecision = tInputPrecision;
		String equation = tEquation;

		long origin = System.currentTimeMillis();

		Function f = new Function(equation);
		Expression dummyExpression = new Expression("f(0)", f);

		StringBuilder keyWordSplitter = new StringBuilder();
		List<KeyWord> keywords = dummyExpression.getKeyWords();
		for (KeyWord keyWord : keywords) {
			if (keyWord == null || keyWord.wordString == null) continue;

			if (equation.contains(keyWord.wordString)) {
				if (keyWord.wordString.equals("\\")) {
					keyWordSplitter.append("\\");
				} else keyWordSplitter.append(keyWord.wordString);
			}
		}

		List<String> tokens = new ArrayList<>();//(Collections.arrayToList(token.contains(",") ? token.split(",") : new String[]{token}));
		StringTokenizer stringTokenizer = new StringTokenizer(equation, keyWordSplitter.toString());
		while (stringTokenizer.hasMoreTokens()) {
			String nonKeyword = stringTokenizer.nextToken();
			String letter = nonKeyword.replaceAll("[^A-Za-z]+", "").trim();
			if (!letter.isEmpty()) tokens.add(letter);
		}

		stringTokenizer = new StringTokenizer(equation, keyWordSplitter.toString());
		while (stringTokenizer.hasMoreTokens()) {
			String nonKeyword = stringTokenizer.nextToken();
			for (String token1 : tokens) {
				if (nonKeyword.contains(token1) && !nonKeyword.equals(token1)) {
					StringBuilder reconstructedNonKeyWord = new StringBuilder(nonKeyword);

					if (nonKeyword.startsWith(token1)) {
						reconstructedNonKeyWord.insert(1, "*");
					} else if (nonKeyword.endsWith(token1)) {
						reconstructedNonKeyWord.insert(reconstructedNonKeyWord.length() - 1, "*");
					} else {
						reconstructedNonKeyWord.insert(reconstructedNonKeyWord.indexOf(token1) + 1, "*");
						reconstructedNonKeyWord.insert(reconstructedNonKeyWord.indexOf(token1), "*");
					}

					equation = equation.replace(nonKeyword, reconstructedNonKeyWord);
				}
			}
		}

		boolean regularMaths = true;

		if (!tokens.isEmpty()) {
			if (!equation.startsWith("f(") && !equation.contains("=")) {

				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < tokens.size(); i++) {
					String token = tokens.get(i);
					builder.append(token);
					if (i < tokens.size() - 1) {
						builder.append(", ");
					}
				}

				equation = "f(" + builder.toString() + ") = " + equation;
				regularMaths = false;
			}
		}

		message.getChannel().sendMessage("`" + equation + "`" + " - " + "f(" + StringUtils.repeat("0,", tokens.size() - 1) + "0" + ")");

		Expression testExpression = new Expression("f(" + StringUtils.repeat("0,", tokens.size() - 1) + "0" + ")", f);
		if (!testExpression.checkSyntax()) {
			message.getChannel().sendMessage("Something's wrong with that equation.");
			System.out.println(testExpression.getErrorMessage());
			return true;
		}

		if (regularMaths) {
			Expression expression = new Expression(equation);
			message.getChannel().sendMessage("`" + expression.getExpressionString() + "`\n=" + expression.calculate());
			Statistics.INSTANCE.addToStat("equations_solved");
			return true;
		}

		BufferedImage image = new BufferedImage(1920, 1920, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setBackground(Color.WHITE);
		graphics.clearRect(0, 0, 1920, 1920);

		graphics.setColor(Color.BLACK);
		graphics.setStroke(new BasicStroke(1));
		double verticalScale = Math.min(inputScale, 1000);
		double precision = Math.max(0.1, inputPrecision);

		for (double x = 0; x < 1920.0 / 2.0; x += verticalScale) {
			graphics.drawLine((int) (x + (1920.0 / 2.0)), 0, (int) (x + (1920.0 / 2.0)), 1920);
		}
		for (double x = 0; x > -1920.0 / 2.0; x -= verticalScale) {
			graphics.drawLine((int) (x + (1920.0 / 2.0)), 0, (int) (x + (1920.0 / 2.0)), 1920);
		}

		for (double y = 0; y < 1920.0 / 2.0; y += verticalScale) {
			graphics.drawLine(0, (int) (y + (1920.0 / 2.0)), 1920, (int) (y + (1920.0 / 2.0)));
		}

		for (double y = 0; y > -1920.0 / 2.0; y -= verticalScale) {
			graphics.drawLine(0, (int) (y + (1920.0 / 2.0)), 1920, (int) (y + (1920.0 / 2.0)));
		}

		graphics.setStroke(new BasicStroke(3));
		graphics.drawLine((int) (1920.0 / 2.0), 1920, (int) (1920.0 / 2.0), 0);
		graphics.drawLine(1920, (int) (1920.0 / 2.0), 0, (int) (1920.0 / 2.0));

		graphics.setStroke(new BasicStroke(5));
		message.getChannel().sendMessage("Graphing your equation... This might take a minute.");

		Vec2d vec;
		Vec2d prevPoint = null;
		for (double x = -1920.0 / 2.0; x < 1920.0 / 2.0; x += precision) {

			Expression e = new Expression("f(" + x + ")", f);
			double y = -e.calculate();

			if (Double.isFinite(y) && !Double.isNaN(y)) {
				vec = new Vec2d(x, y);
				if (prevPoint == null) {
					prevPoint = vec;
				}
				double x1 = vec.x, y1 = vec.y;

				vec = new Vec2d((vec.x * verticalScale) + 1920.0 / 2.0, (vec.y * verticalScale) + 1920 / 2.0);
				prevPoint = new Vec2d((prevPoint.x * verticalScale) + 1920.0 / 2.0, (prevPoint.y * verticalScale) + 1920 / 2.0);
				graphics.drawLine((int) prevPoint.x, (int) prevPoint.y, (int) vec.x, (int) vec.y);
				prevPoint = new Vec2d(x1, y1);
			} else prevPoint = null;
		}

		graphics.dispose();
		message.getChannel().sendMessage("done");

		try {
			File file = new File("downloads/graph_" + UUID.randomUUID().toString() + ".png");
			if (!file.exists()) file.createNewFile();
			ImageIO.write(image, "png", file);

			String url = ImgurUploader.upload(file);

			message.getChannel().sendMessage("Done! That took me " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - origin) + " seconds.");
			EmbedBuilder builder = new EmbedBuilder();
			builder.setTitle("Grapher")
					.setDescription(tEquation)
					.setImage(url)
					.setUrl(url);
			message.getChannel().sendMessage(builder);
			Statistics.INSTANCE.addToStat("graphs_created");
		} catch (IOException e) {
			message.getChannel().sendMessage(e.getMessage());
		}

		return true;
	}
}