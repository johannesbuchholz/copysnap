package com.github.johannesbuchholz.copysnap.model;

import com.github.johannesbuchholz.copysnap.model.state.FileSystemState;
import com.github.johannesbuchholz.copysnap.model.state.FileSystemStates;
import com.github.johannesbuchholz.copysnap.util.FileStateUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileSystemStateTest {

    private static Path tmpFilePath;

    @BeforeAll
    public static void createTmpDir() throws IOException {
        tmpFilePath = Files.createTempDirectory("copysnap_unittest");
        System.out.println("CREATED TEMP DIRECTORY AT " + tmpFilePath);
    }

    @Test
    @Disabled("Only for manual performance test")
    public void serde() throws IOException {
        int fileCount = 10_000;

        // given
        long start = System.currentTimeMillis();
        FileSystemState.Builder builder = FileSystemState.builder();
        IntStream.range(0, fileCount).forEach(i -> builder.add(FileStateUtils.generateRandomFileState()));
        FileSystemState fst = builder.build();
        long initEnd = System.currentTimeMillis();

        // when ser + de
        Path tempFile = Files.createTempFile(tmpFilePath, "tmpfile" + System.currentTimeMillis(), ".tmp");
        FileSystemStates.write(fst, tempFile);
        long serEnd = System.currentTimeMillis();
        FileSystemState deserializedFst = FileSystemStates.read(tempFile);
        long deEnd = System.currentTimeMillis();

        // then
        System.out.println("File count: " + fileCount);
        System.out.println("Init: " + (initEnd - start) / 1000.0);
        System.out.println("Ser: " + (serEnd - initEnd) / 1000.0);
        System.out.println("De: " + (deEnd - serEnd) / 1000.0);
        assertEquals(fst.paths(), deserializedFst.paths());
    }

}
