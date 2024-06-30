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
package ovh.neziw.filehost;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import ovh.neziw.filehost.model.FileInfo;
import ovh.neziw.filehost.service.FileService;

@SpringBootTest
@ActiveProfiles("test")
public class FileServiceTest {

    @Value("${storage.location}")
    private String storageLocation;
    @InjectMocks
    private FileService fileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.fileService = new FileService();
        this.fileService.setStorageLocation(this.storageLocation);
        this.fileService.init();
        this.cleanUp();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        this.cleanUp();
    }

    private void cleanUp() {
        try {
            final Path filePath = Paths.get(this.storageLocation, "test.txt");
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (final Exception ignored) {
            // Ignore exceptions during cleanup
        }
    }

    @Test
    void testStore() {
        final MockMultipartFile file = new MockMultipartFile("multipartFile", "test.txt", "text/plain", "Test file content".getBytes());
        this.fileService.store(file);

        final Path destinationFile = Paths.get(this.storageLocation).resolve(file.getOriginalFilename()).normalize().toAbsolutePath();
        assertTrue(Files.exists(destinationFile));
    }

    @Test
    void testLoadAll() throws Exception {
        final Path filePath = Paths.get(this.storageLocation, "test.txt");
        Files.createFile(filePath);
        final List<FileInfo> files = this.fileService.loadAll();

        assertFalse(files.isEmpty());
        Files.delete(filePath);
    }

    @Test
    void testLoadAsResource() throws Exception {
        final Path filePath = Paths.get(this.storageLocation, "test.txt");
        Files.createFile(filePath);
        final Resource resource = this.fileService.loadAsResource("test.txt");

        assertTrue(resource.exists());
        Files.delete(filePath);
    }
}