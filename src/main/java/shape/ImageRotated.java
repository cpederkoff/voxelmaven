package shape;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.vecmath.Point3d;

public class ImageRotated extends Shape {
    private BufferedImage img;

    public ImageRotated(String path) throws IOException {
        img = ImageIO.read(new File(path));
    }

    @Override
    public boolean inShape(Point3d point) {

        double distFromY = Math.sqrt(point.x * point.x + point.z * point.z);
        if (distFromY >= img.getWidth() || point.y < 0
                || point.y >= img.getHeight()) {
            return false;
        }
        int rgb = img.getRGB((int) distFromY, (int) point.y);
        Color color = new Color(rgb);
        double greyscale = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
        greyscale = 255 - greyscale;
        // now black = 255; white = 0
        if (greyscale == 255) {
            return true;
        }
        if (greyscale == 0) {
            return false;
        }
        double angleDeg = ((Math.atan2(point.z, point.x) * 180 / Math.PI) + 360) % 360;
        int period = 30;
        double cycle = angleDeg % period;
        if (cycle / period < greyscale / 255) {
            return true;
        }
        return false;

    }

    @Override
    public double[] getBounds() {
        return new double[] { -img.getWidth(), img.getWidth(), 0,
                img.getHeight(), -img.getWidth(), img.getWidth() };
    }

}
