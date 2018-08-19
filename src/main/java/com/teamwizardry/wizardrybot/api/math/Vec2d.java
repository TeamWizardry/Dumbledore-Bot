package com.teamwizardry.wizardrybot.api.math;

import java.util.Objects;

public class Vec2d {

	public static Vec2d ZERO = new Vec2d(0, 0);
	public double x, y;

	public Vec2d(double x, double y) {

		this.x = x;
		this.y = y;
	}

	public Vec2d add(IFacing facing) {
		return add(facing.getVector());
	}

	public Vec2d add(double x, double y) {
		return new Vec2d(this.x + x, this.y + y);
	}

	public Vec2d add(double i) {
		return new Vec2d(this.x + i, this.y + i);
	}

	public Vec2d add(Vec2d v) {
		return new Vec2d(x + v.x, y + v.y);
	}

	public Vec2d sub(IFacing facing) {
		return sub(facing.getVector());
	}

	public Vec2d sub(double x, double y) {
		return new Vec2d(this.x - x, this.y - y);
	}

	public Vec2d sub(double i) {
		return new Vec2d(this.x - i, this.y - i);
	}

	public Vec2d sub(Vec2d v) {
		return new Vec2d(x - v.x, y - v.y);
	}

	public Vec2d mul(double x, double y) {
		return new Vec2d(this.x * x, this.y * y);
	}

	public Vec2d mul(Vec2d v) {
		return new Vec2d(x * v.x, y * v.y);
	}

	public Vec2d mul(double i) {
		return new Vec2d(x * i, y * i);
	}

	public Vec2d div(double x, double y) {
		return new Vec2d(this.x / x, this.y / y);
	}

	public Vec2d div(double i) {
		return new Vec2d(x / i, y / i);
	}

	public Vec2d div(Vec2d v) {
		return new Vec2d(x / v.x, y / v.y);
	}

	public double length() {
		return Math.sqrt(x * x + y * y);
	}

	public double dot(Vec2d v) {
		return x * v.x + y * v.y;
	}

	public Vec2d normalize() {
		double len = length();
		return new Vec2d(x / len, y / len);
	}

	public double distance(Vec2d to) {
		return sub(to).length();
	}

	public Vec2d rotate(double angle) {
		double rad = Math.toRadians(angle);
		double cos = Math.cos(rad);
		double sin = Math.sin(rad);

		return new Vec2d(x * cos - y * sin, x * sin + y * cos);
	}

	public Vec2d perpendicular() {
		return new Vec2d(-y, x);
	}

	public Vec2d opposite() {
		return new Vec2d(-x, -y);
	}

	public int compare(Vec2d v) {
		int cX = Double.compare(x, v.x);
		int cY = Double.compare(y, v.y);
		return cX + cY;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Vec2d vec2d = (Vec2d) o;
		return Double.compare(vec2d.x, x) == 0 &&
				Double.compare(vec2d.y, y) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public String toString() {
		return "Vec2d [x=" + x + ", y=" + y + "]";
	}

}