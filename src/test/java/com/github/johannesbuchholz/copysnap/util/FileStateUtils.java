package com.github.johannesbuchholz.copysnap.util;

import com.github.johannesbuchholz.copysnap.model.state.CheckpointChecksum;
import com.github.johannesbuchholz.copysnap.model.state.FileState;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

public class FileStateUtils {

    private static final Random RNG = new Random();

    /**
     * File state with root "/".
     */
    public static FileState generateRandomFileState() {
        byte[] bytes = new byte[RNG.nextInt(1, 10_000)];
        RNG.nextBytes(bytes);
        // 97 = 'a', 122 = 'z'
        Path p = getRandomRelativePath(RNG.nextInt(1, 8));
        CheckpointChecksum checkpointChecksum = CheckpointChecksum.from(new ByteArrayInputStream(bytes));
        return new FileState(p, Instant.now(), checkpointChecksum);
    }

    /**
     * @return Path.of("g/a/c/a/a/h/i/").
     */
    private static Path getRandomRelativePath(int randomPartLength) {
        String[] randomParts = Stream.concat(
                        RNG.ints(97, 123)
                                .limit(randomPartLength)
                                .mapToObj(Character::toChars)
                                .map(String::new),
                        Stream.of(UUID.randomUUID().toString()))
                .toArray(String[]::new);
        return Path.of("", randomParts);
    }

}
