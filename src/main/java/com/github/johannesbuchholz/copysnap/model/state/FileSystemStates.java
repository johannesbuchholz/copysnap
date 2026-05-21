package com.github.johannesbuchholz.copysnap.model.state;

import java.nio.file.Path;

public class FileSystemStates {

    private static final FileSystemStateIO TEXT_IO = new TextFileSystemStateIO();

    private FileSystemStates() {
        // do not instantiate
    }

    public static FileSystemState read(Path fssSourceFile) {
        String fileName = fssSourceFile.getFileName().toString();
        if (fileName.endsWith(".db")) {
            throw new IllegalArgumentException("Not yet supported: " + fssSourceFile);
        } else {
            return TEXT_IO.read(fssSourceFile);
        }
    }

    public static void write(FileSystemState fss, Path fssDestination) {
        String fileName = fssDestination.getFileName().toString();
        if (fileName.endsWith(".db")) {
            throw new IllegalArgumentException("Not yet supported: " + fssDestination);
        } else {
            TEXT_IO.write(fss, fssDestination);
        }
    }

}
