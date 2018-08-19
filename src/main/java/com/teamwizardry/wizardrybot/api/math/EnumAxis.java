package com.teamwizardry.wizardrybot.api.math;

public enum EnumAxis implements IFacing {
	VERTICAL(new Vec2d(0, 1)), HORIZONTAL(new Vec2d(1, 0));

	private final Vec2d vec2d;

	EnumAxis(Vec2d vec2d) {

		this.vec2d = vec2d;
	}

	public static EnumAxis getOppositeOf(EnumAxis facing) {
		switch (facing) {
			case VERTICAL:
				return HORIZONTAL;
			case HORIZONTAL:
				return VERTICAL;
		}
		return VERTICAL;
	}

	public static EnumAxis getAxisFromVector(Vec2d vec) {
		Vec2d norm = vec.normalize();

		for (EnumAxis facing : values()) {
			if (facing.getVector().equals(norm)) return facing;
		}

		return VERTICAL;
	}

	@Override
	public Vec2d getVector() {
		return vec2d;
	}

}
