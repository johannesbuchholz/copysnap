package johnny.buckels.copysnap.service.logging;

public class Message {

    private final String prefix;
    private final String message;

    public static Message progressInfo(String subject, int processedCount, int totalCount) {
        int percentage = totalCount < 1 ? 0 : (int) (((double) processedCount / totalCount) * 100);
        int maxPercentageLength = 100 / 2;
        int percentageLength = percentage / 2;
        return Message.info("%-24s %3s%% [%s] (%s / %s)",
                subject, percentage, "#".repeat(percentageLength) + " ".repeat(maxPercentageLength - percentageLength), processedCount, totalCount);
    }

    public static Message info(String message, Object... args) {
        return new Message(String.format(message, args));
    }

    public static Message error(String message, Object... args) {
        return new Message(String.format(message, args), "ERROR");
    }

    private Message(String message) {
        this(message, "");
    }

    private Message(String message, String prefix) {
        this.message = message;
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return prefix.isBlank() ? message : prefix + " " + message;
    }

}