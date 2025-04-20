
import com.sun.jdi.request.DuplicateRequestException;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Scanner;

public class RequestInput implements Closeable, Constant {
    
    private final Scanner scanner;
    private final HashSet<Integer> existedPersonId = new HashSet<>();
    
    public RequestInput(InputStream inputStream) {
        this.scanner = new Scanner(inputStream);
    }
    
    public void close() throws IOException {
        this.scanner.close();
    }
    
    public Person nextRequest() {
        while (this.scanner.hasNextLine()) {
            String line = this.scanner.nextLine();
            if (Person.matches(line)) {
                try {
                    Person p = new Person(line);
                    if (p.from == p.to || p.from < BOTTOM_FLOOR || p.from > TOP_FLOOR || p.to < BOTTOM_FLOOR || p.to > TOP_FLOOR) {
                        throw new InvalidRequestException("InvalidRequest" + line);
                    }
                    if (this.existedPersonId.contains(p.id)) {
                        throw new DuplicateRequestException(line);
                    }
                    this.existedPersonId.add(p.id);
                    return p;
                } catch (DuplicateRequestException e) {
                    e.printStackTrace(System.err);
                }
            } else {
                try {
                    throw new InvalidRequestException("InvalidRequest: " + line);
                } catch (InvalidRequestException e) {
                    e.printStackTrace(System.err);
                }
            }
        }
        return null;
    }
    
}
