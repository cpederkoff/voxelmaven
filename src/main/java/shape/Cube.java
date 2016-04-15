package shape;

import javax.vecmath.Point3d;

public class Cube extends Shape {

	private double sideRadius;

	public Cube(double sideRadius) {
		this.sideRadius = sideRadius;
	}

	@Override
	public long fastInShape(Point3d point) {
		if (point.y < -sideRadius || point.y > sideRadius || point.z < -sideRadius || point.z > sideRadius) {
			return 0;
		}
		if (point.x + 64 < -sideRadius) {
			return 0;
		}
		if (point.x - 64 > sideRadius) {
			return 0;
		}
		int startZeros = (int) (-sideRadius - point.x);
		int endZeros = (int) ((point.x + 64) - sideRadius);
		int remainingBits = 64;
		if (startZeros > 0) {
			remainingBits -= startZeros;
		}
		int middleOnes = remainingBits;
		if (endZeros > 0) {
			remainingBits -= endZeros;
		}
		long ret = 0;
		for (int i = 0; i < middleOnes; i++) {
			ret = ret << 1;
			ret = ret | 0x1;
		}
		for (int i = 0; i < endZeros; i++) {
			ret = ret << 1;
		}
		return ret;
	}

	@Override
	public boolean inShape(Point3d point) {
		return point.x >= -sideRadius && point.x <= sideRadius && point.y >= -sideRadius && point.y <= sideRadius
				&& point.z >= -sideRadius && point.z <= sideRadius;

	}

	@Override
	public double[] getBounds() {
		return new double[] { -sideRadius, sideRadius, -sideRadius, sideRadius, -sideRadius, sideRadius };
	}

}
