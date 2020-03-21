package core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "GameStat")
public class GameStat {

    private Long id;
    private Integer year;
    private Integer points;
    private Integer twoPointMade;
    private Integer twoPointAttempted;
    private Integer threePointMade;
    private Integer threePointAttempted;
    private Integer assists;
    private Integer steals;
    private Integer blocks;
    private Integer offensiveRebounds;
    private Integer defensiveRebounds;
    private Integer freeThrowMade;
    private Integer freeThrowAttempted;
    private Integer turnovers;
    private Integer fouls;

    public GameStat(Integer year) {
        setYear(year);
        setPoints(0);
        setTwoPointMade(0);
        setTwoPointAttempted(0);
        setThreePointMade(0);
        setThreePointAttempted(0);
        setAssists(0);
        setSteals(0);
        setBlocks(0);
        setOffensiveRebounds(0);
        setDefensiveRebounds(0);
        setFreeThrowAttempted(0);
        setFreeThrowMade(0);
        setTurnovers(0);
        setFouls(0);
    }

    @Id
    @Column(name = "game_stat_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "Points")
    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    @Column(name = "TwoPointMade")
    public Integer getTwoPointMade() {
        return twoPointMade;
    }

    public void setTwoPointMade(Integer twoPointMade) {
        this.twoPointMade = twoPointMade;
    }

    @Column(name = "TwoPointAttempted")
    public Integer getTwoPointAttempted() {
        return twoPointAttempted;
    }

    public void setTwoPointAttempted(Integer twoPointAttempted) {
        this.twoPointAttempted = twoPointAttempted;
    }

    @Column(name = "ThreePointMade")
    public Integer getThreePointMade() {
        return threePointMade;
    }

    public void setThreePointMade(Integer threePointMade) {
        this.threePointMade = threePointMade;
    }

    @Column(name = "ThreePointAttempted")
    public Integer getThreePointAttempted() {
        return threePointAttempted;
    }

    public void setThreePointAttempted(Integer threePointAttempted) {
        this.threePointAttempted = threePointAttempted;
    }

    @Column(name = "Assists")
    public Integer getAssists() {
        return assists;
    }

    public void setAssists(Integer assists) {
        this.assists = assists;
    }

    @Column(name = "Steals")
    public Integer getSteals() {
        return steals;
    }

    public void setSteals(Integer steals) {
        this.steals = steals;
    }

    @Column(name = "Blocks")
    public Integer getBlocks() {
        return blocks;
    }

    public void setBlocks(Integer blocks) {
        this.blocks = blocks;
    }

    @Column(name = "OffensiveRebounds")
    public Integer getOffensiveRebounds() {
        return offensiveRebounds;
    }

    public void setOffensiveRebounds(Integer offensiveRebounds) {
        this.offensiveRebounds = offensiveRebounds;
    }

    @Column(name = "DefensiveRebounds")
    public Integer getDefensiveRebounds() {
        return defensiveRebounds;
    }

    public void setDefensiveRebounds(Integer defensiveRebounds) {
        this.defensiveRebounds = defensiveRebounds;
    }

    @Column(name = "FreeThrowMade")
    public Integer getFreeThrowMade() {
        return freeThrowMade;
    }

    public void setFreeThrowMade(Integer freeThrowMade) {
        this.freeThrowMade = freeThrowMade;
    }

    @Column(name = "FreeThrowAttempted")
    public Integer getFreeThrowAttempted() {
        return freeThrowAttempted;
    }

    public void setFreeThrowAttempted(Integer freeThrowAttempted) {
        this.freeThrowAttempted = freeThrowAttempted;
    }

    @Column(name = "Turnovers")
    public Integer getTurnovers() {
        return turnovers;
    }

    public void setTurnovers(Integer turnovers) {
        this.turnovers = turnovers;
    }

    @Column(name = "Fouls")
    public Integer getFouls() {
        return fouls;
    }

    public void setFouls(Integer fouls) {
        this.fouls = fouls;
    }

    @Column(name = "Year")
    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
