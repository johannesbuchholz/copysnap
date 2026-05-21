package com.github.johannesbuchholz.copysnap.model.state;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileSystemStateIOTextTest {

    private static final int HASH_SIZE = 16;
    private static final int MAX_PATH_LENGTH = 8;
    private static final Random RNG = new Random();

    @TempDir
    Path tempDir;

    @Test
    public void serde() {
        Path tempFile = tempDir.resolve(UUID.randomUUID() + "-serde-test.txt");
        FileState fs = generateRandomFileState();
        FileSystemState fss = FileSystemState.builder().add(fs).build();

        // serialization
        TextFileSystemStateIO.write(fss, tempFile);
        assertTrue(Files.exists(tempFile));

        // deserialization
        FileSystemState fssRead = TextFileSystemStateIO.read(tempFile);
        assertEquals(fss.getStatesByPath(), fssRead.getStatesByPath());
    }

    @Test
    public void serde_withNewLineInPathName() {
        Path tempFile = tempDir.resolve(UUID.randomUUID() + "-serde-test.txt");
        FileState fs = new FileState(Path.of("a/b/c/x\ny\nz"), Instant.now(), new CheckpointChecksum(List.of(0L)));
        FileSystemState fss = FileSystemState.builder().add(fs).build();

        // serialization
        TextFileSystemStateIO.write(fss, tempFile);
        assertTrue(Files.exists(tempFile));

        // deserialization
        FileSystemState fssRead = TextFileSystemStateIO.read(tempFile);
        assertEquals(fss.getStatesByPath(), fssRead.getStatesByPath());
    }

    @Test
    public void serde_withDelimiterInPathName() {
        Path tempFile = tempDir.resolve(UUID.randomUUID() + "-serde-test.txt");
        FileState fs = new FileState(Path.of("a/b/c/xyz" + FileState.FIELD_SERDE_SEPARATOR + "bli bla blubb"), Instant.now(), new CheckpointChecksum(List.of(0L)));
        FileSystemState fss = FileSystemState.builder().add(fs).build();

        // serialization
        TextFileSystemStateIO.write(fss, tempFile);
        assertTrue(Files.exists(tempFile));

        // deserialization
        FileSystemState fssRead = TextFileSystemStateIO.read(tempFile);
        assertEquals(fss.getStatesByPath(), fssRead.getStatesByPath());
    }

    /**
     * File state with root "/".
     */
    private FileState generateRandomFileState() {
        return generateRandomFileState(RNG.nextInt(MAX_PATH_LENGTH));
    }

    private FileState generateRandomFileState(int randomPartLength) {
        byte[] bytes = new byte[HASH_SIZE];
        RNG.nextBytes(bytes);
        // 97 = 'a', 122 = 'z'
        Path p = getRandomRelativePath(randomPartLength);
        CheckpointChecksum checkpointChecksum = CheckpointChecksum.from(new ByteArrayInputStream(bytes));
        return new FileState(p, Instant.now(), checkpointChecksum);
    }

    /**
     * @return Path.of("g/a/c/a/a/h/i/").
     */
    private Path getRandomRelativePath(int length) {
        String[] randomParts = Stream.concat(
                        RNG.ints(97, 123)
                                .limit(length)
                                .mapToObj(Character::toChars)
                                .map(String::new),
                        Stream.of(UUID.randomUUID().toString()))
                .toArray(String[]::new);
        return Path.of("", randomParts);
    }


}
