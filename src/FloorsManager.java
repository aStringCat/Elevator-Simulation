import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class FloorsManager implements Constant {
    
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition wait = lock.newCondition();
    private final ArrayList<Request> requests;
    private final Map<Integer, Floor> floors;
    private boolean isEnded = false;
    
    public FloorsManager() {
        this.requests = new ArrayList<>();
        floors = new HashMap<>();
        for (int i = BOTTOM_FLOOR; i <= TOP_FLOOR; i++) {
            floors.put(i, new Floor(i));
        }
    }
    
    public void acquireLock() {
        lock.lock();
    }
    
    public void releaseLock() {
        lock.unlock();
    }
    
    public void await() {
        lock.lock();
        try {
            try {
                wait.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } finally {
            lock.unlock();
        }
        
    }
    
    public void signalAll() {
        lock.lock();
        try {
            wait.signalAll();
        } finally {
            lock.unlock();
        }
    }
    
    public void addPassenger(Person person) {
        Floor floor = floors.get(person.from);
        floor.acquireLock();
        try {
            if (person.getDirection() == Direction.UP) {
                if (!floor.isUpPressed()) {
                    putRequest(new Request(floor.floorNumber, Direction.UP));
                }
                floor.addQueueUp(person);
            } else if (person.getDirection() == Direction.DOWN) {
                if (!floor.isDownPressed()) {
                    putRequest(new Request(floor.floorNumber, Direction.DOWN));
                }
                floor.addQueueDown(person);
            }
        } finally {
            floor.releaseLock();
        }
    }
    
    public void putRequest(Request request) {
        lock.lock();
        try {
            requests.add(request);
        } finally {
            wait.signalAll();
            lock.unlock();
        }
    }
    
    public void removeRequest(Request request) {
        lock.lock();
        try {
            requests.remove(request);
        } finally {
            lock.unlock();
        }
    }
    
    public ArrayList<Request> getRequests() {
        lock.lock();
        try {
            return new ArrayList<>(requests);
        } finally {
            lock.unlock();
        }
    }
    
    public boolean isRequestEmpty() {
        lock.lock();
        try {
            return requests.isEmpty();
        } finally {
            lock.unlock();
        }
    }
    
    public Floor getFloor(int floorNumber) {
        return floors.get(floorNumber);
    }
    
    public void setEnded() {
        lock.lock();
        try {
            isEnded = true;
            wait.signalAll();
        } finally {
            lock.unlock();
        }
    }
    
    public boolean isEnded() {
        lock.lock();
        try {
            return isEnded;
        } finally {
            lock.unlock();
        }
    }
    
}