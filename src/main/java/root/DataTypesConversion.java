package root;

public enum DataTypesConversion {
    STRING,
    DATE,
    DECIMAL;

    public static DataTypesConversion getInstanceByName(String name) {

        DataTypesConversion type = null;

        switch (name){
            case "Строка":
                type = STRING;
                break;
            case "Число":
                type = DECIMAL;
                break;
            case "Дата":
                type = DATE;
                break;
        }
        return type;
    }


}
