package com.github.johannesbuchholz.copysnap.model.state;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.util.List;

final class SqliteFileSystemStateIO implements FileSystemStateIO {

    private static final String TABLE_NAME = "file_states";
    private static final List<String> COL_NAMES = List.of("path", "last_modified", "checksum");

    @Override
    public FileSystemState read(Path sourceFile) {
        if (!sourceFile.isAbsolute() || !Files.exists(sourceFile)) {
            throw new IllegalArgumentException("Source file does not exist or path is absolute: " + sourceFile);
        }

        FileSystemState.Builder builder = FileSystemState.builder();
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + sourceFile.toUri() + "?mode=ro")) {
            try (Statement stmt = c.createStatement()) {
                stmt.execute("PRAGMA query_only = ON");
                stmt.execute("PRAGMA cache_size = -20000"); // ~20MB cache
            }
            try (PreparedStatement ps = c.prepareStatement("SELECT %s FROM %s".formatted(String.join(", ", COL_NAMES), TABLE_NAME))) {
                ps.setFetchSize(10_000);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    builder.add(
                            new FileState(
                                    Path.of(rs.getString(1)),
                                    Instant.parse(rs.getString(2)),
                                    CheckpointChecksum.deserialize(rs.getString(3)))
                    );
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read from database file at %s: %s".formatted(sourceFile, e.getMessage()), e);
        }
        return builder.build();
    }

    @Override
    public void write(FileSystemState fss, Path destination) {
        if (!destination.isAbsolute()) {
            throw new IllegalArgumentException("File path is not absolute: " + destination);
        } else if (Files.exists(destination)) {
            // delete file
            try {
                Files.deleteIfExists(destination);
            } catch (IOException e) {
                throw new UncheckedIOException("Could not delete existing database file: " + destination, e);
            }
        }

        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + destination)) {
            // Speed optimizations
            try (Statement stmt = c.createStatement()) {
                stmt.execute("PRAGMA journal_mode = WAL");
                stmt.execute("PRAGMA synchronous = OFF");
                stmt.execute("PRAGMA temp_store = MEMORY");
                stmt.execute("CREATE TABLE %s (%s)".formatted(TABLE_NAME, String.join(", ", COL_NAMES)));
            }

            c.setAutoCommit(false);
            int batchSize = Math.clamp(fss.fileCount() / 100, 100, 10_000);
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO %s (%s) VALUES (?, ?, ?)"
                    .formatted(TABLE_NAME, String.join(", ", COL_NAMES)))) {
                int rowCount = 0;
                for (FileState fs : fss.getFileStates()) {
                    ps.setString(1, fs.getPath().toString());
                    ps.setString(2, fs.getLastModified().toString());
                    ps.setString(3, fs.getChecksum().serialize());
                    ps.addBatch();
                    rowCount += 1;

                    if (rowCount % batchSize == 0) {
                        ps.executeBatch();
                        c.commit();
                        ps.clearBatch();
                    }
                }
                // flush remaining
                ps.executeBatch();
                c.commit();
            }

            c.setAutoCommit(true);
            try (Statement stmt = c.createStatement()) {
                stmt.execute("PRAGMA wal_checkpoint(FULL)");
                stmt.execute("PRAGMA journal_mode = DELETE");
                stmt.execute("PRAGMA synchronous = NORMAL");
                // Compact DB file
                stmt.execute("VACUUM");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not write to database file at %s: %s".formatted(destination, e.getMessage()), e);
        }
    }

}
