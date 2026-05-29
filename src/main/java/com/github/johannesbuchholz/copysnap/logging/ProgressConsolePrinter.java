package com.github.johannesbuchholz.copysnap.logging;

import java.io.PrintStream;

public class ProgressConsolePrinter {

    private static final int BAR_WIDTH = 32;
    private static final PrintStream OUT = System.out;

    private final String prefix;

    public ProgressConsolePrinter(String prefix) {
        this.prefix = prefix;
    }

    public void newLine() {
        OUT.println();
    }

    /**
     * PREFIX [#########--------------]  64/250 ( 26%)
     */
    public void update(int completed, int total) {
        OUT.print("\r\033[K"); // clear line
        String progressString = computeFormatString(completed, total);
        OUT.print(progressString);
        OUT.flush();
    }

    private String computeFormatString(int completed, int total) {
        double ratio = total < 1 ? 1 : (double) completed / total;

        int filledCount = (int) Math.round(ratio * BAR_WIDTH);
        // the width (number of digits) needed to neatly align completed and total in the progress output
        int maxDigitCount = Math.max(1, (int) Math.floor(Math.log10(Math.max(completed, total))) + 1);

        return ("%s [%s%s] %" + maxDigitCount + "d/%d (%3.0f%%)").formatted(
                prefix,
                "#".repeat(filledCount),
                "-".repeat(BAR_WIDTH - filledCount),
                completed,
                total,
                ratio * 100
        );
    }

}
