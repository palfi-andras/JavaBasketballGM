public class AbstractEntity implements Entity {
    private String entityName;

    @Override
    public String getName() {
        return entityName;
    }

    @Override
    public void setEntityName(String name) {
        this.entityName = name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
