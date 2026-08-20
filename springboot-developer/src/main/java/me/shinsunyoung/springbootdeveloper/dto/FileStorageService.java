package me.shinsunyoung.springbootdeveloper.dto;

public interface FileStorageService {
    UploadResponse store(byte[] bytes, String filename);
}
