package com.github.johannesbuchholz.copysnap.model.state;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.github.johannesbuchholz.copysnap.util.FileStateUtils.generateRandomFileState;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileSystemStatesTextTest {

    private final TextFileSystemStateIO textFileSystemStateIO = new TextFileSystemStateIO();

    @TempDir
    Path tempDir;

    @Test
    public void serde() {
        Path tempFile = tempDir.resolve(UUID.randomUUID() + "-serde-test.txt");
        FileState fs = generateRandomFileState();
        FileSystemState fss = FileSystemState.builder().add(fs).build();

        // serialization
        textFileSystemStateIO.write(fss, tempFile);
        assertTrue(Files.exists(tempFile));

        // deserialization
        FileSystemState fssRead = textFileSystemStateIO.read(tempFile);
        assertEquals(Set.copyOf(fss.getFileStates()), Set.copyOf(fssRead.getFileStates()));
    }

    @Test
    public void serde_withNewLineInPathName() {
        Path tempFile = tempDir.resolve(UUID.randomUUID() + "-serde-test.txt");
        FileState fs = new FileState(Path.of("a/b/c/x\ny\nz"), Instant.now(), new CheckpointChecksum(List.of(0L)));
        FileSystemState fss = FileSystemState.builder().add(fs).build();

        // serialization
        textFileSystemStateIO.write(fss, tempFile);
        assertTrue(Files.exists(tempFile));

        // deserialization
        FileSystemState fssRead = textFileSystemStateIO.read(tempFile);
        assertEquals(Set.copyOf(fss.getFileStates()), Set.copyOf(fssRead.getFileStates()));
    }

    @Test
    public void serde_withDelimiterInPathName() {
        Path tempFile = tempDir.resolve(UUID.randomUUID() + "-serde-test.txt");
        FileState fs = new FileState(Path.of("a/b/c/xyz" + FileState.FIELD_SERDE_SEPARATOR + "bli bla blubb"), Instant.now(), new CheckpointChecksum(List.of(0L)));
        FileSystemState fss = FileSystemState.builder().add(fs).build();

        // serialization
        textFileSystemStateIO.write(fss, tempFile);
        assertTrue(Files.exists(tempFile));

        // deserialization
        FileSystemState fssRead = textFileSystemStateIO.read(tempFile);
        assertEquals(Set.copyOf(fss.getFileStates()), Set.copyOf(fssRead.getFileStates()));
    }

}
