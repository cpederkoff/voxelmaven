package runner;

import java.io.IOException;

import meshmaker.MeshMaker;
import shape.ImageRotated;
import shape.Shape;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class PartialRotateExtrude {

    @Parameter(names = "-inputFile", description = "File to read as an input", required = true)
    String inputFile;
    @Parameter(names = "-outputFile", description = "File to write to", required = true)
    String outputFile;
    @Parameter(names = "-detail", description = "Amount of detail to compute. (Default 300)")
    int detail = 300;
    @Parameter(names = "-holes", description = "Number of holes to make when rotating non-black pixels (default 12)")
    int holes = 12;

    public static void main(String[] args) throws IOException {
        PartialRotateExtrude rotateExtrude = new PartialRotateExtrude();
        new JCommander(rotateExtrude, args);
        rotateExtrude.run();
    }

    public void run() throws IOException {
        Shape extrusion = new ImageRotated(inputFile, holes)
                .scaleToSize(detail);
        MeshMaker maker = new MeshMaker(extrusion, 100);
        maker.writeMesh(outputFile);
    }
}
