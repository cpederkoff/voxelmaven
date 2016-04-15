package shape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.vecmath.Point3d;

public class Voronoi extends Shape {

	private double thickness;
	private Point3d[] points;
	private int dimension;
	private Shape targetShape;
	private double keepAway;

	/**
	 * The radius of the voronoi object to be produced
	 * 
	 * @param size
	 * @param thickness
	 * @param dimension
	 * @param targetShape
	 */
	public Voronoi(double thickness, int dimension, Shape targetShape, Point3d[] points, double keepAway) {
		this.thickness = thickness;
		this.dimension = dimension;
		this.targetShape = targetShape;
		this.points = points;
		this.keepAway = keepAway;
	}

	private static Point3d[] toArray(List<Point3d> points) {
		return points.toArray(new Point3d[points.size()]);
	}

	@Override
	public boolean inShape(final Point3d point) {
		double otherdist = getPointDiff(point, points);
		return otherdist < thickness;
	}

	private double getPointDiff(final Point3d point, Point3d[] points) {
		sortPointsByDist(point, points);
		double otherdist = points[dimension].distance(point) - points[0].distance(point);
		return otherdist;
	}

	@Override
	public long fastInShape(Point3d point) {
		// Filter points down to only the ones that could possibly influence the
		// result.
		ArrayList<Point3d> subset = new ArrayList<>();
		for (Point3d p : points) {
			double cutoff = keepAway + thickness * 2;
			if (p.y > point.y - cutoff && p.y < point.y + cutoff && p.z > point.z - cutoff && p.z < point.z + cutoff
					&& p.x > point.x - cutoff && p.x < point.x + 63 + cutoff) {
				subset.add(p);
			}
		}
		long ret = 0;
		Point3d[] arr = toArray(subset);
		for (int x = 0; x < 63; x++) {
			if (arr.length > dimension && getPointDiff(new Point3d(point.x + x, point.y, point.z), arr) < thickness) {
				ret |= 0x1;
			}
			ret = ret << 1;
		}
		if (arr.length > dimension && getPointDiff(new Point3d(point.x + 63, point.y, point.z), arr) < thickness) {
			ret |= 0x1;
		}
		return ret;

	}

	private void sortPointsByDist(final Point3d point, Point3d[] points) {
		Comparator<Point3d> comparator = new Comparator<Point3d>() {

			@Override
			public int compare(Point3d p1, Point3d p2) {
				double p1dist = p1.distanceSquared(point);
				double p2dist = p2.distanceSquared(point);
				if (p1dist > p2dist) {
					return 1;
				} else if (p2dist > p1dist) {
					return -1;
				} else {
					return 0;
				}
			}
		};
		Arrays.sort(points, comparator);
	}

	@Override
	public double[] getBounds() {
		return targetShape.getBounds();
	}
}
