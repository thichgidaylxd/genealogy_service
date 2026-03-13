package com.nckh.genealogy.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    /**
     * Upload file lên Cloudinary
     * @return [url, publicId]
     */
    String[] upload(MultipartFile file, String folder);

    /**
     * Xóa file khỏi Cloudinary theo publicId
     */
    void delete(String publicId);
}