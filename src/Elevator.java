import java.util.HashSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class Elevator implements Constant {
    
    public int index;
    
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition empty = lock.newCondition();
    private final HashSet<Integer> targetFloors;
    private final HashSet<Person> passengers;
    
    private int currentFloor;
    private State state;
    private boolean isEnded;
    
    public Elevator(int id) {
        this.index = id;
        this.currentFloor = 0;
        this.state = State.IDLE;
        this.passengers = new HashSet<>();
        this.targetFloors = new HashSet<>();
        this.isEnded = false;
    }
    
    public void acquireLock() {
        lock.lock();
    }
    
    public void releaseLock() {
        lock.unlock();
    }
    
    private void withLock(Runnable action) {
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }
    
    private <T> T getWithLock(Supplier<T> supplier) {
        lock.lock();
        try {
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }
    
    public Integer getCost(Request request) {
        return getWithLock(() -> {
            if (state == State.IDLE) {
                return Math.abs(this.currentFloor - request.floorNumber);
            } else if (state == State.UP && request.floorNumber >= this.currentFloor) {
                return Math.abs(this.currentFloor - request.floorNumber);
            } else if (state == State.DOWN && request.floorNumber <= this.currentFloor) {
                return Math.abs(this.currentFloor - request.floorNumber);
            }
            return Integer.MAX_VALUE;
        });
    }
    
    public void addTarget(Request request) {
        withLock(() -> {
            this.targetFloors.add(request.floorNumber);
            if (this.state == State.IDLE) {
                if (this.currentFloor > request.floorNumber) {
                    this.state = State.DOWN;
                } else if (this.currentFloor < request.floorNumber) {
                    this.state = State.UP;
                } else {
                    this.state = request.direction == Direction.UP ? State.UP : State.DOWN;
                }
            }
            empty.signalAll();
        });
    }
    
    public int getFloor() {
        return getWithLock(() -> this.currentFloor);
    }
    
    public State getState() {
        return getWithLock(() -> this.state);
    }
    
    public int availableSpace() {
        return getWithLock(() -> ELEVATOR_CAPACITY - this.targetFloors.size());
    }
    
    public HashSet<Person> getPassengers() {
        return getWithLock(() -> this.passengers);
    }
    
    public void loadPassenger(Person person) {
        withLock(() -> {
            this.passengers.add(person);
            this.targetFloors.add(person.to);
        });
    }
    
    public void unloadPassenger(Person person) {
        withLock(() -> this.passengers.remove(person));
    }
    
    public void moveUpOneFloor() {
        withLock(() -> this.currentFloor++);
    }
    
    public void moveDownOneFloor() {
        withLock(() -> this.currentFloor--);
    }
    
    public boolean containTarget() {
        return getWithLock(() -> this.targetFloors.contains(this.currentFloor));
    }
    
    public void updateState() {
        withLock(() -> {
            this.targetFloors.remove(currentFloor);
            if (this.state == State.UP && IntStream.rangeClosed(currentFloor, TOP_FLOOR).noneMatch(this.targetFloors::contains)) {
                this.state = State.IDLE;
            } else if (this.state == State.DOWN && IntStream.rangeClosed(BOTTOM_FLOOR, currentFloor).noneMatch(this.targetFloors::contains)) {
                this.state = State.IDLE;
            }
        });
    }
    
    public boolean isEmpty() {
        return getWithLock(() -> {
            if (this.state == State.IDLE && !isEnded) {
                try {
                    empty.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return this.state == State.IDLE;
        });
    }
    
    public void setEnd() {
        lock.lock();
        try {
            isEnded = true;
        } finally {
            empty.signalAll();
            lock.unlock();
        }
    }
    
    public boolean isEnded() {
        return getWithLock(() -> isEnded);
    }
    
}