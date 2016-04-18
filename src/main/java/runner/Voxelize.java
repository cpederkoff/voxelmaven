package runner;

import java.io.IOException;

import meshmaker.MeshMaker;
import shape.STLShape;

public class Voxelize {
    public static void main(String[] args) throws IOException {
        STLShape shape = new STLShape("src/main/resources/meshtovoxelize.stl");
        MeshMaker meshMaker = new MeshMaker(shape);
        meshMaker.writeMesh("src/main/resources/voxelizedmesh.stl");
    }
}
