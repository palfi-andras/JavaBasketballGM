package core;

public class Player extends AbstractEntity{

    private static final String pathToFirstNameCSV = "./resources/first-names.csv";
    private static final String pathToLastNameCSV = "./resources/last-names.csv";


    Player(int id) {
        super(id);
    }

    static String getPathToFirstNameCSV() {
        return pathToFirstNameCSV;
    }

    static String getPathToLastNameCSV() {
        return pathToLastNameCSV;
    }

    void setEntityName(String firstName, String lastName) {
        this.setEntityName(String.format("%s %s", firstName, lastName));
    }

    @Override
    public String toString() {
        return "Player Name: " + getName() + "\n" + super.toString();
    }
}
