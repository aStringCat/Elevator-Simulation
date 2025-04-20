import java.util.LinkedHashMap;

public class DispatchThread extends Thread implements Constant {
    
    private final FloorsManager floorsManager;
    private final LinkedHashMap<Integer, Elevator> elevators;
    
    public DispatchThread(FloorsManager floorsManager, LinkedHashMap<Integer, Elevator> elevators) {
        this.floorsManager = floorsManager;
        this.elevators = elevators;
        super.setName("DispatchThread");
    }
    
    @Override
    public void run() {
        while (true) {
            if (floorsManager.isRequestEmpty() && floorsManager.isEnded()) {
                elevators.values().forEach(Elevator::setEnd);
                break;
            }
            dispatch();
        }
    }
    
    private void dispatch() {
        floorsManager.acquireLock();
        try {
            for (Request request : floorsManager.getRequests()) {
                for (Elevator elevator : elevators.values()) {
                    elevator.acquireLock();
                }
                try {
                    Integer index = selectElevator(request);
                    if (index != null) {
                        Elevator elevator = elevators.get(index);
                        elevator.addTarget(request);
                        floorsManager.removeRequest(request);
                    }
                } finally {
                    for (Elevator elevator : elevators.values()) {
                        elevator.releaseLock();
                    }
                }
            }
            floorsManager.await();
        } finally {
            floorsManager.releaseLock();
        }
    }
    
    private Integer selectElevator(Request request) {
        Integer index = null;
        int distance = Integer.MAX_VALUE;
        for (int i = 1; i <= ELEVATOR_NUMBER; i++) {
            Elevator elevator = elevators.get(i);
            Integer elevatorCost = elevator.getCost(request);
            if (elevatorCost < distance) {
                index = i;
                distance = elevatorCost;
            }
        }
        return index;
    }
    
}
