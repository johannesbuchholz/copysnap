package com.github.johannesbuchholz.copysnap.model.state;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public record FileState(Path path, Instant lastModified, CheckpointChecksum checksum) {

    static final String FIELD_SERDE_SEPARATOR = ";";

    public static FileState readFileState(Path rootToRelativizeAgainst, Path absPath) throws UncheckedIOException {
        Instant lastModified;
        try {
            lastModified = Files.getLastModifiedTime(absPath).toInstant();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read last modified from %s: %s".formatted(absPath, e.getMessage()), e);
        }
        CheckpointChecksum checksum;
        try {
            checksum = CheckpointChecksum.from(Files.newInputStream(absPath));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create checksum from %s: %s".formatted(absPath, e.getMessage()), e);
        }
        return new FileState(rootToRelativizeAgainst.relativize(absPath), lastModified, checksum);
    }

    public FileState {
        if (path.isAbsolute()) {
            throw new IllegalArgumentException("Given path is not relative: " + path);
        }
    }

    public Path getPath() {
        return path;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public CheckpointChecksum getChecksum() {
        return checksum;
    }

}
