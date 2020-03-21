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
@Table(name = "TeamStat")
public class TeamStat {

    private Long id;
    private Integer year;
    private GameSimulation game;
    private Team team;
    private GameStat gameStat;

    public TeamStat(Team team, GameSimulation game, Integer year) {
        this.team = team;
        this.game = game;
        this.year = year;
        this.gameStat = new GameStat(year);
    }

    @Id
    @Column(name = "team_stat_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "game_id")
    public GameSimulation getGame() {
        return game;
    }

    public void setGame(GameSimulation game) {
        this.game = game;
    }

    @OneToOne
    @JoinColumn(name = "team_id")
    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    @OneToOne
    @JoinColumn(name = "game_stat_id")
    public GameStat getGameStat() {
        return gameStat;
    }

    public void setGameStat(GameStat gameStat) {
        this.gameStat = gameStat;
    }

    @Column(name = "Year")
    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
