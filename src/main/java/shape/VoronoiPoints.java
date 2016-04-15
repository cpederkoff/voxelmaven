package shape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import javax.vecmath.Point3d;

public class VoronoiPoints {
	private Shape targetShape;

	public VoronoiPoints(Shape targetShape) {
		this.targetShape = targetShape;
	}

	public Point3d[] generatePoints() {
		Shape shell = targetShape.shell(5);

		double[] bounds = targetShape.getBounds();
		List<Point3d> outerPoints = new ArrayList<>();
		double keepAway = getKeepAway();
		Random r = new Random(0);
		for (int i = 0; i < 200000; i++) {
			double x = randomInRange(r, bounds[0], bounds[1]);
			double y = randomInRange(r, bounds[2], bounds[3]);
			double z = randomInRange(r, bounds[4], bounds[5]);
			// double zprop = (z - bounds[4]) / (bounds[5] - bounds[4]);
			// zprop = 1 - zprop;
			Point3d point3d = new Point3d(x, y, z);
			if (outerPoints.size() == 0) {
				outerPoints.add(point3d);
				// } else if (shell.inShape(point3d) &&
				// getClosestPointDist(outerPoints, point3d) > (2 - zprop) *
				// keepAway) {
			} else if (shell.inShape(point3d) && getClosestPointDist(outerPoints, point3d) > keepAway) {
				outerPoints.add(point3d);
			}
		}

		List<Point3d> innerPoints = new ArrayList<>();
		for (int i = 0; i < 20000; i++) {
			double x = randomInRange(r, bounds[0], bounds[1]);
			double y = randomInRange(r, bounds[2], bounds[3]);
			double z = randomInRange(r, bounds[4], bounds[5]);
			// double zprop = (z - bounds[4]) / (bounds[5] - bounds[4]);
			// zprop = 1 - zprop;

			Point3d point3d = new Point3d(x, y, z);
			if (innerPoints.size() == 0) {
				innerPoints.add(point3d);
			} else {
				double alldist = Math.min(getClosestPointDist(innerPoints, point3d),
						getClosestPointDist(outerPoints, point3d));
				// if (alldist > (2 - zprop) * keepAway &&
				// targetShape.inShape(point3d)) {
				if (alldist > keepAway && targetShape.inShape(point3d)) {
					innerPoints.add(point3d);
				}
			}
		}
		List<Point3d> newPoint = new ArrayList<>();
		for (Point3d point : innerPoints) {
			newPoint.add(new Point3d(point.x, point.y, point.z));
		}
		for (Point3d point : outerPoints) {
			newPoint.add(new Point3d(point.x, point.y, point.z));
		}
		return toArray(newPoint);
	}

	private static double randomInRange(Random r, double start, double end) {
		return r.nextDouble() * (end - start) + start;
	}

	private static double getClosestPointDist(List<Point3d> points, Point3d point3d) {
		Point3d[] array = toArray(points);
		sortPointsByDist(point3d, array);
		Point3d closest = array[0];
		return closest.distance(point3d);
	}

	public double getKeepAway() {
		double[] bounds = targetShape.getBounds();
		return ((bounds[1] - bounds[0]) + (bounds[3] - bounds[2]) + (bounds[5] - bounds[4])) / 17;
	}

	private static Point3d[] toArray(List<Point3d> points) {
		return points.toArray(new Point3d[points.size()]);
	}

	private static void sortPointsByDist(final Point3d point, Point3d[] points) {
		Arrays.sort(points, new Comparator<Point3d>() {

			@Override
			public int compare(Point3d p1, Point3d p2) {
				double p1dist = p1.distance(point);
				double p2dist = p2.distance(point);
				if (p1dist > p2dist) {
					return 1;
				} else if (p2dist > p1dist) {
					return -1;
				} else {
					return 0;
				}
			}
		});
	}

}
