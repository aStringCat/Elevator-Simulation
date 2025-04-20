import java.io.PrintStream;

public final class Timestamp {
    
    private static long startTimestamp = 0L;
    private static final PrintStream OUT = System.out;
    
    public static synchronized void initStartTimestamp() {
        if (startTimestamp == 0L) {
            startTimestamp = System.currentTimeMillis();
        }
    }
    
    public static synchronized void println(Object obj) {
        OUT.println(new TimedMessage(obj));
    }
    
    private static class TimedMessage {
        
        private final long timestamp;
        private final Object content;
        
        TimedMessage(Object content) {
            this.timestamp = System.currentTimeMillis();
            this.content = content;
        }
        
        @Override
        public String toString() {
            long totalMs = timestamp - startTimestamp;
            long hours = totalMs / (3600 * 1000);
            long remainingMs = totalMs % (3600 * 1000);
            long minutes = remainingMs / (60 * 1000);
            remainingMs %= (60 * 1000);
            long seconds = remainingMs / 1000;
            long millis = remainingMs % 1000;
            return String.format("[%02d:%02d:%02d.%03d]%s", hours, minutes, seconds, millis, content.toString());
        }
        
    }
    
}