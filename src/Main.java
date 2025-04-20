import java.util.LinkedHashMap;

public class Main implements Constant {
    
    public static void main(String[] args) {
        Timestamp.initStartTimestamp();
        System.out.println("--- Elevator Simulation Starting ---");
        FloorsManager floorsManager = new FloorsManager();
        LinkedHashMap<Integer, Elevator> elevators = new LinkedHashMap<>();
        for (int i = 1; i <= ELEVATOR_NUMBER; i++) {
            Elevator elevator = new Elevator(i);
            elevators.put(i, elevator);
            ScheduleThread scheduleThread = new ScheduleThread(elevator, floorsManager);
            scheduleThread.start();
        }
        InputThread inputThread = new InputThread(floorsManager);
        inputThread.start();
        DispatchThread dispatchThread = new DispatchThread(floorsManager, elevators);
        dispatchThread.start();
    }
    
}