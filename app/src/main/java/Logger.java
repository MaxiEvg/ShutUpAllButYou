import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    public static void log(String message) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:ms");
        String timestamp = now.format(formatter);
        String fullMessage = timestamp + " - " + message;

        // Вывод в консоль
        System.out.println(fullMessage);

        // Запись в файл
        try (FileWriter fw = new FileWriter("suabu.log", true)) {
            fw.write(fullMessage + "\n");
        } catch (IOException e) {
            System.err.println("Ошибка записи в лог: " + e.getMessage());
        }
    }

    public static Logger getLogger(String name) {
        // Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLogger'");
    }
}