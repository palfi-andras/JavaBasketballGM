package core;

public class Team extends AbstractEntity {
    private static final String pathToCitiesCSV = "./resources/us-cities.csv";

    Team(int id) {
        super(id);
    }

    static String getPathToCitiesCSV() {
        return pathToCitiesCSV;
    }

    @Override
    public String toString() {
        return "Team Location: " + getName() + "\n" + super.toString();
    }
}
