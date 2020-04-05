package base;

public class Train implements Comparable<Train> {
    final int id;
    final int arrivalTime;
    final int unloadingTime;
    final int unloadingCost;

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
