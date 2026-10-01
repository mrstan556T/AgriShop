package com.agrishop.web.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import jakarta.faces.context.FacesContext;
import org.primefaces.model.file.UploadedFile;

public class FileUploadUtil {

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(
        Arrays.asList("jpg", "jpeg", "png", "webp", "gif")
    );
    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(
        Arrays.asList("image/jpeg", "image/png", "image/webp", "image/gif")
    );

    public static String saveFile(UploadedFile file, String folder) throws Exception {
        if (file == null || file.getSize() == 0) return null;

        // 1. Validate File Size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new Exception("Dung lượng tệp vượt quá giới hạn cho phép (tối đa 2MB).");
        }

        // 2. Validate MIME Type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new Exception("Định dạng tệp không hợp lệ. Chỉ chấp nhận định dạng ảnh (JPEG, PNG, WEBP, GIF).");
        }

        // 3. Validate Extension
        String originalName = file.getFileName();
        if (originalName == null || originalName.trim().isEmpty()) {
            throw new Exception("Tên tệp không hợp lệ.");
        }

        int dotIdx = originalName.lastIndexOf(".");
        if (dotIdx == -1 || dotIdx == originalName.length() - 1) {
            throw new Exception("Tệp tải lên thiếu phần mở rộng.");
        }

        String ext = originalName.substring(dotIdx + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new Exception("Phần mở rộng tệp '." + ext + "' không được phép tải lên hệ thống.");
        }

        // 4. Secure Random UUID Filename (prevents path traversal & execution)
        String safeFileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;

        try {
            FacesContext context = FacesContext.getCurrentInstance();
            String rootPath = context.getExternalContext().getRealPath("/");
            String deployPath = rootPath + "assets/images/" + folder;
            String sourcePath = "D:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/assets/images/" + folder;

            byte[] fileContent = file.getContent();

            try {
                saveToPath(fileContent, deployPath, safeFileName);
            } catch (Exception e) {}

            try {
                saveToPath(fileContent, sourcePath, safeFileName);
            } catch (Exception e) {}

            return "/assets/images/" + folder + "/" + safeFileName;
        } catch (Exception e) {
            throw new Exception("Lỗi khi lưu tệp ảnh trên máy chủ: " + e.getMessage());
        }
    }

    private static void saveToPath(byte[] bytes, String folderPath, String fileName) throws Exception {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File targetFile = new File(folder, fileName);
        try (OutputStream out = new FileOutputStream(targetFile)) {
            out.write(bytes);
        }
    }
}
