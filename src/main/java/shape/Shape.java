package shape;

import javax.vecmath.Point3d;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public abstract class Shape {
    /**
     * This method returns a long whose most significant bit is 1 if the point
     * provided is in the shape. The least significant bit is 1 if the point
     * provided + 64 units in x is in the shape. The middle follows this pattern
     * also.
     * 
     * @param point
     * @return
     */
    // public long fastInShape(Point3d point) {
    // long ret = 0x0;
    // boolean start = this.inShape(point);
    // if (start) {
    // ret = 0x1;
    // }
    // for (int i = 1; i < 62; i += 2) {
    // boolean next = this.inShape(new Point3d(point.x + i + 1, point.y,
    // point.z));
    // if (next == start) {
    // ret = ret << 2;
    // if (next) {
    // ret |= 0x3;
    // }
    // } else {
    // boolean mid = this.inShape(new Point3d(point.x + i, point.y, point.z));
    // ret = ret << 2;
    // if (mid) {
    // ret |= 0x2;
    // }
    // if (next) {
    // ret |= 0x1;
    // }
    // }
    // start = next;
    // }
    // ret = ret << 1;
    // if (this.inShape(new Point3d(point.x + 63, point.y, point.z))) {
    // ret |= 0x1;
    // }
    //
    // return ret;
    // }

    public long fastInShape(Point3d point) {
        long ret = 0x0;
        for (int i = 0; i < 63; i += 1) {
            boolean next = this.inShape(new Point3d(point.x + i, point.y, point.z));
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

    public abstract boolean inShape(Point3d point);

    public abstract double[] getBounds();

    public Shape intersect(final Shape shape) {
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
                    ret[i] = Math.max(self.getBounds()[i], shape.getBounds()[i]);
                    ret[i + 1] = Math.min(self.getBounds()[i + 1], shape.getBounds()[i + 1]);
                }
                return ret;
            }
        };
    }

    public Shape subtract(final Shape shape) {
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
                    ret[i] = Math.min(self.getBounds()[i], shape.getBounds()[i]);
                    ret[i + 1] = Math.max(self.getBounds()[i + 1], shape.getBounds()[i + 1]);
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
                    Point3d newPoint = new Point3d(point.x / 1, point.y / y, point.z / z);
                    return self.fastInShape(newPoint);
                } else {
                    return super.fastInShape(point);
                }
            }

            @Override
            public boolean inShape(Point3d point) {
                Point3d newPoint = new Point3d(point.x / x, point.y / y, point.z / z);
                return self.inShape(newPoint);
            }

            @Override
            public double[] getBounds() {
                return new double[] { self.getBounds()[0] * x, self.getBounds()[1] * x, self.getBounds()[2] * y,
                        self.getBounds()[3] * y, self.getBounds()[4] * z, self.getBounds()[5] * z

                };
            }
        };
    }

    public Shape translate(final double x, final double y, final double z) {
        final Shape self = this;
        return new Shape() {
            @Override
            public long fastInShape(Point3d point) {
                Point3d newPoint = new Point3d(point.x - x, point.y - y, point.z - z);
                return self.fastInShape(newPoint);
            }

            @Override
            public boolean inShape(Point3d point) {
                Point3d newPoint = new Point3d(point.x - x, point.y - y, point.z - z);
                return self.inShape(newPoint);
            }

            @Override
            public double[] getBounds() {
                return new double[] { self.getBounds()[0] + x, self.getBounds()[1] + x, self.getBounds()[2] + y,
                        self.getBounds()[3] + y, self.getBounds()[4] + z, self.getBounds()[5] + z

                };
            }
        };
    }

    public Shape shell(double thick) {
        return this.subtract(inset(thick));

    }

    public Shape inset(double thick) {
        return this.intersect(this.translate(thick, 0, 0)).intersect(this.translate(-thick, 0, 0))
                .intersect(this.translate(0, thick, 0)).intersect(this.translate(0, -thick, 0))
                .intersect(this.translate(0, 0, thick)).intersect(this.translate(0, 0, -thick));
    }

    public Shape rotate(double x, double y, double z, double angle) {
        angle = angle * (2 * Math.PI) / 360;
        final Rotation rotation = new Rotation(new Vector3D(x, y, z), angle);
        final Shape self = this;
        return new Shape() {

            @Override
            public boolean inShape(Point3d point) {
                Vector3D newPoint = rotation.applyTo(new Vector3D(new double[] { point.x, point.y, point.z }));
                return self.inShape(new Point3d(newPoint.getX(), newPoint.getY(), newPoint.getZ()));
            }

            @Override
            public double[] getBounds() {
                double xmin = 0, xmax = 0, ymin = 0, ymax = 0, zmin = 0, zmax = 0;
                double[] bounds = self.getBounds();
                for (double x : new double[] { bounds[0], bounds[1] }) {
                    for (double y : new double[] { bounds[2], bounds[3] }) {
                        for (double z : new double[] { bounds[4], bounds[5] }) {
                            Vector3D newPoint = rotation.applyTo(new Vector3D(new double[] { x, y, z }));
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
                double distFromY = Math.sqrt(point.x * point.x + point.z * point.z);
                return self.inShape(new Point3d(distFromY, point.y, 0));
            }

            @Override
            public double[] getBounds() {
                double x = Math.max(0, self.getBounds()[1]);

                return new double[] { -x, x, self.getBounds()[2], self.getBounds()[3], -x, x };
            }

        };
    }

    public Shape push(final double top) {
        final Shape self = this;
        return new Shape() {
            @Override
            public long fastInShape(Point3d point) {
                return super.fastInShape(point);
            }

            @Override
            public boolean inShape(Point3d point) {
                double ratio = (point.z - self.getBounds()[4]) / (self.getBounds()[5] - self.getBounds()[4]);
                ratio *= (1 / top - 1);
                ratio += 1;
                Point3d newPoint = new Point3d(point.x / ratio, point.y / ratio, point.z);
                return self.inShape(newPoint);
            }

            @Override
            public double[] getBounds() {
                double mult = Math.max(top, 1);
                return new double[] { self.getBounds()[0] * mult, self.getBounds()[1] * mult,
                        self.getBounds()[2] * mult, self.getBounds()[3] * mult, self.getBounds()[4],
                        self.getBounds()[5] };
            }
        };
    }
}
