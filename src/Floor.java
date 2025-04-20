import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

public class Floor implements Constant {
    
    public final int floorNumber;
    private boolean upPressed;
    private boolean downPressed;
    private final Queue<Person> upQueue = new LinkedList<>();
    private final Queue<Person> downQueue = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock();
    
    public Floor(int floorNumber) {
        this.floorNumber = floorNumber;
    }
    
    public void acquireLock() {
        lock.lock();
    }
    
    public void releaseLock() {
        lock.unlock();
    }
    
    public void addQueueUp(Person person) {
        lock.lock();
        try {
            upPressed = true;
            upQueue.add(person);
        } finally {
            lock.unlock();
        }
    }
    
    public void addQueueDown(Person person) {
        lock.lock();
        try {
            downPressed = true;
            downQueue.add(person);
        } finally {
            lock.unlock();
        }
    }
    
    public Person removePassenger(State state) {
        lock.lock();
        try {
            if (state == State.UP) {
                return upQueue.poll();
            } else if (state == State.DOWN) {
                return downQueue.poll();
            }
            return null;
        } finally {
            lock.unlock();
        }
    }
    
    public boolean isUpPressed() {
        lock.lock();
        try {
            return upPressed;
        } finally {
            lock.unlock();
        }
    }
    
    public boolean isDownPressed() {
        lock.lock();
        try {
            return downPressed;
        } finally {
            lock.unlock();
        }
    }
    
    public void receiveElevator(Constant.State state) {
        lock.lock();
        try {
            if (state == Constant.State.UP) {
                upPressed = false;
            } else if (state == Constant.State.DOWN) {
                downPressed = false;
            }
        } finally {
            lock.unlock();
        }
    }
    
    public void updateState() {
        lock.lock();
        try {
            if (!upQueue.isEmpty()) {
                upPressed = true;
            }
            if (!downQueue.isEmpty()) {
                downPressed = true;
            }
        } finally {
            lock.unlock();
        }
    }
    
    public boolean hasUpRequest() {
        lock.lock();
        try {
            return !upQueue.isEmpty();
        } finally {
            lock.unlock();
        }
    }
    
    public boolean hasDownRequest() {
        lock.lock();
        try {
            return !downQueue.isEmpty();
        } finally {
            lock.unlock();
        }
    }
    
}