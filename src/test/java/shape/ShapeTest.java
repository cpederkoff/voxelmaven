package shape;

import java.io.IOException;

import javax.vecmath.Point3d;

import junit.framework.TestCase;

public class ShapeTest extends TestCase {

	public void testCube() {
		checkFastInShape(new Cube(50));
	}

	public void testSphere() {
		checkFastInShape(new Sphere(50));
	}

	public void testCylinder() {
		checkFastInShape(new Cylinder(50, 50));
	}

	public void testVoronoi() {
		Cube cube = new Cube(25);
		VoronoiPoints voronoiPoints = new VoronoiPoints(cube);
		checkFastInShape(new Voronoi(5, 2, cube, voronoiPoints.generatePoints(), voronoiPoints.getKeepAway()));
	}

	public void testSTL() throws IOException {
		checkFastInShape(new STLShape("src/test/resources/bunny.stl").scale(.5));
	}

	private void checkFastInShape(Shape shape) {
		double[] bounds = shape.getBounds();
		for (int z = (int) bounds[4]; z < (int) bounds[5]; z++) {
			for (int y = (int) bounds[2]; y < (int) bounds[3]; y++) {
				for (int x = (int) bounds[0]; x < (int) bounds[1]; x += 64) {
					long vals = shape.fastInShape(new Point3d(x, y, z));
					for (int i = 0; i < 64; i++) {
						if (x + i < bounds[1]) {
							long top = (vals & 0x8000000000000000L);
							top = top >> 63;
							top = top & 0x1L;
							boolean fastIsIn = top == 0x1L;
							assertEquals(String.format("Didn't match at coordinate x:%d, y:%d, z:%d", x + i, y, z),
									fastIsIn, shape.inShape(new Point3d(x + i, y, z)));
						}
						vals = vals << 1;
					}

				}
			}
		}
	}
}
