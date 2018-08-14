package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.teamwizardry.wizardrybot.api.*;
import com.teamwizardry.wizardrybot.api.imgur.ImgurUploader;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
	public boolean onCommand(DiscordApi api, Message message, Command command, Result result) {
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
		final String equation = tEquation;

		if (equation.contains("x")
				|| equation.contains("y")
				|| equation.contains("z")
				|| equation.contains("a")
				|| equation.contains("t")
				|| equation.contains("b")
				|| equation.contains("c")
				|| equation.contains("k")
				|| equation.contains("i")
				|| equation.contains("j")
				|| equation.contains("n")) {

			long origin = System.currentTimeMillis();

			try {
				new ExpressionBuilder(equation)
						.variables("x", "y", "z", "a", "b", "c", "k", "i", "j", "n", "m", "t", "q", "p")
						.build()
						.evaluate();
			} catch (Exception e) {
				if (e.getMessage().startsWith("Unknown function or variable")) {
					message.getChannel().sendMessage(e.getMessage());
					return true;
				}
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
			double precision = Math.max(0.01, inputPrecision);

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
			Vec2d prevPoint = null;
			message.getChannel().sendMessage("Graphing your equation... This might take a minute.");
			for (double x = -1920.0 / 2.0; x < 1920.0 / 2.0; x += precision) {

				if (System.currentTimeMillis() - origin > 10000) {
					message.getChannel().sendMessage("No. I may be a wizard but even magic can't draw that graph.");
					return true;
				}

				try {
					Expression e = new ExpressionBuilder(equation)
							.variables("x")
							.build()
							.setVariable("x", x);
					double y = -e.evaluate();

					if (Double.isFinite(y) && !Double.isNaN(y)) {
						Vec2d vec = new Vec2d(x, y);
						if (prevPoint == null) {
							prevPoint = vec;
						}
						double x1 = vec.x, y1 = vec.y;

						vec = new Vec2d((vec.x * verticalScale) + 1920.0 / 2.0, (vec.y * verticalScale) + 1920 / 2.0);
						prevPoint = new Vec2d((prevPoint.x * verticalScale) + 1920.0 / 2.0, (prevPoint.y * verticalScale) + 1920 / 2.0);
						graphics.drawLine((int) prevPoint.x, (int) prevPoint.y, (int) vec.x, (int) vec.y);
						prevPoint = new Vec2d(x1, y1);
					} else prevPoint = null;
				} catch (Exception ignored) {
				}
			}

			graphics.dispose();

			try {
				File file = new File("tempFile.png");
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

		} else {
			try {
				Expression expression = new ExpressionBuilder(equation).build();
				message.getChannel().sendMessage(expression.evaluate() + "");
			} catch (Exception e) {
				message.getChannel().sendMessage(e.getMessage());
			}
		}

		return true;
	}
}