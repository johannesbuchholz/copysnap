package com.github.johannesbuchholz.copysnap.model.state;

import java.nio.file.Path;

/**
 * @apiNote Reusable.
 */
interface FileSystemStateIO {

    FileSystemState read(Path sourceFile);

    void write(FileSystemState fss, Path destination);

}
