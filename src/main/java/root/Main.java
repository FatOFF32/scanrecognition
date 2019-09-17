package root;

public class Main {
    public static void main(String[] args) {

        if (args == null || args.length == 0)
            throw new IllegalArgumentException("Порт REST сервиса автораспознователя не заполнен!");

//        ProcessMonitor processMonitor = new ProcessMonitor(Integer.parseInt(args[0])); // todo delete
        ProcessMonitor processMonitor = ProcessMonitor.getInstance();
        processMonitor.setRestPort(Integer.parseInt(args[0])); // todo обработать ситуацию когда пытаемся стартовать без рест порта, и что будет когда меняем рест порт уже в процессе работы

        processMonitor.start();

    }
}
