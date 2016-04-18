package shape;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.vecmath.Point3d;

public class Image extends Shape {

    private BufferedImage img;

    public Image(String path) throws IOException {
        img = ImageIO.read(new File(path));
    }

    @Override
    public boolean inShape(Point3d point) {
        if (point.z >= 0 && point.z < 1 && point.x >= 0
                && point.x < img.getWidth() && point.y >= 0
                && point.y < img.getHeight()) {
            int rgb = img.getRGB((int) point.x, (int) point.y);
            Color color = new Color(rgb);
            return (color.getRed() < 128 && color.getBlue() < 128 && color
                    .getGreen() < 128);
        }
        return false;
    }

    @Override
    public double[] getBounds() {
        return new double[] { 0, img.getWidth(), 0, img.getHeight(), 0, 1 };
    }

}
