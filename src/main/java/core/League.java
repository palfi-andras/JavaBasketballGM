package core;

import application.DatabaseConnection;
import utilities.CoreConfiguration;
import utilities.Utils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name = "League")
public class League {

    private static final int MAX_NUM_THREADS = Runtime.getRuntime().availableProcessors() + 1;

    private Long id;
    private String name;
    private Integer currentYear;
    private Double salaryCap;
    private Double minimumSalary;
    private Team userTeam;


    public League() {
    }

    public League(String name) {
        this.name = name;
        this.currentYear = CoreConfiguration.getInstance().getIntProperty("league.start_year");
        this.salaryCap = CoreConfiguration.getInstance().getDoubleProperty("league.salary_cap");
        this.minimumSalary = CoreConfiguration.getInstance().getDoubleProperty("league.minimum_salary");
        bootstrap();
    }


    @Id
    @Column(name = "league_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "Name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "CurrentYear")
    public Integer getCurrentYear() {
        return currentYear;
    }

    public void setCurrentYear(Integer currentYear) {
        this.currentYear = currentYear;
    }

    @Column(name = "SalaryCap")
    public Double getSalaryCap() {
        return salaryCap;
    }

    public void setSalaryCap(Double salaryCap) {
        this.salaryCap = salaryCap;
    }

    @Column(name = "MinimumSalary")
    public Double getMinimumSalary() {
        return minimumSalary;
    }

    public void setMinimumSalary(Double minimumSalary) {
        this.minimumSalary = minimumSalary;
    }

    private void bootstrap() {
        List<Object> leagueEntities = new LinkedList<>();
        int numPlayersPerTeam = CoreConfiguration.getInstance().getIntProperty("league.players_per_team");
        int numTeams = CoreConfiguration.getInstance().getIntProperty("league.num_teams");
        if (numTeams % 2 != 0) numTeams += 1;
        List<String> firstNames = Utils.getFirstRowFromCSVFile(CoreConfiguration.getInstance().
                getStringProperty("file_path.first_names_csv"));
        List<String> lastNames = Utils.getFirstRowFromCSVFile(CoreConfiguration.getInstance().
                getStringProperty("file_path.last_names_csv"));
        List<String> cities = Utils.getFirstRowFromCSVFile(CoreConfiguration.getInstance().
                getStringProperty("file_path.cities_csv"));
        for (int i = 0; i < numTeams; i++)
            if (i % 2 == 0)
                leagueEntities.add(new Team(cities.remove(Utils.getRandomInteger(0, cities.size() - 1)),
                        Conference.EAST));
            else
                leagueEntities.add(new Team(cities.remove(Utils.getRandomInteger(0, cities.size() - 1)),
                        Conference.WEST));

        for (int i = 0; i < (numPlayersPerTeam * numTeams) + 100; i++)
            leagueEntities.add(new Player(String.format("%s %s",
                    firstNames.remove(Utils.getRandomInteger(0, firstNames.size() - 1)),
                    lastNames.remove(Utils.getRandomInteger(0, lastNames.size() - 1))),
                    Utils.getRandomInteger(20, 40)));
        DatabaseConnection.getConnection().createNewEntities(leagueEntities);
    }

}
