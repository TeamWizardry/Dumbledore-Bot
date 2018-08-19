package com.teamwizardry.wizardrybot.api.math;

public enum EnumCardinal implements IFacing {
	UP(new Vec2d(0, 1), EnumAxis.VERTICAL),
	DOWN(new Vec2d(0, -1), EnumAxis.VERTICAL),
	LEFT(new Vec2d(-1, 0), EnumAxis.HORIZONTAL),
	RIGHT(new Vec2d(1, 0), EnumAxis.HORIZONTAL);

	private final Vec2d vec2d;
	private EnumAxis axis;

	EnumCardinal(Vec2d vec2d, EnumAxis axis) {

		this.vec2d = vec2d;
		this.axis = axis;
	}

	public static EnumAxis getOppositeAxis(EnumCardinal cardinal) {
		return EnumAxis.getOppositeOf(cardinal.getAxis());
	}

	public static EnumCardinal oppositeOf(EnumCardinal facing) {
		switch (facing) {
			case UP:
				return DOWN;
			case DOWN:
				return UP;
			case LEFT:
				return RIGHT;
			case RIGHT:
				return LEFT;
		}
		return UP;
	}

	public static EnumCardinal getCardinalFromVector(Vec2d vec) {
		Vec2d norm = vec.normalize();

		for (EnumCardinal facing : values()) {
			if (facing.getVector().equals(norm)) return facing;
		}

		return UP;
	}

	@Override
	public Vec2d getVector() {
		return vec2d;
	}

	public EnumAxis getAxis() {
		return axis;
	}

}
