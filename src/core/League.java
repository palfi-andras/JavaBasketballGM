package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class League extends AbstractEntity {

    private static final int NUM_TEAMS = 10;
    private static final int NUM_PLAYERS = (NUM_TEAMS * 15) + 100;


    private static League instance = null;
    private static AtomicInteger idCreator = new AtomicInteger();
    private List<String> firstNames;
    private List<String> lastNames;
    private List<String> cities;
    private Random randomInstance = new Random(System.currentTimeMillis());
    private LinkedList<AbstractEntity> entities = new LinkedList<>();


    private League(int id) {
        super(id);
        initialize();
    }

    private static AtomicInteger getIdCreator() {
        return idCreator;
    }


    public static League getInstance() {
        if (instance == null)
            instance = new League(0);
        return instance;
    }

    private void setFirstNames(List<String> firstNames) {
        this.firstNames = firstNames;
    }

    List<String> getFirstNames() {
        return firstNames;
    }

    private String getRandomFirstName() {
        return getFirstNames().get(getRandomInteger(getFirstNames().size() - 1));
    }

    private void setLastNames(List<String> lastNames) {
        this.lastNames = lastNames;
    }

    private List<String> getLastNames() {
        return lastNames;
    }

    private String getRandomLastName() {
        return getLastNames().get(getRandomInteger(getLastNames().size() - 1));
    }

    private void setCities(List<String> cities) {
        this.cities = cities;
    }

    private List<String> getCities() {
        return cities;
    }

    private String getRandomCity() {
        int i = getRandomInteger(getCities().size() - 1);
        String city = getCities().get(i);
        getCities().remove(i);
        return city;
    }

    private Random getRandomInstance() {
        return randomInstance;
    }

    public LinkedList<AbstractEntity> getEntities() {
        return entities;
    }

    private boolean entityExists(AbstractEntity entity) { return getEntities().contains(entity); }

    private void addEntity(AbstractEntity entity) {
        assert !entityExists(entity);
        getEntities().add(entity);
    }

    private void initialize() {
        // Load the list of names for each entity type from CSVs
        setFirstNames(getFirstRowFromCSVFile(Player.getPathToFirstNameCSV()));
        setLastNames(getFirstRowFromCSVFile(Player.getPathToLastNameCSV()));
        setCities(getFirstRowFromCSVFile(Team.getPathToCitiesCSV()));
        // Create the required amount of teams and players
        for (int i = 0; i <= NUM_TEAMS; i++)
            addEntity(new Team(getNextUniqueKey()));
        for (int i = 0; i <= NUM_PLAYERS; i++)
            addEntity(new Player(getNextUniqueKey()));
        // Set the names of all of the Entity types
        for (AbstractEntity entity : getEntities()) {
            if (entity instanceof Player) {
                ((Player) entity).setEntityName(getRandomFirstName(), getRandomLastName());
            } else if (entity instanceof  Team) {
                entity.setEntityName(getRandomCity());
            } else {
                throw new IllegalStateException("Unrecognized entity type");
            }
        }
        // Finally, initialize all of the attributes for each entity
        for (AbstractEntity entity : getEntities())
            initializeAttributes(entity);
    }

    private void initializeAttributes(AbstractEntity entity) {
        if (entity instanceof Player)
            for (PlayerAttributes attr : PlayerAttributes.values())
                entity.setEntityAttribute(attr.toString(), getRandomDouble());

        if (entity instanceof Team)
            for (TeamAttributes attr : TeamAttributes.values())
                entity.setEntityAttribute(attr.toString(), getRandomDouble());

    }

    private List<String> getFirstRowFromCSVFile(String path) {
        List<String> values = new LinkedList<>();
        try {
            System.out.println(new File("").getAbsolutePath());
            BufferedReader csvReader = new BufferedReader(new FileReader(path));
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(",");
                values.add(data[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return values;
    }



    private int getNextUniqueKey() { return getIdCreator().incrementAndGet(); }
    private int getRandomInteger(int bound) { return getRandomInstance().nextInt(bound);}
    private double getRandomDouble() { return getRandomDouble(0, 1);}
    private double getRandomDouble(int low, int high) { return low + (high - low) * getRandomInstance().nextDouble();}




}
