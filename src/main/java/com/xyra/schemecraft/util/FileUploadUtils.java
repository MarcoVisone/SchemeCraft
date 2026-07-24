package com.xyra.schemecraft.util;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

public final class FileUploadUtils {

    private FileUploadUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String saveUploadedFile(HttpServletRequest req, String partName, String subFolder)
            throws IOException, ServletException {

        Part filePart = null;
        try {
            filePart = req.getPart(partName);
        } catch (Exception e) {
            return null;
        }

        return saveUploadedFile(filePart, req.getServletContext().getRealPath(""), subFolder);
    }

    public static String saveUploadedFile(Part filePart, String realContextPath, String subFolder)
            throws IOException {

        if (filePart == null || filePart.getSize() <= 0) {
            return null;
        }

        String originalFileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFileName.substring(dotIndex);
        }

        String uniqueFileName = UUID.randomUUID().toString() + extension;

        String uploadDirPath = realContextPath + File.separator + "uploads" + File.separator + subFolder;
        File uploadDir = new File(uploadDirPath);

        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (!created) {
                throw new IOException("Unable to create upload directory: " + uploadDirPath);
            }
        }

        String destinationPath = uploadDirPath + File.separator + uniqueFileName;
        filePart.write(destinationPath);

        return "uploads/" + subFolder + "/" + uniqueFileName;
    }
}
