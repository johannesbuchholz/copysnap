package com.github.johannesbuchholz.copysnap.model.state;

import java.nio.file.Path;

public class FileSystemStateIO {

    private FileSystemStateIO(FileSystemState state) {
        // do not instantiate
    }

    public static FileSystemState read(Path fssSourceFile) {
        String fileName = fssSourceFile.getFileName().toString();
        if (fileName.endsWith(".db")) {
            throw new IllegalArgumentException("Not yet supported: " + fssSourceFile);
        } else {
            return TextFileSystemStateIO.read(fssSourceFile);
        }
    }

    public static void write(FileSystemState fss, Path fssDestination) {
        String fileName = fssDestination.getFileName().toString();
        if (fileName.endsWith(".db")) {
            throw new IllegalArgumentException("Not yet supported: " + fssDestination);
        } else {
            TextFileSystemStateIO.write(fss, fssDestination);
        }
    }

}
