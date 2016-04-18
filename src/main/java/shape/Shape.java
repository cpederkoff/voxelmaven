package shape;

import javax.vecmath.Point3d;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public abstract class Shape {

    /**
     * 
     * @param point
     *            The x,y,z coordinate to compute
     * @return True if the point provided is in the shape. False if not.
     */
    public abstract boolean inShape(Point3d point);

    /**
     * @return an array which represents {xmin, xmax, ymin, ymax, zmin, zmax}
     */
    public abstract double[] getBounds();

    /**
     * This method returns a long whose most significant bit represents if the
     * point provided is in the shape. The least significant bit represents if
     * the point provided + 64 units in x is in the shape.
     * 
     * @param point
     *            The coordinate to start the calculation at.
     * @return A long whose bits represent the result of calling inShape on
     *         (x,y,z) to (x+64,y,z)
     */
    public long fastInShape(Point3d point) {
        long ret = 0x0;
        for (int i = 0; i < 63; i += 1) {
            boolean next = this.inShape(new Point3d(point.x + i, point.y,
                    point.z));
            if (next) {
                ret |= 0x1;
            }
            ret = ret << 1;
        }
        if (this.inShape(new Point3d(point.x + 63, point.y, point.z))) {
            ret |= 0x1;
        }

        return ret;
    }

    public Shape intersection(final Shape shape) {
        final Shape self = this;
        return new Shape() {
            @Override
            public long fastInShape(Point3d point) {
                return self.fastInShape(point) & shape.fastInShape(point);
            }

            @Override
            public boolean inShape(Point3d point) {
                return self.inShape(point) && shape.inShape(point);
            }

            @Override
            public double[] getBounds() {
                double[] ret = new double[6];
                for (int i = 0; i < 6; i += 2) {
                    ret[i] = Math
                            .max(self.getBounds()[i], shape.getBounds()[i]);
                    ret[i + 1] = Math.min(self.getBounds()[i + 1],
                            shape.getBounds()[i + 1]);
                }
                return ret;
            }
        };
    }

    public Shape difference(final Shape shape) {
        final Shape self = this;
        return new Shape() {
            @Override
            public long fastInShape(Point3d point) {
                return self.fastInShape(point) & ~shape.fastInShape(point);
            }

            @Override
            public boolean inShape(Point3d point) {
                return self.inShape(point) && !shape.inShape(point);
            }

            @Override
            public double[] getBounds() {
                return self.getBounds();
            }

        };
    }

    public Shape union(final Shape shape) {
        final Shape self = this;
        return new Shape() {
            @Override
            public long fastInShape(Point3d point) {
                return self.fastInShape(point) | shape.fastInShape(point);
            }

            @Override
            public boolean inShape(Point3d point) {
                return self.inShape(point) || shape.inShape(point);
            }

            @Override
            public double[] getBounds() {
                double[] ret = new double[6];
                for (int i = 0; i < 6; i += 2) {
                    ret[i] = Math
                            .min(self.getBounds()[i], shape.getBounds()[i]);
                    ret[i + 1] = Math.max(self.getBounds()[i + 1],
                            shape.getBounds()[i + 1]);
                }
                return ret;
            }
        };
    }

    public Shape scale(final double s) {
        return scale(s, s, s);
    }

    public Shape scale(final double x, final double y, final double z) {
        final Shape self = this;
        return new Shape() {
            @Override
            public long fastInShape(Point3d point) {
                if (x == 1) {
                    Point3d newPoint = new Point3d(point.x / 1, point.y / y,
                            point.z / z);
                    return self.fastInShape(newPoint);
                } else {
                    return super.fastInShape(point);
                }
            }

            @Override
            public boolean inShape(Point3d point) {
                Point3d newPoint = new Point3d(point.x / x, point.y / y,
                        point.z / z);
                return self.inShape(newPoint);
            }

            @Override
            public double[] getBounds() {
                return new double[] { self.getBounds()[0] * x,
                        self.getBounds()[1] * x, self.getBounds()[2] * y,
                        self.getBounds()[3] * y, self.getBounds()[4] * z,
                        self.getBounds()[5] * z

                };
            }
        };
    }

    public Shape translate(final double x, final double y, final double z) {
        final Shape self = this;
        return new Shape() {
            @Override
            public long fastInShape(Point3d point) {
                Point3d newPoint = new Point3d(point.x - x, point.y - y,
                        point.z - z);
                return self.fastInShape(newPoint);
            }

            @Override
            public boolean inShape(Point3d point) {
                Point3d newPoint = new Point3d(point.x - x, point.y - y,
                        point.z - z);
                return self.inShape(newPoint);
            }

            @Override
            public double[] getBounds() {
                return new double[] { self.getBounds()[0] + x,
                        self.getBounds()[1] + x, self.getBounds()[2] + y,
                        self.getBounds()[3] + y, self.getBounds()[4] + z,
                        self.getBounds()[5] + z

                };
            }
        };
    }

    public Shape shell(double thick) {
        return this.difference(inset(thick));

    }

    public Shape inset(double thick) {
        return this.intersection(this.translate(thick, 0, 0))
                .intersection(this.translate(-thick, 0, 0))
                .intersection(this.translate(0, thick, 0))
                .intersection(this.translate(0, -thick, 0))
                .intersection(this.translate(0, 0, thick))
                .intersection(this.translate(0, 0, -thick));
    }

    public Shape rotate(double x, double y, double z, double angle) {
        angle = angle * (2 * Math.PI) / 360;
        final Rotation rotation = new Rotation(new Vector3D(x, y, z), angle);
        final Shape self = this;
        return new Shape() {

            @Override
            public boolean inShape(Point3d point) {
                Vector3D newPoint = rotation.applyTo(new Vector3D(new double[] {
                        point.x, point.y, point.z }));
                return self.inShape(new Point3d(newPoint.getX(), newPoint
                        .getY(), newPoint.getZ()));
            }

            @Override
            public double[] getBounds() {
                double xmin = 0, xmax = 0, ymin = 0, ymax = 0, zmin = 0, zmax = 0;
                double[] bounds = self.getBounds();
                for (double x : new double[] { bounds[0], bounds[1] }) {
                    for (double y : new double[] { bounds[2], bounds[3] }) {
                        for (double z : new double[] { bounds[4], bounds[5] }) {
                            Vector3D newPoint = rotation.applyTo(new Vector3D(
                                    new double[] { x, y, z }));
                            xmin = Math.min(newPoint.getX(), xmin);
                            xmax = Math.max(newPoint.getX(), xmax);
                            ymin = Math.min(newPoint.getY(), ymin);
                            ymax = Math.max(newPoint.getY(), ymax);
                            zmin = Math.min(newPoint.getZ(), zmin);
                            zmax = Math.max(newPoint.getZ(), zmax);

                        }
                    }
                }
                return new double[] { xmin, xmax, ymin, ymax, zmin, zmax };
            }

        };
    }

    public Shape rotateExtrude() {
        final Shape self = this;
        return new Shape() {

            @Override
            public boolean inShape(Point3d point) {
                double distFromY = Math.sqrt(point.x * point.x + point.z
                        * point.z);
                return self.inShape(new Point3d(distFromY, point.y, 0));
            }

            @Override
            public double[] getBounds() {
                double x = Math.max(0, self.getBounds()[1]);

                return new double[] { -x, x, self.getBounds()[2],
                        self.getBounds()[3], -x, x };
            }

        };
    }
}
