package com.github.johannesbuchholz.copysnap.model.state;

import java.nio.file.Path;

public class FileSystemStates {

    /** Legacy */
    private static final FileSystemStateIO TEXT_IO = new TextFileSystemStateIO();
    private static final FileSystemStateIO SQLITE_IO = new SqliteFileSystemStateIO();

    private FileSystemStates() {
        // do not instantiate
    }

    public static FileSystemState read(Path fssSourceFile) {
        String fileName = fssSourceFile.getFileName().toString();
        FileSystemStateIO io;
        if (fileName.endsWith(".db")) {
            io = SQLITE_IO;
        } else {
            io = TEXT_IO;
        }
        return io.read(fssSourceFile);
    }

    public static void write(FileSystemState fss, Path fssDestination) {
        String fileName = fssDestination.getFileName().toString();
        FileSystemStateIO io;
        if (fileName.endsWith(".db")) {
            io = SQLITE_IO;
        } else {
            io = TEXT_IO;
        }
        io.write(fss, fssDestination);
    }

}
