package base;

public class Train implements Comparable<Train> {
    public final int id;
    public final int arrivalTime;
    public final int unloadingTime;
    public final int unloadingCost;

    public Train(int id, int arrivalTime, int unloadingTime, int unloadingCost) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.unloadingTime = unloadingTime;
        this.unloadingCost = unloadingCost;
    }

    @Override
    public int compareTo(Train train) {
        return Integer.compare(this.arrivalTime, train.arrivalTime);
    }
}
