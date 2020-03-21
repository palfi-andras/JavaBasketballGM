package core;


import utilities.Utils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * CS-622
 * Player.java
 * <p>
 * The Player subclass of AbstractEntity represents a basketball player in the League
 *
 * @author Andras Palfi apalfi@bu.edu
 * @version 1.0
 */

@Entity
@Table(name = "Player")
public class Player {
    // Player Info
    private Long id;
    private String name;
    private Integer age;
    private Double salary_amount;
    private Integer salary_length;
    // Player Physical Attributes
    private Double height_inches;
    private Double weight_lbs;
    // Player Game Attributes
    private Double speed;
    private Double strength;
    private Double inside_scoring;
    private Double mid_scoring;
    private Double three_poInteger_scoring;
    private Double dunk;
    private Double free_throw;
    private Double off_reb;
    private Double def_reb;
    private Double inside_defense;
    private Double perimeter_defense;
    private Double assist;
    private Double turnover;


    public Player() {
    }

    public Player(String name, Integer age) {
        this.name = name;
        this.age = age;
        bootstrap();
    }

    @Id
    @Column(name = "player_id")
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

    @Column(name = "Age")
    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Column(name = "SalaryAmount")
    public Double getSalary_amount() {
        return salary_amount;
    }

    public void setSalary_amount(Double salary_amount) {
        this.salary_amount = salary_amount;
    }

    @Column(name = "SalaryLength")
    public Integer getSalary_length() {
        return salary_length;
    }

    public void setSalary_length(Integer salary_length) {
        this.salary_length = salary_length;
    }

    @Column(name = "HeightInches")
    public Double getHeight_inches() {
        return height_inches;
    }

    public void setHeight_inches(Double height_inches) {
        this.height_inches = height_inches;
    }

    @Column(name = "WeightLbs")
    public Double getWeight_lbs() {
        return weight_lbs;
    }

    public void setWeight_lbs(Double weight_lbs) {
        this.weight_lbs = weight_lbs;
    }

    @Column(name = "Speed")
    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    @Column(name = "Strength")
    public Double getStrength() {
        return strength;
    }

    public void setStrength(Double strength) {
        this.strength = strength;
    }

    @Column(name = "InsideScoring")
    public Double getInside_scoring() {
        return inside_scoring;
    }

    public void setInside_scoring(Double inside_scoring) {
        this.inside_scoring = inside_scoring;
    }

    @Column(name = "MidScoring")
    public Double getMid_scoring() {
        return mid_scoring;
    }

    public void setMid_scoring(Double mid_scoring) {
        this.mid_scoring = mid_scoring;
    }

    @Column(name = "ThreePoIntegerScoring")
    public Double getThree_poInteger_scoring() {
        return three_poInteger_scoring;
    }

    public void setThree_poInteger_scoring(Double three_poInteger_scoring) {
        this.three_poInteger_scoring = three_poInteger_scoring;
    }

    @Column(name = "Dunk")
    public Double getDunk() {
        return dunk;
    }

    public void setDunk(Double dunk) {
        this.dunk = dunk;
    }

    @Column(name = "FreeThrow")
    public Double getFree_throw() {
        return free_throw;
    }

    public void setFree_throw(Double free_throw) {
        this.free_throw = free_throw;
    }

    @Column(name = "OffReb")
    public Double getOff_reb() {
        return off_reb;
    }

    public void setOff_reb(Double off_reb) {
        this.off_reb = off_reb;
    }

    @Column(name = "DefReb")

    public Double getDef_reb() {
        return def_reb;
    }

    public void setDef_reb(Double def_reb) {
        this.def_reb = def_reb;
    }

    @Column(name = "InsideDefense")
    public Double getInside_defense() {
        return inside_defense;
    }

    public void setInside_defense(Double inside_defense) {
        this.inside_defense = inside_defense;
    }

    @Column(name = "PerimeterDefense")
    public Double getPerimeter_defense() {
        return perimeter_defense;
    }

    public void setPerimeter_defense(Double perimeter_defense) {
        this.perimeter_defense = perimeter_defense;
    }

    @Column(name = "Assist")
    public Double getAssist() {
        return assist;
    }

    public void setAssist(Double assist) {
        this.assist = assist;
    }

    @Column(name = "Turnover")
    public Double getTurnover() {
        return turnover;
    }

    public void setTurnover(Double turnover) {
        this.turnover = turnover;
    }

    private void bootstrap() {
        setHeight_inches(Utils.getRandomDouble(65, 85));
        setWeight_lbs(Utils.getRandomDouble(175, 300));
        setSpeed(Utils.getRandomDouble());
        setStrength(Utils.getRandomDouble());
        setInside_defense(Utils.getRandomDouble());
        setInside_scoring(Utils.getRandomDouble());
        setMid_scoring(Utils.getRandomDouble());
        setThree_poInteger_scoring(Utils.getRandomDouble());
        setDunk(Utils.getRandomDouble());
        setFree_throw(Utils.getRandomDouble());
        setOff_reb(Utils.getRandomDouble());
        setDef_reb(Utils.getRandomDouble());
        setPerimeter_defense(Utils.getRandomDouble());
        setAssist(Utils.getRandomDouble());
        setTurnover(Utils.getRandomDouble());
    }
}
