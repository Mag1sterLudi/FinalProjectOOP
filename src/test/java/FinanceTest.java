import model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.AuthService;
import service.FileService;
import service.FinanceService;
import java.util.HashMap;
import java.util.Map;

// Специальный класс-балванка, чтобы тесты не создавали реальные файлы на диске
class MockFileService extends FileService {
    @Override
    public void save(Map<String, User> users) {
        // Ничего не делаем (отключаем сохранение в файл для тестов)
    }

    @Override
    public Map<String, User> load() {
        // Возвращаем пустую базу, как будто файл пустой
        return new HashMap<>();
    }
}

public class FinanceTest {
    private FinanceService financeService;
    private AuthService authService;

    // Этот метод запускается перед каждым тестом (сбрасывает состояние)
    @BeforeEach
    void setUp() {
        // Подключаем нашу балванку вместо реального файла
        authService = new AuthService(new MockFileService());

        // Создаем основного тестового пользователя
        authService.register("user1", "pass");
        authService.login("user1", "pass");

        financeService = new FinanceService(authService);
    }

    @Test
    void testAddIncome() {
        // Добавляем зарплату и проверяем, что баланс стал 100
        financeService.addTransaction("Зарплата", 100.0, true, "Тест");
        Assertions.assertEquals(100.0, authService.getCurrentUser().wallet.balance, 0.01);
    }

    @Test
    void testAddExpense() {
        // Сначала начисляем 100, потом тратим 30
        financeService.addTransaction("Доход", 100.0, true, "Начало");
        financeService.addTransaction("Еда", 30.0, false, "Обед");

        // Проверяем остаток (должно быть 70)
        Assertions.assertEquals(70.0, authService.getCurrentUser().wallet.balance, 0.01);
    }

    @Test
    void testBudgetCalc() {
        // Проверяем, правильно ли считается общая сумма по категории
        financeService.addTransaction("Еда", 50.5, false, "1");
        financeService.addTransaction("Еда", 20.0, false, "2");

        double total = financeService.calculateTotalByCategory(authService.getCurrentUser(), "Еда", false);
        Assertions.assertEquals(70.5, total, 0.01);
    }

    @Test
    void testTransfer() {
        // Создаем второго юзера, чтобы было кому переводить
        authService.register("user2", "pass");
        authService.login("user1", "pass"); // Вернулись в user1

        // Пополняем баланс и делаем перевод
        financeService.addTransaction("Депозит", 1000.0, true, "Старт");
        financeService.transfer("user2", 200.0);

        // У первого пользователя должно остаться 800
        Assertions.assertEquals(800.0, authService.getCurrentUser().wallet.balance, 0.01);
    }


    @Test
    void testAddNegativeIncome() {
        // Пытаемся добавить отрицательную сумму (не должно сработать)
        financeService.addTransaction("Зарплата", -500.0, true, "Тест");
        Assertions.assertEquals(0.0, authService.getCurrentUser().wallet.balance, 0.01);
    }

    @Test
    void testOverdraft() {
        // Тратим 50 при балансе 0. Баланс должен уйти в минус (логика позволяет, но предупреждает)
        financeService.addTransaction("Еда", 50.0, false, "Обед");
        Assertions.assertEquals(-50.0, authService.getCurrentUser().wallet.balance, 0.01);
    }

    @Test
    void testSetBudget() {
        // Проверяем, что бюджет сохраняется в мапу
        financeService.setBudget("Еда", 5000.0);
        Assertions.assertEquals(5000.0, authService.getCurrentUser().wallet.budgets.get("Еда"), 0.01);
    }

    @Test
    void testDuplicateRegister() {
        // Пытаемся зарегистрировать user1 снова (должно вернуть false)
        boolean result = authService.register("user1", "newpass");
        Assertions.assertFalse(result);
    }

    @Test
    void testWrongPassword() {
        // Пытаемся войти с неправильным паролем
        User user = authService.login("user1", "wrongpass");
        Assertions.assertNull(user);
    }

    @Test
    void testCorrectLogin() {
        // Вход с правильным паролем
        User user = authService.login("user1", "pass");
        Assertions.assertNotNull(user);
        Assertions.assertEquals("user1", user.login);
    }

    @Test
    void testTransferToSelf() {
        // Пытаемся перевести деньги самому себе
        financeService.addTransaction("ЗП", 1000, true, "1");
        financeService.transfer("user1", 500);
        // Баланс не должен измениться
        Assertions.assertEquals(1000, authService.getCurrentUser().wallet.balance, 0.01);
    }

    @Test
    void testTransferToGhost() {
        // Перевод несуществующему пользователю
        financeService.addTransaction("ЗП", 1000, true, "1");
        financeService.transfer("ghost_user", 500);
        // Деньги не должны списаться
        Assertions.assertEquals(1000, authService.getCurrentUser().wallet.balance, 0.01);
    }

    @Test
    void testTransferTooMuch() {
        // Перевод суммы, которой нет на счете
        authService.register("user2", "pass");
        authService.login("user1", "pass"); // Убеждаемся, что мы user1

        financeService.addTransaction("ЗП", 100, true, "1");
        financeService.transfer("user2", 5000); // Пытаемся перевести 5000, имея 100

        // Баланс должен остаться 100
        Assertions.assertEquals(100, authService.getCurrentUser().wallet.balance, 0.01);
    }

    @Test
    void testCalculateExpense() {
        // Проверяем подсчет именно расходов (isIncome = false)
        financeService.addTransaction("Такси", 100, false, "1");
        financeService.addTransaction("Такси", 50, false, "2");
        double sum = financeService.calculateTotalByCategory(authService.getCurrentUser(), "Такси", false);
        Assertions.assertEquals(150, sum, 0.01);
    }

    @Test
    void testNewUserBalance() {
        // Новый пользователь должен иметь баланс 0
        authService.register("user3", "p");
        authService.login("user3", "p");
        Assertions.assertEquals(0.0, authService.getCurrentUser().wallet.balance, 0.01);
    }
}