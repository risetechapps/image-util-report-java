package br.app.risetech.image.tools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.UUID;

public class Main {

    public static void main(String[] args) {

    }

    public static String makeRoundedCorner(String path) throws IOException {
        return makeRoundedCorner(path, 300, 300);
    }

    public static String makeRoundedCorner(String path, int targetWidth, int targetHeight) throws IOException {
        return rounded(path, targetWidth, targetHeight);
    }

    private static String rounded(String path, int targetWidth, int targetHeight) throws IOException {
        BufferedImage originalImage = loadImage(path);

        if (originalImage == null) {
            originalImage = loadDefaultImage();
        }

        int newSize = targetWidth + 10;
        BufferedImage resizedImage = resizeImageWithHighQuality(originalImage, newSize, newSize);

        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = outputImage.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        BufferedImage mask = createSoftMask(targetWidth, targetHeight, 2);

        g2.setClip(new Ellipse2D.Float(0, 0, targetWidth, targetHeight));
        g2.drawImage(resizedImage, 0, 0, null);
        g2.setClip(null);

        g2.drawImage(mask, 0, 0, null);
        g2.dispose();

        File outputFile = File.createTempFile(UUID.randomUUID().toString(), ".png");
        ImageIO.write(outputImage, "png", outputFile);

        return outputFile.getAbsolutePath();
    }

    private static BufferedImage createSoftMask(int width, int height, int borderSize) {
        BufferedImage mask = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = mask.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(borderSize * 2));
        g2.draw(new Ellipse2D.Float(borderSize / 2f, borderSize / 2f, width - borderSize, height - borderSize));

        g2.dispose();
        return mask;
    }

    private static BufferedImage loadImage(String path) throws IOException {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return ImageIO.read(new File(downloadImage(path)));
        } else {
            return ImageIO.read(new File(path));
        }
    }

    private static String downloadImage(String imageUrl) throws IOException {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
            connection.setRequestProperty("Referer", "https://www.google.com/");
            connection.setRequestProperty("Accept", "image/webp,image/apng,image/*,*/*;q=0.8");

            connection.connect();

            int responseCode = connection.getResponseCode();

            File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".png");

            if (responseCode != 200) {
                BufferedImage image = loadDefaultImage();
                ImageIO.write(image, "png", tempFile);

                return tempFile.getAbsolutePath();
            }

            try (InputStream inputStream = connection.getInputStream()) {
                BufferedImage image = ImageIO.read(inputStream);
                ImageIO.write(image, "png", tempFile);
            }

            return tempFile.getAbsolutePath();

        } catch (Exception e) {

            File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".png");
            BufferedImage image = loadDefaultImage();
            ImageIO.write(image, "png", tempFile);

            return tempFile.getAbsolutePath();
        }
    }

    private static BufferedImage resizeImageWithHighQuality(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return resizedImage;
    }

    private static BufferedImage loadDefaultImage() throws IOException {
        InputStream inputStream = Main.class.getResourceAsStream("/default.png");
        assert inputStream != null;
        return ImageIO.read(inputStream);
    }
}
