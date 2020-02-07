package gameplay;

import core.Utils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StatContainer<K, E extends Number & Comparable<E>> {

    private Map<K, List<E>> baseContainer;
    private Map<K, E> avgContainer;
    private Map<K, E> maxContainer;

    StatContainer() {
        initializeContainers();
    }

    StatContainer(K stat, E firstValue) {
        initializeContainers();
        updateStat(stat, firstValue);
    }

    private void initializeContainers() {
        setBaseContainer(new HashMap<>());
        setAvgContainer(new HashMap<>());
        setMaxContainer(new HashMap<>());
    }

    public Map<K, List<E>> getBaseContainer() {
        return baseContainer;
    }

    public void setBaseContainer(Map<K, List<E>> baseContainer) {
        this.baseContainer = baseContainer;
    }

    E getSumOfStatContainer(K stat) {
        assert statExists(stat);
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
            throw new NotImplementedException();
        }
    }

    private Set<K> getAllStatKeys() {
        return getBaseContainer().keySet();
    }

    public Map<K, E> getAvgContainer() {
        return avgContainer;
    }

    public void setAvgContainer(Map<K, E> avgContainer) {
        this.avgContainer = avgContainer;
    }

    E getAvgValueOfStat(K stat) {
        assert statExists(stat);
        E avg = getAvgContainer().get(stat);
        return (avg instanceof Double) ?
                (E) (Double) Utils.round((Double) avg, 2) : avg;
    }

    public Map<K, E> getMaxContainer() {
        return maxContainer;
    }

    public void setMaxContainer(Map<K, E> maxContainer) {
        this.maxContainer = maxContainer;
    }

    E getHighestValueOfStat(K stat) {
        assert statExists(stat);
        return getMaxContainer().get(stat);
    }

    List<E> getAllValuesOfStat(K stat) {
        assert statExists(stat);
        return getBaseContainer().get(stat);
    }

    boolean statExists(K stat) {
        return getBaseContainer().containsKey(stat);
    }

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

    void initializeStat(K stat, E val) {
        assert !statExists(stat);
        getBaseContainer().put(stat, new ArrayList<E>());
        getBaseContainer().get(stat).add(val);
        getAvgContainer().put(stat, val);
        getMaxContainer().put(stat, val);
    }

    E sum(E e1, E e2) {
        if (e1 instanceof Integer)
            return (E) (Integer) (e1.intValue() + e2.intValue());
        if (e1 instanceof Double)
            return (E) (Double) (e1.doubleValue() + e2.doubleValue());
        return null;
    }

    E divide(E e1, E e2) {
        if (e1 instanceof Integer)
            return (E) (Integer) (e1.intValue() / e2.intValue());
        if (e1 instanceof Double)
            return (E) (Double) (e1.doubleValue() / e2.doubleValue());
        return null;
    }

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
