package shape;

import javax.vecmath.Point3d;

public class Cube extends Shape {

    private double sideLength;

    public Cube(double sideLength) {
        this.sideLength = sideLength;
    }

    @Override
    public long fastInShape(Point3d point) {
        if (point.y < 0 || point.y > sideLength || point.z < 0
                || point.z > sideLength) {
            return 0;
        }
        if (point.x + 64 < 0) {
            return 0;
        }
        if (point.x - 64 > sideLength) {
            return 0;
        }
        int startZeros = (int) -point.x;
        int endZeros = (int) ((point.x + 64) - sideLength);
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
        return point.x >= 0 && point.x <= sideLength && point.y >= 0
                && point.y <= sideLength && point.z >= 0
                && point.z <= sideLength;

    }

    @Override
    public double[] getBounds() {
        return new double[] { 0, sideLength, 0, sideLength, 0, sideLength };
    }

}
