import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Person implements Constant {
    
    private static final String PARSE_PATTERN_STRING = "^(?<personId>\\d+)-FROM-(?<fromFloor>[BF]\\d+)-TO-(?<toFloor>[BF]\\d+)";
    private static final Pattern PARSE_PATTERN = Pattern.compile(PARSE_PATTERN_STRING);
    
    public static boolean matches(String string) {
        Matcher matcher = PARSE_PATTERN.matcher(string);
        return matcher.matches();
    }
    
    public final int id;
    public final int from;
    public final int to;
    
    public Person(String input) {
        Matcher matcher = PARSE_PATTERN.matcher(input);
        if (matcher.matches()) {
            id = Integer.parseInt(matcher.group("personId"));
            String fromString = matcher.group("fromFloor");
            if (fromString.charAt(0) == 'F') {
                this.from = Integer.parseInt(fromString.substring(1)) - 1;
            } else {
                this.from = -Integer.parseInt(fromString.substring(1));
            }
            String toString = matcher.group("toFloor");
            if (toString.charAt(0) == 'F') {
                this.to = Integer.parseInt(toString.substring(1)) - 1;
            } else {
                this.to = -Integer.parseInt(toString.substring(1));
            }
        } else {
            throw new InvalidRequestException("InvalidRequest: " + input);
        }
    }
    
    public Direction getDirection() {
        return to > from ? Direction.UP : Direction.DOWN;
    }
    
}
