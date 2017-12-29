public enum DataTypesConversion {
    STRING,
    DATA,
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
                type = DATA;
                break;
        }
        return type;
    }


}
