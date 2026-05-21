package com.github.johannesbuchholz.copysnap.model.state;

import com.github.johannesbuchholz.copysnap.model.ContextIOException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class TextFileSystemStateIO {

    private static final String FIELD_SERDE_SEPARATOR = ";";
    private static final Pattern FILE_STATE_STRING_PATTERN = Pattern.compile("^(?<checksum>[^;]+);(?<modified>[^;]+);(?<path>(?s).+)$");

    /**
     * Reads a file of the form
     * <p>
     * #ROOT_PATH
     * #DATE
     * #HASH & PATH
     * ...
     * #HASH & #PATH
     * </p>
     */
    public static FileSystemState read(Path textFile) {
        FileSystemState.Builder builder = FileSystemState.builder();
        Optional<String> nextLineOpt;
        try (InputStream is = Files.newInputStream(textFile)) {
            while ((nextLineOpt = readUntilNextNull(is)).isPresent()) {
                FileState fs = deserialize(nextLineOpt.get());
                builder.add(fs);
            }
        } catch (IOException e) {
            throw new ContextIOException("Could not read FileSystemState from %s: %s".formatted(textFile, e.getMessage()), e);
        }
        return builder.build();
    }

    private static Optional<String> readUntilNextNull(InputStream is) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(1024);
        int nextByte;
        while ((nextByte = is.read()) > -1) {
            if (nextByte == Character.MIN_VALUE) {
                // skip new line character
                long ignored = is.skip(1);
                ByteBuffer slice = bb.slice(0, bb.position());
                return Optional.of(StandardCharsets.UTF_8.decode(slice).toString());
            }
            if (bb.position() >= bb.capacity()) {
                ByteBuffer bbNew = ByteBuffer.allocate(bb.capacity() * 2);
                bb = bbNew.put(bb.array());
            }
            bb.put((byte) nextByte);
        }
        // the stream is exhausted. We do not return the buffer since no null byte has been found.
        return Optional.empty();
    }

    public static void write(FileSystemState fss, Path destination) {
        try (OutputStream fileSystemStateOs = Files.newOutputStream(destination, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
             OutputStreamWriter writer = new OutputStreamWriter(fileSystemStateOs)
        ) {
            for (FileState fileState : fss.getStatesByPath().values()) {
                writeLine(writer, serialize(fileState));
            }
            writer.flush();
        } catch (IOException e) {
            throw new ContextIOException("Could not write latest file states to %s: %s".formatted(destination, e.getMessage()), e);
        }
    }

    /**
     * Each line is finished by {@link Character#MIN_VALUE}.
     * When parsing the file, line-wise reading could lead to errors since the path-strings may contain new-line
     * characters. To solve that issue, we decide to put "anchors" at the end of each line.
     * We also put new-line characters between each entry, to maintain human readability.
     */
    private static void writeLine(Writer bw, String s) throws IOException {
        bw.write(s);
        bw.write(Character.MIN_VALUE);
        bw.write(System.lineSeparator());
    }

    private static String serialize(FileState fileState) {
        return String.join(FIELD_SERDE_SEPARATOR,
                List.of(
                        fileState.checksum().serialize(),
                        fileState.lastModified().toString(),
                        fileState.path().toString())
        );
    }

    private static FileState deserialize(String fileStateString) {
        Matcher matcher = FILE_STATE_STRING_PATTERN.matcher(fileStateString);
        if (!matcher.find())
            throw new IllegalArgumentException("Could not create file state from string %s: Does not match pattern %s".formatted(fileStateString, FILE_STATE_STRING_PATTERN));
        CheckpointChecksum checksum = CheckpointChecksum.deserialize(matcher.group("checksum"));
        Instant modified = Instant.parse(matcher.group("modified"));
        Path path = Path.of(matcher.group("path"));
        return new FileState(path, modified, checksum);
    }

}
