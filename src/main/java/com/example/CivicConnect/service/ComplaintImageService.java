package com.example.CivicConnect.service;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

@Service
public class ComplaintImageService {

    private static final String UPLOAD_DIR = "C:/civicconnect/uploads/";

    public Resource loadImage(String fileName) {
        try {
            Path path = Paths.get(UPLOAD_DIR).resolve(fileName);
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Image not found");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to load image", e);
        }
    }
}
