package com.bytebender.premnoybiye.Component;

import javafx.concurrent.Task;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

public class ImageProcessingService {

    /**
     * Opens a file chooser dialog to select an image file
     * 
     * @return Selected File or null if cancelled
     */
    public static File selectImageFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Images", "*.jpeg", "*.jpg", "*.png"));
        return fileChooser.showOpenDialog(new Stage());
    }

    /**
     * Processes an image file synchronously (compression for JPEG, conversion for
     * PNG)
     * 
     * @param imageFile    The input image file
     * @param targetSizeKB Target size in KB (only used for JPEG compression, can be
     *                     null for PNG)
     * @return Processed image file or null if processing failed
     */
    public static File processImageSync(File imageFile, Integer targetSizeKB) {
        if (imageFile == null || !imageFile.exists()) {
            System.err.println("Error: Invalid image file.");
            return null;
        }

        try {
            String imagePath = imageFile.getAbsolutePath();
            String extension = imagePath.substring(imagePath.lastIndexOf(".")).toLowerCase();

            // Determine processing method based on file extension
            if (extension.equals(".png")) {
                return convertPngToJpg(imagePath);
            } else if (extension.equals(".jpg") || extension.equals(".jpeg")) {
                if (targetSizeKB == null) {
                    System.err.println("Error: Please set the target file size in KB for JPEG compression.");
                    return null;
                }
                return compressJpeg(imagePath, targetSizeKB);
            } else {
                System.err.println("Error: Unsupported file format.");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error processing image: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Processes an image file asynchronously
     * 
     * @param imageFile    The input image file
     * @param targetSizeKB Target size in KB
     * @return CompletableFuture with the processed image file
     */
    public static CompletableFuture<File> processImageAsync(File imageFile, Integer targetSizeKB) {
        return CompletableFuture.supplyAsync(() -> processImageSync(imageFile, targetSizeKB));
    }

    /**
     * Processes an image file (compression for JPEG, conversion for PNG)
     * 
     * @param imagePath    Path to the input image
     * @param targetSizeKB Target size in KB (only used for JPEG compression, can be
     *                     null for PNG)
     */
    public static void processImage(String imagePath, Integer targetSizeKB) {
        if (imagePath == null || imagePath.isEmpty()) {
            System.err.println("Error: Please select an image file.");
            return;
        }

        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                System.out.println("Processing image...");

                String extension = imagePath.substring(imagePath.lastIndexOf("."));

                // Determine processing method based on file extension
                if (extension.toLowerCase().equals(".png")) {
                    File result = convertPngToJpg(imagePath);
                    return result != null ? "PNG converted to JPG successfully!" : "Failed to convert PNG";
                } else {
                    if (targetSizeKB == null) {
                        return "Error: Please set the target file size in KB for JPEG compression.";
                    }
                    File result = compressJpeg(imagePath, targetSizeKB);
                    return result != null ? "JPEG compressed successfully!" : "Failed to compress JPEG";
                }
            }
        };

        task.setOnSucceeded(e -> System.out.println("Success: " + task.getValue()));
        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            System.err.println("Error: " + (exception != null ? exception.getMessage() : "Unknown error"));
        });

        new Thread(task).start();
    }

    private static File convertPngToJpg(String srcImg) {
        try {
            System.out.println("Converting PNG to JPG...");

            // Read the PNG image
            BufferedImage pngImage = ImageIO.read(new File(srcImg));

            // Create a new BufferedImage with RGB color model (no transparency)
            BufferedImage jpgImage = new BufferedImage(
                    pngImage.getWidth(),
                    pngImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB);

            // Draw the PNG onto the JPG image with white background
            Graphics2D g2d = jpgImage.createGraphics();
            g2d.setColor(java.awt.Color.WHITE);
            g2d.fillRect(0, 0, jpgImage.getWidth(), jpgImage.getHeight());
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.drawImage(pngImage, 0, 0, null);
            g2d.dispose();

            // Generate output filename
            int dotpos = srcImg.lastIndexOf(".");
            String destImg = srcImg.substring(0, dotpos) + "_converted.jpg";

            // Write the JPG image
            File output = new File(destImg);
            ImageIO.write(jpgImage, "jpg", output);

            System.out.println("Conversion completed!");
            return output;
        } catch (IOException e) {
            System.err.println("Error converting PNG to JPG: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static File compressJpeg(String srcImg, int sizeThreshold) {
        try {
            System.out.println("Starting JPEG compression...");

            File file = new File(srcImg);
            long fileSize = file.length();

            if (fileSize / 1024 <= sizeThreshold) {
                System.out.println("Image file size is already under threshold (" + fileSize / 1024 + "KB ≤ "
                        + sizeThreshold + "KB)");
                return file; // Return original file if already under threshold
            }

            Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
            if (!iter.hasNext()) {
                System.err.println("No JPEG writers available");
                return null;
            }

            ImageWriter writer = iter.next();
            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

            BufferedImage originalImage = ImageIO.read(new FileInputStream(file));
            IIOImage image = new IIOImage(originalImage, null, null);

            float quality = 1.0f;
            float step = 0.1f;

            // Generate output filename
            int dotpos = srcImg.lastIndexOf(".");
            String destImg = srcImg.substring(0, dotpos) + "_compressed" + srcImg.substring(dotpos);

            File fileOut = new File(destImg);

            while (fileSize / 1024 > sizeThreshold && quality > 0.1f) {
                quality -= step;

                System.out.println("Compressing... Quality: " + String.format("%.1f", quality * 100) + "%");

                if (fileOut.exists())
                    fileOut.delete();

                try (FileImageOutputStream output = new FileImageOutputStream(fileOut)) {
                    writer.setOutput(output);
                    params.setCompressionQuality(quality);
                    writer.write(null, image, params);
                }

                long newFileSize = fileOut.length();
                if (newFileSize == fileSize)
                    break;
                fileSize = newFileSize;

                if (quality <= step)
                    step *= 0.1f;
            }

            writer.dispose();
            System.out.println("Compression completed! Final size: " + fileSize / 1024 + "KB");
            return fileOut;
        } catch (IOException e) {
            System.err.println("Error compressing JPEG: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the file size in KB
     * 
     * @param file The file to check
     * @return Size in KB
     */
    public static long getFileSizeKB(File file) {
        if (file != null && file.exists()) {
            return file.length() / 1024;
        }
        return 0;
    }

    /**
     * Check if the file needs processing based on size threshold
     * 
     * @param file        The file to check
     * @param thresholdKB The size threshold in KB
     * @return true if file needs processing
     */
    public static boolean needsProcessing(File file, int thresholdKB) {
        return getFileSizeKB(file) > thresholdKB;
    }
}
