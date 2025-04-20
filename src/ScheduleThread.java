import java.util.HashSet;
import java.util.stream.Collectors;

public class ScheduleThread extends Thread implements Constant {
    
    private final int index;
    private final Elevator elevator;
    private final FloorsManager floorsManager;
    
    public ScheduleThread(Elevator elevator, FloorsManager floorsManager) {
        this.index = elevator.index;
        this.elevator = elevator;
        this.floorsManager = floorsManager;
        super.setName("ScheduleThread" + index);
    }
    
    @Override
    public void run() {
        while (!elevator.isEmpty() || !elevator.isEnded()) {
            floorsManager.signalAll();
            if (elevator.getState() != Elevator.State.IDLE) {
                processCurrentFloor();
                updateElevatorState();
                executeMovement();
            }
        }
        
    }
    
    private void processCurrentFloor() {
        if (shouldOpenDoor()) {
            handleDoorOperations(floorsManager.getFloor(elevator.getFloor()));
        }
    }
    
    private boolean shouldOpenDoor() {
        elevator.acquireLock();
        try {
            return hasArrivingPassengers() || shouldPickUpPassengers();
        } finally {
            elevator.releaseLock();
        }
    }
    
    private boolean hasArrivingPassengers() {
        HashSet<Person> passengers = elevator.getPassengers();
        return passengers.stream().anyMatch(p -> p.to == elevator.getFloor());
    }
    
    private boolean shouldPickUpPassengers() {
        if (elevator.availableSpace() == 0) {
            return false;
        }
        Floor floor = floorsManager.getFloor(elevator.getFloor());
        floor.acquireLock();
        try {
            return hasSameDirection(floor);
        } finally {
            floor.releaseLock();
        }
    }
    
    private void handleDoorOperations(Floor floor) {
        floor.acquireLock();
        try {
            floor.receiveElevator(elevator.getState());
        } finally {
            floor.releaseLock();
        }
        Timestamp.println(index + "-OPEN-" + floorToString(floor.floorNumber));
        try {
            sleep(ELEVATOR_DOOR_TIME);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        handlePassengerMovement(floor);
        Timestamp.println(index + "-CLOSE-" + floorToString(floor.floorNumber));
        try {
            sleep(ELEVATOR_DOOR_TIME);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void handlePassengerMovement(Floor floor) {
        elevator.acquireLock();
        HashSet<Person> out = elevator.getPassengers().stream()
                .filter(p -> p.to == elevator.getFloor())
                .collect(Collectors.toCollection(HashSet::new));
        elevator.releaseLock();
        out.forEach(this::unloadPassenger);
        while (elevator.availableSpace() != 0 && hasSameDirection(floor)) {
            Person person = floor.removePassenger(elevator.getState());
            loadPassenger(person);
        }
    }
    
    private void loadPassenger(Person person) {
        Timestamp.println(index + "-LOAD-" + person.id + "-AT-" + floorToString(elevator.getFloor()));
        try {
            sleep(PASSENGER_MOVE_TIME);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        elevator.loadPassenger(person);
    }
    
    private void unloadPassenger(Person person) {
        Timestamp.println(index + "-UNLOAD-" + person.id + "-AT-" + floorToString(elevator.getFloor()));
        try {
            sleep(PASSENGER_MOVE_TIME);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        elevator.unloadPassenger(person);
    }
    
    private void updateElevatorState() {
        floorsManager.acquireLock();
        elevator.acquireLock();
        try {
            boolean shouldRedispatch = elevator.containTarget();
            elevator.updateState();
            if (shouldRedispatch) {
                Floor floor = floorsManager.getFloor(elevator.getFloor());
                floor.acquireLock();
                try {
                    floor.updateState();
                    if (floor.isUpPressed()) {
                        floorsManager.putRequest(new Request(floor.floorNumber, Direction.UP));
                    }
                    if (floor.isDownPressed()) {
                        floorsManager.putRequest(new Request(floor.floorNumber, Direction.DOWN));
                    }
                } finally {
                    floor.releaseLock();
                }
            }
        } finally {
            elevator.releaseLock();
            floorsManager.releaseLock();
        }
    }
    
    private void executeMovement() {
        if (elevator.getState() == Elevator.State.UP) {
            elevator.moveUpOneFloor();
            try {
                sleep(ELEVATOR_MOVE_TIME);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Timestamp.println(index + "-ARRIVE-" + floorToString(elevator.getFloor()));
        } else if (elevator.getState() == Elevator.State.DOWN) {
            elevator.moveDownOneFloor();
            try {
                sleep(ELEVATOR_MOVE_TIME);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Timestamp.println(index + "-ARRIVE-" + floorToString(elevator.getFloor()));
        }
    }
    
    private boolean hasSameDirection(Floor floor) {
        if (elevator.getState() == Elevator.State.UP) {
            return floor.hasUpRequest();
        }
        if (elevator.getState() == Elevator.State.DOWN) {
            return floor.hasDownRequest();
        }
        return false;
    }
    
    private String floorToString(int floor) {
        return floor < 0 ? "B" + (-floor) : "F" + (floor + 1);
    }
    
}