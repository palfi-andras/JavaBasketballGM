package gameplay;

import core.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CS-622
 * StatContainer.java
 * <p>
 * The StatContainer class is tasked with holding stats for any Entity in the simulation. It is essentially a data structure
 * that holds a Map of Stats, and a list for each stat type which has the values that the entity achieved in that particular stat.
 * <p>
 * The strcuture grows with each game, meaning that stat values are recorded historically so that an Entities performance
 * may be gauged over time.
 * <p>
 * What makes this class unique is that it keeps "references" of the average and max value encountered for every stat
 * in real-time. The reason for this is that the GameSimulation will rely on finding average and max values for a variety
 * of stats. In order to avoid expensive computations during the game simulation, the StatContainer class will simply
 * update the average and max values for any arbitrary stat when it is added. Here is the process:
 * <p>
 * 1. A new stat value is updated using the updateStat() function.
 * 2. The new value for the stat is added into the appropriate list in the baseContainer.
 * 3. The Average value for that stat is re-computed by taking the past average and summing it with the new addition and dividing by 2.
 * 4. If the new addition is higher than any previously encountered value for that stat, the maxContainer is updated accordingly.
 *
 * @param <K> What type of stats this container will hold. Usually this will be PlayerStat or TeamStat, but it may also be Strings
 * @param <E> What Number type to represent the stats in. Will most likely always be in double or integer.
 * @author Andras Palfi apalfi@bu.edu
 * @version 1.0
 */
public class StatContainer<K, E extends Number & Comparable<E>> implements Serializable {

    // The base container keeps a list of all instances of a stat that has been encountered
    private Map<K, List<E>> baseContainer;
    // THe average container stores the average value of each stat type that has been added to the base container
    private Map<K, E> avgContainer;
    // The max container stores the max values of each stat type that has been added to the base container
    private Map<K, E> maxContainer;

    public StatContainer() {
        initializeContainers();
    }

    public StatContainer(K stat, E firstValue) {
        initializeContainers();
        updateStat(stat, firstValue);
    }

    /**
     * Initialize each container to an emty hash map
     */
    private void initializeContainers() {
        setBaseContainer(new HashMap<>());
        setAvgContainer(new HashMap<>());
        setMaxContainer(new HashMap<>());
    }

    /**
     * Return the base container
     *
     * @return Map<K, List < E>>
     */
    public Map<K, List<E>> getBaseContainer() {
        return baseContainer;
    }

    /**
     * Set the base container
     *
     * @param baseContainer Map<K, List<E>>
     */
    public void setBaseContainer(Map<K, List<E>> baseContainer) {
        this.baseContainer = baseContainer;
    }

    /**
     * Returns the nth instance of stat K
     */
    public E getNthInstanceOfStat(K stat, int n) {
        assert statExists(stat);
        assert getAllValuesOfStat(stat).size() >= n;
        return getAllValuesOfStat(stat).get(n);
    }

    /**
     * Returns a sum of each of the elements stored in the base container for a particular stat of type K
     *
     * @param stat K
     * @return E
     */
    public E getSumOfStatContainer(K stat) {
        if (!statExists(stat) || getBaseContainer().get(stat).size() == 0) {
            return (E) (Integer) 0;
        }
        if (getBaseContainer().get(stat).get(0) instanceof Integer) {
            Integer sum = 0;
            for (E val : getBaseContainer().get(stat))
                sum = (Integer) sum(val, (E) sum);
            return (E) sum;
        } else if (getBaseContainer().get(stat).get(0) instanceof Double) {
            Double sum = 0.0;
            for (E val : getBaseContainer().get(stat))
                sum = (Double) sum(val, (E) sum);
            return (E) sum;
        } else {
            // So far we only use double or ints. Longs and floats could also be implemented
            throw new RuntimeException();
        }
    }

    /**
     * Returns the set of all stat names used in this container.
     *
     * @return Set<K>
     */
    private Set<K> getAllStatKeys() {
        return getBaseContainer().keySet();
    }

    /**
     * Returns the average container
     *
     * @return Map<K, E>
     */
    public Map<K, E> getAvgContainer() {
        return avgContainer;
    }

    /**
     * Set the average container
     *
     * @param avgContainer Map<K, E>
     */
    public void setAvgContainer(Map<K, E> avgContainer) {
        this.avgContainer = avgContainer;
    }

    /**
     * Return the average value of a stat. If the stat has no historical values yet, we return 0.
     *
     * @param stat K
     * @return E
     */
    public E getAvgValueOfStat(K stat) {
        if (!statExists(stat) || statIsEmpty(stat))
            return (E) (Integer) 0;
        E avg = getAvgContainer().get(stat);
        return (avg instanceof Double) ?
                (E) (Double) Utils.round((Double) avg, 2) : avg;
    }

    /**
     * Returns the max container
     *
     * @return Map<K, E>
     */
    public Map<K, E> getMaxContainer() {
        return maxContainer;
    }

    /**
     * Set the max container
     *
     * @param maxContainer Map<K, E>
     */
    public void setMaxContainer(Map<K, E> maxContainer) {
        this.maxContainer = maxContainer;
    }

    /**
     * Returns the highest value ever encountered for a stat
     *
     * @param stat K
     * @return E: max value
     */
    E getHighestValueOfStat(K stat) {
        assert statExists(stat);
        return getMaxContainer().get(stat);
    }

    /**
     * Returns a list of all historical values of a particular stat
     *
     * @param stat K
     * @return List<E>
     */
    List<E> getAllValuesOfStat(K stat) {
        assert statExists(stat);
        return getBaseContainer().get(stat);
    }

    /**
     * Returns true if a stat exists in this container
     *
     * @param stat K
     * @return boolean
     */
    boolean statExists(K stat) {
        return getBaseContainer().containsKey(stat);
    }

    /**
     * Returns true if a stat has no values yet in this container
     *
     * @param stat K
     * @return boolean
     */
    boolean statIsEmpty(K stat) {
        assert statExists(stat);
        return getAllValuesOfStat(stat).size() == 0;
    }

    /**
     * This is the outward facing function that should be used to add stats to the container from other classes
     * and packages.
     * <p>
     * It will add the newAddition to the baseContainer, check if it is higher than the current value of K in the
     * max container, and then recompute the average value for this stat
     *
     * @param stat        K
     * @param newAddition E
     */
    public void updateStat(K stat, E newAddition) {
        if (!statExists(stat)) {
            initializeStat(stat, newAddition);
        } else {
            getAvgContainer().put(stat, average(getAvgValueOfStat(stat), newAddition));
            getBaseContainer().get(stat).add(newAddition);
            if (newAddition.compareTo(getMaxContainer().get(stat)) > 0)
                getMaxContainer().put(stat, newAddition);
        }
    }

    /**
     * Initializes a new stat for addition to this container.
     *
     * @param stat K
     * @param val  E
     */
    void initializeStat(K stat, E val) {
        assert !statExists(stat);
        getBaseContainer().put(stat, new ArrayList<E>());
        getBaseContainer().get(stat).add(val);
        getAvgContainer().put(stat, val);
        getMaxContainer().put(stat, val);
    }

    /**
     * Provides casts to various sum functions depending on the type of E
     *
     * @param e1 E
     * @param e2 E
     * @return E
     */
    E sum(E e1, E e2) {
        if (e1 instanceof Integer)
            return (E) (Integer) (e1.intValue() + e2.intValue());
        if (e1 instanceof Double)
            return (E) (Double) (e1.doubleValue() + e2.doubleValue());
        return null;
    }

    /**
     * Provides casts to various divide functions depending on the type of E
     *
     * @param e1 E
     * @param e2 E
     * @return E
     */
    E divide(E e1, E e2) {
        if (e1 instanceof Integer)
            return (E) (Integer) (e1.intValue() / e2.intValue());
        if (e1 instanceof Double)
            return (E) (Double) (e1.doubleValue() / e2.doubleValue());
        return null;
    }

    /**
     * averages two values of E together
     *
     * @param e1 E
     * @param e2 E
     * @return E
     */
    E average(E e1, E e2) {
        return divide(sum(e1, e2), (E) (Double) 2.0);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Stat History: \n");
        for (K stat : getAllStatKeys()) {
            builder.append(stat).append("   Best: ").
                    append(getHighestValueOfStat(stat)).
                    append("   Average Value: ").
                    append(getAvgValueOfStat(stat)).
                    append("   Historical Values: ");
            for (E val : getAllValuesOfStat(stat))
                builder.append(val).append(", ");
            builder.replace(builder.length() - 2, builder.length() - 1, "");
            builder.append("\n");
        }
        return builder.toString();
    }
}
