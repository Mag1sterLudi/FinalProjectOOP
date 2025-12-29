package service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import model.User;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

// Отвечает за сохранение данных в файл JSON и загрузку обратно
public class FileService {
    // Подключаем Jackson для работы с JSON и датами
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final String FILE_PATH = "users_data.json";

    // Сохранение Map пользователей в файл
    public void save(Map<String, User> users) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), users);
        } catch (Exception e) {
            System.out.println("Не удалось сохранить файл: " + e.getMessage());
        }
    }

    // Загрузка из файла. Если файла нет - возвращаем пустую карту.
    public Map<String, User> load() {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) return new HashMap<>();

            // Читаем структуру Map<String, User>
            return mapper.readValue(file, new TypeReference<Map<String, User>>() {});
        } catch (Exception e) {
            return new HashMap<>(); // Если ошибка чтения, вернем пустую базу
        }
    }
}