package com.teamwizardry.wizardrybot.module.cluedo;

import com.google.common.collect.HashMultimap;
import com.teamwizardry.wizardrybot.api.Utils;
import com.teamwizardry.wizardrybot.api.math.EnumCardinal;
import com.teamwizardry.wizardrybot.api.math.EnumDiagonal;
import com.teamwizardry.wizardrybot.api.math.Vec2d;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.teamwizardry.wizardrybot.api.math.EnumCardinal.*;
import static com.teamwizardry.wizardrybot.module.cluedo.EnumTile.*;

public class Board {

	public EnumTile[][] boardGrid = new EnumTile[22][22];
	public BufferedImage imgBoard;
	public File file;
	private Set<Vec2d> entrances = new HashSet<>();
	private HashMultimap<EnumTile, Vec2d> rooms = HashMultimap.create();

	public Board() {
		for (int x = 0; x < 22; x++) {
			for (int y = 0; y < 22; y++) {
				boardGrid[x][y] = FLOOR;

				if (x <= 5 && y <= 4) {
					boardGrid[x][y] = KITCHEN;
					rooms.put(KITCHEN, new Vec2d(x, y));
				} else if ((y == 7 && x <= 4) || (y > 7 && y <= 13 && x <= 7)) {
					boardGrid[x][y] = DINING_ROOM;
					rooms.put(DINING_ROOM, new Vec2d(x, y));
				} else if (y >= 17 && x <= 6) {
					boardGrid[x][y] = LOUNGE;
					rooms.put(LOUNGE, new Vec2d(x, y));
				} else if ((x >= 10 && x <= 12 && y == 0) || (x >= 8 && x <= 14 && y >= 1 && y <= 5)) {
					boardGrid[x][y] = BALLROOM;
					rooms.put(BALLROOM, new Vec2d(x, y));
				} else if (y >= 8 && y <= 14 && x >= 10 && x <= 14) {
					boardGrid[x][y] = FINAL_ACCUSATION;
					rooms.put(FINAL_ACCUSATION, new Vec2d(x, y));
				} else if (y >= 16 && x >= 9 && x <= 14) {
					boardGrid[x][y] = HALL;
					rooms.put(HALL, new Vec2d(x, y));
				} else if ((x >= 17 && y <= 2) || (x >= 18 && x <= 21 && y == 3)) {
					boardGrid[x][y] = CONSERVATORY;
					rooms.put(CONSERVATORY, new Vec2d(x, y));
				} else if (y >= 6 && y <= 10 && x >= 17) {
					boardGrid[x][y] = BILLIARD_ROOM;
					rooms.put(BILLIARD_ROOM, new Vec2d(x, y));
				} else if ((y >= 12 && y <= 16 && x >= 17) || (x == 16 && y >= 13 && y <= 15)) {
					boardGrid[x][y] = LIBRARY;
					rooms.put(LIBRARY, new Vec2d(x, y));
				} else if (x >= 16 && y >= 19) {
					boardGrid[x][y] = STUDY;
					rooms.put(STUDY, new Vec2d(x, y));
				}
			}
		}

		entrances.add(new Vec2d(4, 4));
		entrances.add(new Vec2d(8, 4));
		entrances.add(new Vec2d(9, 5));
		entrances.add(new Vec2d(13, 5));
		entrances.add(new Vec2d(14, 4));
		entrances.add(new Vec2d(17, 2));
		entrances.add(new Vec2d(20, 6));
		entrances.add(new Vec2d(17, 7));
		entrances.add(new Vec2d(7, 10));
		entrances.add(new Vec2d(19, 12));
		entrances.add(new Vec2d(4, 13));
		entrances.add(new Vec2d(16, 14));
		entrances.add(new Vec2d(6, 17));
		entrances.add(new Vec2d(11, 16));
		entrances.add(new Vec2d(12, 16));
		entrances.add(new Vec2d(14, 19));
		entrances.add(new Vec2d(17, 19));

		imgBoard = new BufferedImage(2200, 2200, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = imgBoard.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);

		Map<?, ?> desktopHints = (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
		if (desktopHints != null) {
			graphics.setRenderingHints(desktopHints);
		}

		for (int x = 0; x < 22; x++) {
			for (int y = 0; y < 22; y++) {
				int rX = x * 100;
				int rY = y * 100;

				if (boardGrid[x][y] == FLOOR) {
					graphics.setColor(Color.BLACK);
					graphics.fillRect(rX, rY, 100, 100);

					graphics.setColor(Color.GRAY);
					graphics.fillRect(rX + 6, rY + 6, 100 - 12, 100 - 12);
				}
			}
		}

		for (int x = 0; x < 22; x++) {
			for (int y = 0; y < 22; y++) {
				int rX = x * 100;
				int rY = y * 100;

				if (boardGrid[x][y].isRoom()) {
					graphics.setColor(Color.WHITE);
					graphics.fillRect(rX, rY, 100, 100);
				}
			}
		}

		final int wallHeight = 70;
		final int wallThickness = 20;
		for (int x = 0; x < 22; x++) {
			for (int y = 0; y < 22; y++) {
				int vX = x * 100;
				int vY = y * 100;

				Vec2d vec = new Vec2d(x, y);

				int finalY = y;
				int finalX = x;
				if (boardGrid[x][y].isRoom()) {

					for (EnumCardinal cardinal : EnumCardinal.values()) {
						if (getTile(vec.add(cardinal), FLOOR) == FLOOR) {
							graphics.translate(vX + 50, vY + 50);

							graphics.setColor(Color.ORANGE);
							entrances.forEach(vec1 -> {
								if (vec1.x == finalX && vec1.y == finalY)
									graphics.setColor(Color.RED);
							});

							Vec2d offset = cardinal.getVector();
							Vec2d perp = offset.perpendicular();
							Vec2d translate = offset.sub(perp).mul(50);
							Vec2d relative = perp.mul(100).add(cardinal.getVector().opposite().mul(wallThickness));
							Vec2d unRelative = relative.add(translate);

							{
								int largestX = (int) Math.max(translate.x, unRelative.x);
								int largestY = (int) Math.max(translate.y, unRelative.y);
								int smallestX = (int) Math.min(translate.x, unRelative.x);
								int smallestY = (int) Math.min(translate.y, unRelative.y);

								Vec2d realRelative = new Vec2d(largestX, largestY).sub(smallestX, smallestY);

								graphics.fillRect(smallestX, smallestY, (int) realRelative.x, (int) realRelative.y);

								entrances.forEach(vec1 -> {
									if (vec1.x == finalX && vec1.y == finalY) {
										EnumTile tile = getTile(vec);
										if (tile.isRoom()) {
											Set<Vec2d> tiles = rooms.get(tile);
											Vec2d center = Utils.calculateCentroid(tiles).mul(100).add(50);
											Vec2d edge1 = offset.sub(perp).mul(50).add(cardinal.getVector().opposite().mul(wallThickness));

											Vec2d hinge1Start = edge1.opposite().mul(50 - wallThickness);
											Vec2d sub = hinge1Start.sub(center);
											Vec2d subNorm = sub.normalize();

											Vec2d hinge1End = subNorm.mul(wallHeight);

											//	graphics.setColor(Color.GREEN);
											//	graphics.drawLine((int) unRelative.x, (int) unRelative.y, (int) hinge1End.x, (int) hinge1End.y);
										}
									}
								});

							}

							graphics.setColor(Color.BLACK);
							graphics.setStroke(new BasicStroke(3));

							Vec2d strokeCenter = cardinal.getVector().mul(50).add(offset.opposite().mul(wallThickness));
							Vec2d strokeStart = strokeCenter.sub(perp.mul(50));
							Vec2d strokeEnd = strokeCenter.add(perp.mul(50));

							EnumDiagonal corner = getRoomCorner(vec);
							if (corner != null) {
								EnumCardinal complimentary = EnumDiagonal.getComplimentaryCardinal(corner, cardinal);

								switch (corner) {
									case LOWERRIGHT:
										if (cardinal == UP) {
											strokeEnd = strokeEnd.sub(complimentary.getVector().mul(wallThickness));
										} else {
											strokeStart = strokeStart.sub(complimentary.getVector().mul(wallThickness));
										}
										break;
									case LOWERLEFT:
										if (cardinal == UP) {
											strokeStart = strokeStart.add(complimentary.getVector().mul(wallThickness));
										} else {
											strokeEnd = strokeEnd.sub(complimentary.getVector().mul(wallThickness));
										}
										break;
									case UPPERRIGHT:
										if (cardinal == DOWN) {
											strokeEnd = strokeEnd.sub(complimentary.getVector().mul(wallThickness));
										} else {
											strokeEnd = strokeEnd.sub(complimentary.getVector().mul(wallThickness));
										}
										break;
									case UPPERLEFT:
										if (cardinal == DOWN) {
											strokeStart = strokeStart.add(complimentary.getVector().mul(wallThickness));
										} else {
											strokeStart = strokeStart.sub(complimentary.getVector().mul(wallThickness));
										}
										break;
								}
							}

							graphics.setColor(Color.BLACK);
							graphics.drawLine((int) strokeStart.x, (int) strokeStart.y, (int) strokeEnd.x, (int) strokeEnd.y);

							if (corner == null) {
								Vec2d wallBottom = oppositeOf(cardinal).getVector().mul(wallHeight);
								graphics.drawLine((int) (strokeStart.x + wallBottom.x), (int) (strokeStart.y + wallBottom.y), (int) (strokeEnd.x + wallBottom.x), (int) (strokeEnd.y + wallBottom.y));
							} else {
								Vec2d diag = corner.getVector();
								Vec2d diagStart = diag.mul(50 - wallThickness);
								Vec2d diagEnd = diag.opposite().mul(wallHeight / 2.0);
								graphics.drawLine((int) diagStart.x, (int) diagStart.y, (int) diagEnd.x, (int) diagEnd.y);
							}

							graphics.translate(-vX - 50, -vY - 50);
						}
					}

					EnumDiagonal innerDiag = getInnerRoomCorner(vec);
					if (innerDiag != null) {
						graphics.translate(vX + 50, vY + 50);

						Vec2d translate = innerDiag.getVector().mul(50);
						graphics.translate(translate.x, translate.y);

						Vec2d innerCorner1 = Vec2d.ZERO;
						Vec2d innerCorner2 = innerDiag.getVector().opposite().mul(wallThickness);
						Vec2d innerCorner3 = new Vec2d(innerCorner1.x, innerCorner2.y);
						Vec2d innerCorner4 = new Vec2d(innerCorner2.x, innerCorner1.y);

						int largestX = (int) Math.max(Math.max(innerCorner1.x, innerCorner2.x), Math.max(innerCorner3.x, innerCorner4.x));
						int largestY = (int) Math.max(Math.max(innerCorner1.y, innerCorner2.y), Math.max(innerCorner3.y, innerCorner4.y));
						int smallestX = (int) Math.min(Math.min(innerCorner1.x, innerCorner2.x), Math.min(innerCorner3.x, innerCorner4.x));
						int smallestY = (int) Math.min(Math.min(innerCorner1.y, innerCorner2.y), Math.min(innerCorner3.y, innerCorner4.y));

						graphics.setColor(Color.ORANGE);
						graphics.fillRect(smallestX, smallestY, largestX - smallestX, largestY - smallestY);

						graphics.setColor(Color.BLACK);
						graphics.drawLine((int) innerCorner2.x, (int) innerCorner2.y, (int) innerCorner3.x, (int) innerCorner3.y);
						graphics.drawLine((int) innerCorner2.x, (int) innerCorner2.y, (int) innerCorner4.x, (int) innerCorner4.y);

						Vec2d diag = innerDiag.getVector();
						Vec2d diagStart = diag.mul(-wallThickness);
						Vec2d diagEnd = diag.opposite().mul(wallHeight);
						graphics.drawLine((int) diagStart.x, (int) diagStart.y, (int) diagEnd.x, (int) diagEnd.y);

						EnumCardinal edge1 = innerDiag.getCardinal1();
						EnumCardinal edge2 = innerDiag.getCardinal2();
						Vec2d edgeVec1 = edge1.getVector().mul(100 - wallThickness / 2.0);
						Vec2d edgeVec2 = edge2.getVector().mul(100 - wallThickness / 2.0);
						Vec2d edgeStart = diag.opposite().mul(100 - wallThickness / 2.0);
						graphics.drawLine((int) edgeStart.x, (int) edgeStart.y, (int) (edgeStart.x + edgeVec1.x), (int) (edgeStart.y + edgeVec1.y));
						graphics.drawLine((int) edgeStart.x, (int) edgeStart.y, (int) (edgeStart.x + edgeVec2.x), (int) (edgeStart.y + edgeVec2.y));

						graphics.translate(-translate.x, -translate.y);

						graphics.translate(-vX - 50, -vY - 50);
					}
				}
			}
		}

		for (EnumTile room : rooms.keys()) {
			Set<Vec2d> tiles = rooms.get(room);

			String title = room.getName();

			Vec2d upperLeftCorner = getUpperLeftCorner(tiles).mul(100).add(50);
			Vec2d size = getBoundingSize(tiles).mul(100);
			Vec2d center = Utils.calculateCentroid(tiles).add(50);
			if (size == null || center == null) continue;

			graphics.setColor(Color.RED);

			Font fontObj = new Font("TimesRoman", Font.PLAIN, 50);

			Rectangle2D textBounds = graphics.getFontMetrics(fontObj).getStringBounds(title, graphics);
			Vec2d loc = Utils.getVecFromName("center", size, new Vec2d(textBounds.getWidth(), textBounds.getHeight()));

			GlyphVector vector = fontObj.createGlyphVector(graphics.getFontRenderContext(), title);
			Shape textShape = vector.getOutline();

			graphics.translate(upperLeftCorner.x + loc.x, upperLeftCorner.y + loc.y);
			graphics.setStroke(new BasicStroke(10));
			graphics.setColor(Color.BLACK);
			graphics.draw(textShape);

			graphics.setColor(Color.WHITE);
			graphics.fill(textShape);

			graphics.translate(-upperLeftCorner.x - loc.x, -upperLeftCorner.y - loc.y);
		}

		graphics.dispose();

		file = new File("bin/cluedo/board.png");
		try {
			ImageIO.write(imgBoard, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Set<Vec2d> getEntrances() {
		return entrances;
	}

	public HashMultimap<EnumTile, Vec2d> getRooms() {
		return rooms;
	}

	public BufferedImage getImgBoard() {
		return imgBoard;
	}

	public Vec2d getUpperLeftCorner(Set<Vec2d> room) {
		Vec2d smallest = null;

		for (Vec2d vec : room) {
			if (smallest == null) smallest = vec;

			if (smallest.compare(vec) > 0) smallest = vec;
		}

		if (smallest == null) return null;

		return smallest;
	}

	public Vec2d getBoundingSize(Set<Vec2d> room) {
		Vec2d smallest = null;
		Vec2d largest = null;

		for (Vec2d vec : room) {
			if (smallest == null) smallest = vec;
			if (largest == null) largest = vec;

			if (smallest.compare(vec) > 0) smallest = vec;
			if (largest.compare(vec) < 0) largest = vec;
		}

		if (smallest == null || largest == null) return null;

		return largest.sub(smallest);
	}

	@Nullable
	public EnumCardinal getRoomEdgeTile(Vec2d vec) {
		for (EnumCardinal facing : EnumCardinal.values()) {
			Vec2d offset = vec.add(facing);

			if (!isInBounds(offset)) return facing;
			if (getTile(offset, FLOOR) == FLOOR) return facing;
		}
		return null;
	}

	public EnumDiagonal getInnerRoomCorner(Vec2d tile) {
		int floors = 0;

		for (EnumCardinal cardinal : EnumCardinal.values()) {
			Vec2d offset = tile.add(cardinal.getVector());

			EnumTile string = getTile(offset);
			if (string == FLOOR) floors++;
		}

		EnumDiagonal cornerDiag = null;
		for (EnumDiagonal diagonal : EnumDiagonal.values()) {
			Vec2d offset = tile.add(diagonal.getVector());

			if (getTile(offset) == FLOOR) {
				cornerDiag = diagonal;
				floors++;
			}
		}

		if (floors == 1 && cornerDiag != null)
			return cornerDiag;

		return null;
	}

	@Nullable
	public EnumDiagonal getRoomCorner(Vec2d vec) {
		if (vec == null) return null;

		EnumCardinal floor1 = null;
		EnumCardinal floor2 = null;

		if (!isInBounds(vec.add(LEFT))) {
			floor1 = LEFT;
		} else if (!isInBounds(vec.add(RIGHT))) {
			floor1 = RIGHT;
		}

		if (!isInBounds(vec.add(UP))) {
			floor2 = UP;
		} else if (!isInBounds(vec.add(DOWN))) {
			floor2 = DOWN;
		}

		for (EnumCardinal facing : EnumCardinal.values()) {
			Vec2d offset = vec.add(facing);

			if (getTile(offset) != FLOOR) continue;

			if (floor1 == null) floor1 = facing;
			else {
				floor2 = facing;
				break;
			}
		}

		if (floor1 == null || floor2 == null) return null;

		return EnumDiagonal.getDiagonal(floor1, floor2);
	}

	@Nonnull
	public EnumTile getTile(Vec2d vec) {
		return isInBounds(vec) ? boardGrid[(int) vec.x][(int) vec.y] : EnumTile.NULL;
	}

	@Nonnull
	public EnumTile getTile(Vec2d vec, EnumTile fallback) {
		return isInBounds(vec) ? boardGrid[(int) vec.x][(int) vec.y] : fallback;
	}

	public EnumTile getTile(int x, int y) {
		return boardGrid[x][y];
	}

	public boolean isInBounds(Vec2d vec) {
		return vec.x >= 0 && vec.x < boardGrid.length && vec.y >= 0 && vec.y < boardGrid.length;
	}

	public boolean isInBounds(int i) {
		return i >= 0 && i < boardGrid.length;
	}
}
