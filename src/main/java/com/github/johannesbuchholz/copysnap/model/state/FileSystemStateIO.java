package com.github.johannesbuchholz.copysnap.model.state;

import java.nio.file.Path;

public class FileSystemStateIO {

    private FileSystemStateIO(FileSystemState state) {
        // do not instantiate
    }

    public static FileSystemState read(Path fssFile) {
        String fileName = fssFile.getFileName().toString();
        if (fileName.endsWith(".db")) {
            throw new IllegalArgumentException("Not yet supported: " + fssFile);
        } else {
            return TextFileSystemStateIO.read(fssFile);
        }
    }

}
