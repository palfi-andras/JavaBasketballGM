package core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Team")
public class Team {

    private Long id;
    private String name;
    private Double capSpace;
    private Integer conference;
    private Integer wins;
    private Integer losses;
    private Roster roster;


    public Team() {
    }

    public Team(String name, Conference conference) {
        this.name = name;
        this.conference = conference.ordinal();
        // Todo: Change this to take the year from the league table
        this.roster = new Roster(2019, this);
        this.wins = 0;
        this.losses = 0;

    }

    @Id
    @Column(name = "team_id")
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

    @Column(name = "CapSpace")
    public Double getCapSpace() {
        return capSpace;
    }

    public void setCapSpace(Double capSpace) {
        this.capSpace = capSpace;
    }

    @Column(name = "Conference")
    public Integer getConference() {
        return conference;
    }

    public void setConference(Integer conference) {
        this.conference = conference;
    }

    @Column(name = "Wins")
    public Integer getWins() {
        return wins;
    }

    public void setWins(Integer wins) {
        this.wins = wins;
    }

    @Column(name = "Losses")
    public Integer getLosses() {
        return losses;
    }

    public void setLosses(Integer losses) {
        this.losses = losses;
    }

    @OneToOne
    @JoinColumn(name = "roster_id")
    public Roster getRoster() {
        return roster;
    }

    public void setRoster(Roster roster) {
        this.roster = roster;
    }
}
