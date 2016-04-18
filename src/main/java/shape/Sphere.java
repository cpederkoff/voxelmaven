package shape;

import javax.vecmath.Point3d;

public class Sphere extends Shape {
    int radius;

    public Sphere(int radius) {
        super();
        this.radius = radius;
    }

    @Override
    public long fastInShape(Point3d point) {
        if (point.y < -radius || point.y > radius || point.z < -radius
                || point.z > radius) {
            return 0x0;
        }
        return super.fastInShape(point);
    }

    @Override
    public boolean inShape(Point3d point) {
        return point.distance(new Point3d(0, 0, 0)) <= radius;
    }

    @Override
    public double[] getBounds() {
        return new double[] { -radius, radius, -radius, radius, -radius, radius };
    }
}
