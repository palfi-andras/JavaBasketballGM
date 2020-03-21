package core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * MET CS 622
 *
 * @author apalfi
 * @version 1.0
 * <p>
 * The PlayerStat class is an AbstractEntity that stores in its Observable Map the stats that a particular player
 * achieved in some game in the past. This class aligns itself with the player_stats table in the DB
 */
@Entity
@Table(name = "PlayerStat")
public class PlayerStat {

    private Long id;
    private Integer year;
    private GameSimulation game;
    private Player player;
    private GameStat gameStat;

    public PlayerStat(Player player, GameSimulation game, Integer year) {
        this.player = player;
        this.game = game;
        this.year = year;
        this.gameStat = new GameStat(year);
    }

    @Id
    @Column(name = "player_stat_id")
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
    @JoinColumn(name = "player_id")
    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
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