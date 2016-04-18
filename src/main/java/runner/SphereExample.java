package runner;

import java.io.IOException;

import meshmaker.MeshMaker;
import shape.Cube;
import shape.Shape;
import shape.Sphere;

public class SphereExample {
    public static void main(String[] args) throws IOException {
        Shape sphere = new Sphere(100);
        MeshMaker maker = new MeshMaker(sphere.union(new Cube(100)));
        maker.writeMesh("src/main/resources/sphere.stl");
    }
}
