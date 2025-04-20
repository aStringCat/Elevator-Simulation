public class Request implements Constant {
    
    int floorNumber;
    Direction direction;
    
    public Request(int floorNumber, Direction direction) {
        this.floorNumber = floorNumber;
        this.direction = direction;
    }
    
}
