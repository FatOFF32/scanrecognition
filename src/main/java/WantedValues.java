public class WantedValues {

    String name;
    DataTypesConversion type;

    public WantedValues(String name, String type) {
        this.name = name;
        this.type = DataTypesConversion.getInstanceByName(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WantedValues that = (WantedValues) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
