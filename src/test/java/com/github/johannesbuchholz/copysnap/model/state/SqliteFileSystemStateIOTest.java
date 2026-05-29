package com.github.johannesbuchholz.copysnap.model.state;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.github.johannesbuchholz.copysnap.util.FileStateUtils.generateRandomFileState;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqliteFileSystemStateIOTest {

    private final SqliteFileSystemStateIO fileSystemStateIO = new SqliteFileSystemStateIO();

    @TempDir
    Path tempDir;

    @Test
    public void serde() {
        Path tempFile = tempDir.resolve(UUID.randomUUID() + "-serde-test.db");
        FileState fs = generateRandomFileState();
        FileSystemState fss = FileSystemState.builder().add(fs).build();

        // serialization
        fileSystemStateIO.write(fss, tempFile);
        assertTrue(Files.exists(tempFile));

        // deserialization
        FileSystemState fssRead = fileSystemStateIO.read(tempFile);
        assertEquals(Set.copyOf(fss.getFileStates()), Set.copyOf(fssRead.getFileStates()));
    }

    @Test
    public void serde_withNewLineInPathName() {
        Path tempFile = tempDir.resolve(UUID.randomUUID() + "-serde-test.db");
        FileState fs = new FileState(Path.of("a/b/c/x\ny\nz"), Instant.now(), new CheckpointChecksum(List.of(0L)));
        FileSystemState fss = FileSystemState.builder().add(fs).build();

        // serialization
        fileSystemStateIO.write(fss, tempFile);
        assertTrue(Files.exists(tempFile));

        // deserialization
        FileSystemState fssRead = fileSystemStateIO.read(tempFile);
        assertEquals(Set.copyOf(fss.getFileStates()), Set.copyOf(fssRead.getFileStates()));
    }

    @Test
    public void serde_withDelimiterInPathName() {
        Path tempFile = tempDir.resolve(UUID.randomUUID() + "-serde-test.db");
        FileState fs = new FileState(Path.of("a/b/c/xyz" + FileState.FIELD_SERDE_SEPARATOR + "bli bla blubb"), Instant.now(), new CheckpointChecksum(List.of(0L)));
        FileSystemState fss = FileSystemState.builder().add(fs).build();

        // serialization
        fileSystemStateIO.write(fss, tempFile);
        assertTrue(Files.exists(tempFile));

        // deserialization
        FileSystemState fssRead = fileSystemStateIO.read(tempFile);
        assertEquals(Set.copyOf(fss.getFileStates()), Set.copyOf(fssRead.getFileStates()));
    }

    @Disabled
    @ParameterizedTest
    @ValueSource(ints = {1_000, 10_000, 100_000, 1_000_000})
    public void serde_stresstest(int fileCount) throws IOException {
        Path tempFile = tempDir.resolve(UUID.randomUUID() + "-serde-large-test.db");

        FileSystemState.Builder builder = FileSystemState.builder();
        Instant now = Instant.now();
        for (int i = 0; i < fileCount; i++) {
            builder.add(new FileState(
                    Path.of("root/dir" + (i % 100) + "/file-" + i),
                    now.plusSeconds(i),
                    new CheckpointChecksum(List.of((long) i))
            ));
        }

        FileSystemState original = builder.build();

        long writeStart = System.currentTimeMillis();
        fileSystemStateIO.write(original, tempFile);
        long writeDuration = System.currentTimeMillis() - writeStart;

        assertTrue(Files.exists(tempFile));

        long fileSize = Files.size(tempFile);

        long readStart = System.currentTimeMillis();
        FileSystemState restored = fileSystemStateIO.read(tempFile);
        long readDuration = System.currentTimeMillis() - readStart;

        assertEquals(
                Set.copyOf(original.getFileStates()),
                Set.copyOf(restored.getFileStates())
        );

        System.out.printf(
                "Serialized %d entries, file size=%d bytes, write=%d ms, read=%d ms%n",
                fileCount, fileSize, writeDuration, readDuration
        );
    }



}