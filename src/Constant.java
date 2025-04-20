public interface Constant {
    
    enum Direction {
        UP, DOWN
    }
    
    enum State {
        IDLE, UP, DOWN
    }
    
    int ELEVATOR_CAPACITY = 13;
    
    int ELEVATOR_NUMBER = 2;
    
    int TOP_FLOOR = 10;
    
    int BOTTOM_FLOOR = -2;
    
    int ELEVATOR_DOOR_TIME = 1000;
    
    int PASSENGER_MOVE_TIME = 500;
    
    int ELEVATOR_MOVE_TIME = 5000;
    
}
