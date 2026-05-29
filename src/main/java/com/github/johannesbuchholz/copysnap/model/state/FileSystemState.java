package com.github.johannesbuchholz.copysnap.model.state;

import java.nio.file.Path;
import java.util.*;

import static java.util.function.Predicate.not;

public class FileSystemState {

    public static FileSystemState empty() {
        return new FileSystemState(Map.of());
    }

    public static FileSystemState.Builder builder() {
        return new Builder();
    }

    public static FileSystemState.Builder builder(FileSystemState existingState) {
        return new Builder(existingState.statesByPath);
    }

    private final Map<Path, FileState> statesByPath;

    Collection<FileState> getFileStates() {
        return statesByPath.values();
    }

    private FileSystemState(Map<Path, FileState> statesByPath) {
        this.statesByPath = statesByPath;
    }

    public Optional<FileState> get(Path relativePath) {
        return Optional.ofNullable(statesByPath.get(relativePath));
    }

    /**
     * @return A new state with all states from this that are contained in the specified paths.
     */
    public FileSystemState newBySetUnion(Set<Path> otherPaths) {
        Builder builder = FileSystemState.builder(this);
        statesByPath.keySet().stream()
                .filter(not(otherPaths::contains))
                .forEach(builder::remove);
        return builder.build();
    }

    /**
     * @return A new state with all states from this that are not contained in the specified state.
     */
    public FileSystemState newBySetMinus(FileSystemState other) {
        Builder builder = FileSystemState.builder(this);
        statesByPath.keySet().stream()
                .filter(other.statesByPath::containsKey)
                .forEach(builder::remove);
        return builder.build();
    }

    public int fileCount() {
        return statesByPath.size();
    }

    public Set<Path> paths() {
        return Collections.unmodifiableSet(statesByPath.keySet());
    }

    /**
     * Not thread safe
     */
    public static class Builder {

        private final Map<Path, FileState> statesByPath;

        private Builder() {
            this(Map.of());
        }

        private Builder(Map<Path, FileState> statesByPath) {
            this.statesByPath = new HashMap<>(statesByPath);
        }

        public Builder add(FileState fileState) {
            if (fileState.getPath().isAbsolute()) {
                throw new IllegalArgumentException("Can not add absolute path: " + fileState.getPath());
            }
            statesByPath.put(fileState.getPath(), fileState);
            return this;
        }

        public void remove(Path path) {
            statesByPath.remove(path);
        }

        public FileSystemState build() {
            return new FileSystemState(Collections.unmodifiableMap(statesByPath));
        }

    }

}
