package com.teamwizardry.wizardrybot.api.math;

import org.jetbrains.annotations.Nullable;

import static com.teamwizardry.wizardrybot.api.math.EnumCardinal.*;

public enum EnumDiagonal implements IFacing {
	UPPERRIGHT(new Vec2d(1, 1), UP, RIGHT),
	UPPERLEFT(new Vec2d(-1, 1), UP, LEFT),
	LOWERRIGHT(new Vec2d(1, -1), DOWN, RIGHT),
	LOWERLEFT(new Vec2d(-1, -1), DOWN, LEFT);

	private final Vec2d vec2d;
	private final EnumCardinal cardinal1;
	private final EnumCardinal cardinal2;

	EnumDiagonal(Vec2d vec2d, EnumCardinal cardinal1, EnumCardinal cardinal2) {

		this.vec2d = vec2d;
		this.cardinal1 = cardinal1;
		this.cardinal2 = cardinal2;
	}

	@Nullable
	public static EnumDiagonal getDiagonal(EnumCardinal cardinal1, EnumCardinal cardinal2) {
		Vec2d vec = cardinal1.getVector().add(cardinal2.getVector());

		return getDiagonalFromVetor(vec);
	}

	@Nullable
	public static EnumDiagonal getDiagonalFromVetor(Vec2d vec) {
		Vec2d norm = vec.normalize();
		Vec2d clamp = new Vec2d(norm.x > 0 ? 1 : -1, norm.y > 0 ? 1 : -1);

		for (EnumDiagonal facing : values()) {
			if (facing.getVector().equals(clamp)) return facing;
		}

		return null;
	}

	public static EnumDiagonal getMirror(EnumDiagonal diagonal, EnumAxis axis) {
		if (axis == EnumAxis.VERTICAL) {
			switch (diagonal) {
				case UPPERRIGHT:
					return UPPERLEFT;
				case UPPERLEFT:
					return UPPERRIGHT;
				case LOWERRIGHT:
					return LOWERLEFT;
				case LOWERLEFT:
					return LOWERRIGHT;
			}
		} else {
			switch (diagonal) {
				case UPPERRIGHT:
					return LOWERRIGHT;
				case UPPERLEFT:
					return LOWERLEFT;
				case LOWERRIGHT:
					return UPPERRIGHT;
				case LOWERLEFT:
					return UPPERLEFT;
			}
		}
		return diagonal;
	}

	public static boolean doesDiagonalContainCardinal(EnumDiagonal diagonal, EnumCardinal cardinal) {
		return diagonal.getCardinal1() == cardinal || diagonal.getCardinal2() == cardinal;
	}

	public static EnumCardinal getComplimentaryCardinal(EnumDiagonal diagonal, EnumCardinal cardinal) {
		if (!doesDiagonalContainCardinal(diagonal, cardinal)) return cardinal;

		if (diagonal.getCardinal1() == cardinal) {
			return diagonal.getCardinal2();
		} else {
			return diagonal.getCardinal1();
		}
	}

	public static EnumDiagonal getOppositeOf(EnumDiagonal diagonal) {
		switch (diagonal) {
			case UPPERRIGHT:
				return LOWERLEFT;
			case UPPERLEFT:
				return LOWERRIGHT;
			case LOWERRIGHT:
				return UPPERLEFT;
			case LOWERLEFT:
				return UPPERRIGHT;
		}
		return UPPERLEFT;
	}

	@Override
	public Vec2d getVector() {
		return vec2d;
	}

	public EnumCardinal getCardinal2() {
		return cardinal2;
	}

	public EnumCardinal getCardinal1() {
		return cardinal1;
	}
}
