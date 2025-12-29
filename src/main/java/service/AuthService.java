package service;

import model.User;
import java.util.Map;

// Вход и регистрация
public class AuthService {
    private final FileService fileService;
    private final Map<String, User> users; // Кэш пользователей в памяти
    private User currentUser; // Кто сейчас онлайн

    public AuthService(FileService fileService) {
        this.fileService = fileService;
        this.users = fileService.load(); // При старте грузим всех из файла
    }

    // Регистрация нового пользователя
    public boolean register(String login, String pass) {
        if (users.containsKey(login)) return false; // Логин занят

        users.put(login, new User(login, pass));
        fileService.save(users); // Сразу сохраняем изменения
        return true;
    }

    // Проверка логина и пароля
    public User login(String login, String pass) {
        User user = users.get(login);
        if (user != null && user.password.equals(pass)) {
            this.currentUser = user;
            return user;
        }
        return null;
    }

    public User getCurrentUser() { return currentUser; }

    public void logout() { currentUser = null; }

    public User findUser(String login) { return users.get(login); }

    public void saveData() { fileService.save(users); }
}