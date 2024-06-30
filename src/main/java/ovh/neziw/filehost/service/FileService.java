/*
 * This file is part of "Spring-FileHost", licensed under MIT License.
 *
 *  Copyright (c) 2024 neziw
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package ovh.neziw.filehost.service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ovh.neziw.filehost.exception.FileNotFoundException;
import ovh.neziw.filehost.exception.FileStorageException;
import ovh.neziw.filehost.model.FileInfo;

@Service
public class FileService {

    @Value("${storage.location}")
    private String storageLocation;
    private Path rootLocation;

    public void setStorageLocation(final String storageLocation) {
        this.storageLocation = storageLocation;
    }

    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(this.storageLocation);
        try {
            Files.createDirectories(this.rootLocation);
        } catch (final IOException exception) {
            throw new FileStorageException("Could not initialize storage location", exception);
        }
    }

    public void store(final MultipartFile multipartFile) {
        try {
            final String filename = multipartFile.getOriginalFilename();
            Path destinationFile = this.rootLocation.resolve(Paths.get(filename)).normalize().toAbsolutePath();

            if (Files.exists(destinationFile)) {
                final String newFilename = this.getUniqueFilename(filename);
                destinationFile = this.rootLocation.resolve(Paths.get(newFilename)).normalize().toAbsolutePath();
            }

            Files.copy(multipartFile.getInputStream(), destinationFile);
        } catch (final IOException e) {
            throw new FileStorageException("Failed to store file", e);
        }
    }

    public List<FileInfo> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(path -> new FileInfo(path.getFileName().toString(), path.toFile().length()))
                    .toList();
        } catch (final IOException exception) {
            throw new FileStorageException("Failed to read stored files", exception);
        }
    }

    public Resource loadAsResource(final String filename) {
        try {
            final Path file = this.rootLocation.resolve(filename);
            final Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("Failed to read file: " + filename);
            }
        } catch (final MalformedURLException exception) {
            throw new FileNotFoundException("Failed to read file: " + filename, exception);
        }
    }

    private String getUniqueFilename(final String filename) {
        String name = filename;
        String extension = "";
        final int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            name = filename.substring(0, dotIndex);
            extension = filename.substring(dotIndex);
        }

        int count = 1;
        String newFilename;
        Path newFilePath;
        do {
            newFilename = name + "-" + String.format("%02d", count++) + extension;
            newFilePath = this.rootLocation.resolve(Paths.get(newFilename)).normalize().toAbsolutePath();
        } while (Files.exists(newFilePath));

        return newFilename;
    }
}