package runner;

import java.io.IOException;

import meshmaker.MeshMaker;
import shape.Image;
import shape.Shape;

public class RotateExtrude {
    public static void main(String[] args) throws IOException {
        Shape cup = new Image("src/main/resources/cup.png").rotateExtrude();
        MeshMaker maker = new MeshMaker(cup);
        maker.writeMesh("src/main/resources/cup.stl");
    }
}
