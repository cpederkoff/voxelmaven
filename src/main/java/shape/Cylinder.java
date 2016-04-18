package shape;

import javax.vecmath.Point3d;

public class Cylinder extends Shape {
    private double radius;
    private double height;

    public Cylinder(double radius, double height) {
        this.radius = radius;
        this.height = height;
    }

    @Override
    public boolean inShape(Point3d point) {
        if (point.z > height / 2 || point.z < -height / 2) {
            return false;
        }
        Point3d target = new Point3d(0, 0, point.z);
        return point.distance(target) <= radius;
    }

    @Override
    public long fastInShape(Point3d point) {
        if (point.z > height / 2 || point.z < -height / 2) {
            return 0x0;
        }
        if (point.y > radius || point.y < -radius) {
            return 0x0;
        }
        return super.fastInShape(point);
    }

    @Override
    public double[] getBounds() {
        return new double[] { -radius, radius, -radius, radius, -height / 2,
                height / 2 };
    }

}
