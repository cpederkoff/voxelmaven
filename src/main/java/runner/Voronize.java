package runner;

import java.io.IOException;

import javax.vecmath.Point3d;

import meshmaker.MeshMaker;
import shape.STLShape;
import shape.Shape;
import shape.Voronoi;
import shape.VoronoiPoints;

public class Voronize {

    public static void main(String[] args) throws IOException {
        STLShape shape = new STLShape("src/main/resources/bunny.stl");
        Shape union = voronoiShell(shape);
        MeshMaker mm = new MeshMaker(union);
        mm.writeMesh("src/main/resources/voronoibunny.stl");
    }

    private static Shape volumeVoronoi(STLShape shape) {
        VoronoiPoints vp = new VoronoiPoints(shape);
        Point3d[] points = vp.generatePoints();
        int wireThickness = 3;
        Shape inner = new Voronoi(wireThickness, 2, shape, points,
                vp.getKeepAway()).intersection(shape);
        Shape outer = new Voronoi(wireThickness, 1, shape, points,
                vp.getKeepAway()).intersection(shape.shell(wireThickness));
        Shape union = inner.union(outer);
        return union;
    }

    private static Shape voronoiShell(STLShape shape) {
        VoronoiPoints vp = new VoronoiPoints(shape);
        Point3d[] points = vp.generatePoints();
        int wireThickness = 3;
        return new Voronoi(wireThickness, 1, shape, points, vp.getKeepAway())
                .intersection(shape.shell(wireThickness));
    }
}
