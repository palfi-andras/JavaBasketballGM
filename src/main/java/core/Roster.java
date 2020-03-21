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
@Table(name = "Roster")
public class Roster {
    private Long id;
    private Integer year;
    private Team team;
    private Player startingPG;
    private Player startingSG;
    private Player startingSF;
    private Player startingPF;
    private Player startingC;
    private Player backup1;
    private Player backup2;
    private Player backup3;
    private Player backup4;
    private Player backup5;
    private Player backup6;
    private Player backup7;
    private Player backup8;
    private Player backup9;
    private Player backup10;

    public Roster() {
    }

    public Roster(Integer year, Team team) {
        this.team = team;
        this.year = year;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "roster_id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "Year")
    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
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
    @JoinColumn(name = "player_id")
    public Player getStartingPG() {
        return startingPG;
    }


    public void setStartingPG(Player startingPG) {
        this.startingPG = startingPG;
    }

    @OneToOne
    @JoinColumn(name = "player_id")
    public Player getStartingSG() {
        return startingSG;
    }

    public void setStartingSG(Player startingSG) {
        this.startingSG = startingSG;
    }

    @OneToOne
    @JoinColumn(name = "player_id")
    public Player getStartingSF() {
        return startingSF;
    }

    public void setStartingSF(Player startingSF) {
        this.startingSF = startingSF;
    }

    @OneToOne
    @JoinColumn(name = "player_id")
    public Player getStartingPF() {
        return startingPF;
    }

    public void setStartingPF(Player startingPF) {
        this.startingPF = startingPF;
    }

    @OneToOne
    @JoinColumn(name = "player_id")
    public Player getStartingC() {
        return startingC;
    }

    public void setStartingC(Player startingC) {
        this.startingC = startingC;
    }

    @OneToOne
    @JoinColumn(name = "player_id")
    public Player getBackup1() {
        return backup1;
    }

    public void setBackup1(Player backup1) {
        this.backup1 = backup1;
    }

    @OneToOne
    @JoinColumn(name = "player_id")
    public Player getBackup2() {
        return backup2;
    }

    public void setBackup2(Player backup2) {
        this.backup2 = backup2;
    }

    @OneToOne
    @JoinColumn(name = "player_id")
    public Player getBackup3() {
        return backup3;
    }

    public void setBackup3(Player backup3) {
        this.backup3 = backup3;
    }

    @OneToOne
    @JoinColumn(name = "player_id")
    public Player getBackup4() {
        return backup4;
    }

    public void setBackup4(Player backup4) {
        this.backup4 = backup4;
    }

    @OneToOne
    @JoinColumn(name = "player_id")
    public Player getBackup5() {
        return backup5;
    }

    public void setBackup5(Player backup5) {
        this.backup5 = backup5;
    }

    @OneToOne
    @JoinColumn(name = "player_id")
    public Player getBackup6() {
        return backup6;
    }

    public void setBackup6(Player backup6) {
        this.backup6 = backup6;
    }

    @OneToOne
    @JoinColumn(name = "player_id")
    public Player getBackup7() {
        return backup7;
    }

    public void setBackup7(Player backup7) {
        this.backup7 = backup7;
    }

    @OneToOne
    @JoinColumn(name = "player_id")
    public Player getBackup8() {
        return backup8;
    }

    public void setBackup8(Player backup8) {
        this.backup8 = backup8;
    }

    @OneToOne
    @JoinColumn(name = "player_id")
    public Player getBackup9() {
        return backup9;
    }

    public void setBackup9(Player backup9) {
        this.backup9 = backup9;
    }

    @OneToOne
    @JoinColumn(name = "player_id")
    public Player getBackup10() {
        return backup10;
    }

    public void setBackup10(Player backup10) {
        this.backup10 = backup10;
    }
}
