package shape;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import abfab3d.io.input.STLReader;
import abfab3d.util.BoundingBoxCalculator;
import abfab3d.util.TriangleCollector;

public class STLShape extends Shape {
    private double[] smallBounds;
    private STLCollector stlCollector;
    private byte[] inFile;

    public STLShape(String inFile) throws IOException {
        this(Files.readAllBytes(Paths.get(inFile)), 1);
    }

    public STLShape(byte[] bytes) {
        this(bytes, 1);
    }

    private STLShape(byte[] bytes, double scale) {
        this.inFile = bytes;
        double MM = 0.001;
        scale = MM / scale;

        // Read the STL file, use the bounds to determine voxel grid size
        STLReader stl = new STLReader();
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        try {
            stl.read(new ByteArrayInputStream(bytes), bb);
            double[] bigbounds = new double[6];
            bb.getBounds(bigbounds);
            smallBounds = new double[] { bigbounds[0] / scale,
                    bigbounds[1] / scale, bigbounds[2] / scale,
                    bigbounds[3] / scale, bigbounds[4] / scale,
                    bigbounds[5] / scale };

            stlCollector = new STLCollector(scale);
            stl.read(new ByteArrayInputStream(bytes), stlCollector);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int countTriangles(Point3d point) {
        int count = 0;
        Vector3d vec = new Vector3d(point);
        String key = (int) point.y + "," + (int) point.z;
        if (!stlCollector.triangles.containsKey(key)) {
            return 0;
        }
        for (Vector3d[] t : stlCollector.triangles.get(key)) {
            if (intersects(t[0], t[1], t[2], vec, new Vector3d(-1, 0, 0))) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean inShape(Point3d point) {
        // count number of triangles which enclose this Y/Z coord and are less
        // than this X coord.
        // If even, return false. If odd, return true.
        int count = countTriangles(point);
        return count % 2 == 1;
    }

    @Override
    public long fastInShape(Point3d point) {
        // TODO Auto-generated method stub
        int count = countTriangles(point);
        if (count == countTriangles(new Point3d(point.x + 63, point.y, point.z))) {
            if (count % 2 == 0) {
                return 0;
            } else {
                return -1;
            }
        }
        return super.fastInShape(point);
    }

    @Override
    public Shape scale(double s) {
        return new STLShape(inFile, s);
    }

    private boolean intersects(Vector3d vertex1, Vector3d vertex2,
            Vector3d vertex3, Vector3d rayOrigin, Vector3d rayDirection) {
        // Find vectors for two edges sharing V1
        // SUB(e1, V2, V1);
        Vector3d edge1 = new Vector3d(vertex2);
        edge1.sub(vertex1);
        // SUB(e2, V3, V1);
        Vector3d edge2 = new Vector3d(vertex3);
        edge2.sub(vertex1);
        // Begin calculating determinant - also used to calculate u parameter
        Vector3d P = new Vector3d(rayDirection);
        P.cross(rayDirection, edge2);
        // CROSS(P, D, e2);
        // if determinant is near zero, ray lies in plane of triangle
        // det = DOT(e1, P);
        double det = edge1.dot(P);
        // NOT CULLING
        // if(det > -EPSILON && det < EPSILON) return 0;
        double inv_det = 1. / det;

        // calculate distance from V1 to ray origin

        // SUB(T, O, V1);
        Vector3d T = new Vector3d(rayOrigin);
        T.sub(vertex1);
        // Calculate u parameter and test bound
        double u = T.dot(P) * inv_det;
        // The intersection lies outside of the triangle
        if (u < 0.f || u > 1.f)
            return false;

        // Prepare to test v parameter
        // CROSS(Q, T, e1);
        Vector3d Q = new Vector3d();
        Q.cross(T, edge1);

        // Calculate V parameter and test bound
        double v = rayDirection.dot(Q) * inv_det;
        // The intersection lies outside of the triangle
        if (v < 0.f || u + v > 1.f)
            return false;

        double t = edge2.dot(Q) * inv_det;

        if (t >= 0) { // ray intersection
            // *out = t;
            return true;
        }

        // No hit, no win
        return false;
    }

    @Override
    public double[] getBounds() {
        return smallBounds;
    }
}

class STLCollector implements TriangleCollector {

    HashMap<String, List<Vector3d[]>> triangles = new HashMap<>();

    private double scale;

    public STLCollector(double scale) {
        this.scale = scale;
    }

    @Override
    public boolean addTri(Vector3d v0, Vector3d v1, Vector3d v2) {
        v0 = v2p(v0);
        v1 = v2p(v1);
        v2 = v2p(v2);
        double ymin = Math.min(Math.min(v0.y, v1.y), v2.y);
        double zmin = Math.min(Math.min(v0.z, v1.z), v2.z);
        double ymax = Math.max(Math.max(v0.y, v1.y), v2.y);
        double zmax = Math.max(Math.max(v0.z, v1.z), v2.z);
        Vector3d[] tri = new Vector3d[] { v0, v1, v2 };
        for (int y = (int) ymin; y <= Math.ceil(ymax); y++) {
            for (int z = (int) zmin; z <= Math.ceil(zmax); z++) {
                if (!triangles.containsKey(y + "," + z)) {
                    triangles.put(y + "," + z, new ArrayList<Vector3d[]>());
                }
                triangles.get(y + "," + z).add(tri);
            }
        }
        return true;
    }

    private Vector3d v2p(Vector3d vec) {
        return new Vector3d(vec.x / scale, vec.y / scale, vec.z / scale);
    }

}
