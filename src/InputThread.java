import java.io.IOException;

public class InputThread extends Thread implements Constant {
    
    private final FloorsManager floorsManager;
    
    public InputThread(FloorsManager floorsManager) {
        this.floorsManager = floorsManager;
        super.setName("InputThread");
    }
    
    @Override
    public void run() {
        RequestInput requestInput = new RequestInput(System.in);
        while (true) {
            Person person = requestInput.nextRequest();
            if (person == null) {
                floorsManager.setEnded();
                break;
            }
            floorsManager.addPassenger(person);
        }
        try {
            requestInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}


