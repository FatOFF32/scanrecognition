package root;

public class Main {
    public static void main(String[] args) {

        if (args == null || args.length == 0)
            throw new IllegalArgumentException("Порт REST сервиса автораспознователя не заполнен!");

        ProcessMonitor PM = new ProcessMonitor(Integer.parseInt(args[0]));

        PM.start();

    }
}
