package com.example.call_track.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.UUID;

@Service
public class AvatarService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private final Random random = new Random();

    public String generateAvatar(String firstName, String lastName) {
        String initials = getInitials(firstName, lastName);
        Color backgroundColor = getRandomColor();
        Color textColor = getContrastColor(backgroundColor);

        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Fill background
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, 100, 100);

        // Draw text
        g2d.setColor(textColor);
        g2d.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (100 - fm.stringWidth(initials)) / 2;
        int y = (100 + fm.getAscent() - fm.getDescent()) / 2;
        g2d.drawString(initials, x, y);

        g2d.dispose();

        // Save to file
        String fileName = UUID.randomUUID().toString() + ".png";
        Path avatarPath = Paths.get(uploadDir, "avatars", fileName);
        try {
            Files.createDirectories(avatarPath.getParent());
            ImageIO.write(image, "png", avatarPath.toFile());
            return "avatars/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate avatar", e);
        }
    }

    public void deleteAvatar(String avatarPath) {
        if (avatarPath != null && !avatarPath.isEmpty()) {
            try {
                Path fullPath = Paths.get(uploadDir, avatarPath);
                Files.deleteIfExists(fullPath);
            } catch (IOException e) {
                // Log error but don't throw
            }
        }
    }

    private String getInitials(String firstName, String lastName) {
        StringBuilder initials = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            initials.append(firstName.charAt(0));
        }
        if (lastName != null && !lastName.isEmpty()) {
            initials.append(lastName.charAt(0));
        }
        return initials.toString().toUpperCase();
    }

    private Color getRandomColor() {
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    private Color getContrastColor(Color background) {
        // Simple contrast: if background is dark, use white; else black
        double luminance = (0.299 * background.getRed() + 0.587 * background.getGreen() + 0.114 * background.getBlue()) / 255;
        return luminance > 0.5 ? Color.BLACK : Color.WHITE;
    }
}