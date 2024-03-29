package meshmaker;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.vecmath.Point3d;

import abfab3d.core.AttributeGrid;
import abfab3d.core.MathUtil;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.io.output.MeshMakerMT;
import abfab3d.io.output.STLWriter;
import abfab3d.mesh.IndexedTriangleSetBuilder;
import abfab3d.util.TriangleCounter;
import shape.Shape;

public class MeshMaker {
    private Shape shape;
    double MM = 0.001;
    boolean test = true;
    private double size;

    public MeshMaker(Shape shape, double size) {
        this.shape = shape;
        this.size = size;
    }

    public MeshMaker(Shape shape) {
        this.shape = shape;
        this.size = 0;
    }

    public void writeMesh(String file) throws IOException {
        byte[] b = writeMesh();
        FileOutputStream stream = new FileOutputStream(file);
        try {
            stream.write(b);
        } finally {
            stream.close();
        }
    }

    public byte[] writeMesh() throws IOException {
        long start = System.currentTimeMillis();
        double[] bounds = shape.getBounds();
        bounds = MathUtil.roundBounds(bounds, 1);
        bounds = MathUtil.extendBounds(bounds, 2);
        int nx = (int) Math.round((bounds[1] - bounds[0]));
        int ny = (int) Math.round((bounds[3] - bounds[2]));
        int nz = (int) Math.round((bounds[5] - bounds[4]));
        System.out.println(nx + " " + ny + " " + nz);
        if (size != 0) {
            double voxelSize = Math.max(nx, Math.max(ny, nz)) / size;
            MM /= voxelSize;
        }

        AttributeGrid grid = new ArrayAttributeGridByte(nx, ny, nz, 1, 1);
        double[] mmbounds = new double[] { bounds[0] * MM, bounds[1] * MM,
                bounds[2] * MM, bounds[3] * MM, bounds[4] * MM, bounds[5] * MM };
        grid.setGridBounds(mmbounds);
        for (int z = (int) bounds[4]; z < (int) bounds[5]; z++) {
            System.out.println((z - bounds[4]) / (bounds[5] - bounds[4]) * 100
                    + "%");
            for (int y = (int) bounds[2]; y < (int) bounds[3]; y++) {
                for (int x = (int) bounds[0]; x < (int) bounds[1]; x += 64) {
                    long vals = shape.fastInShape(new Point3d(x, y, z));
                    for (int i = 0; i < 64; i++) {
                        if (x + i < bounds[1]) {
                            // System.out.println((x+i) + " " + y + " " + z);
                            long top = (vals & 0x8000000000000000L);
                            top = top >> 63;
                            top = top & 0x1L;
                            grid.setState(x + i - (int) bounds[0], y
                                    - (int) bounds[2], z - (int) bounds[4],
                                    (byte) top);
                        }
                        vals = vals << 1;
                    }

                }
            }
        }
        System.out.println(System.currentTimeMillis() - start);
        double errorFactor = 0.5 * MM * MM;
        double maxDecimationError = errorFactor;

        // Write out the grid to an STL file
        MeshMakerMT meshmaker = new MeshMakerMT();
        meshmaker.setBlockSize(50);
        meshmaker.setThreadCount(Runtime.getRuntime().availableProcessors());
        meshmaker.setSmoothingWidth(1);
        meshmaker.setMaxDecimationError(maxDecimationError);
        meshmaker.setMaxDecimationCount(10);
        meshmaker.setMaxAttributeValue(1);
        IndexedTriangleSetBuilder triSet = new IndexedTriangleSetBuilder();
        meshmaker.makeMesh(grid, triSet);
        TriangleCounter counter = new TriangleCounter();
        triSet.getTriangles(counter);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        STLWriter stlw = new STLWriter(os, counter.getCount());
        triSet.getTriangles(stlw);
        stlw.close();
        System.out.println(System.currentTimeMillis() - start);
        return os.toByteArray();
    }
}

