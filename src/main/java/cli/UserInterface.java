package cli;

import model.User;
import service.*;
import java.util.Scanner;

// Наш консольный пользовательский интерфейс)
public class UserInterface {
    private final Scanner scanner = new Scanner(System.in);
    private final AuthService authService;
    private final FinanceService financeService;

    public UserInterface() {
        FileService fs = new FileService();
        this.authService = new AuthService(fs);
        this.financeService = new FinanceService(authService);
    }

    // Главный цикл программы
    public void start() {
        System.out.println("Приложение запущено.");
        while (true) {
            // Если никто не вошел - показываем меню входа
            if (authService.getCurrentUser() == null) {
                showAuthMenu();
            } else {
                // В противном случае показываем меню управления финансами
                showMainMenu();
            }
        }
    }

    private void showAuthMenu() {
        System.out.println("\n1. Войти | 2. Регистрация | 3. Выход");
        System.out.print("Выбор: ");
        String choice = scanner.nextLine();

        if (choice.equals("3")) System.exit(0);

        System.out.print("Логин: "); String login = scanner.nextLine();
        System.out.print("Пароль: "); String pass = scanner.nextLine();

        if (choice.equals("1")) {
            if (authService.login(login, pass) != null) System.out.println("Вход выполнен.");
            else System.out.println("Ошибка: неверный логин или пароль.");
        } else if (choice.equals("2")) {
            if (authService.register(login, pass)) System.out.println("Успешная регистрация.");
            else System.out.println("Ошибка: логин занят.");
        }
    }

    private void showMainMenu() {
        User user = authService.getCurrentUser();
        System.out.printf("\n--- %s | Баланс: %.2f ---\n", user.login, user.wallet.balance);
        System.out.println("1. Доход");
        System.out.println("2. Расход");
        System.out.println("3. Бюджет");
        System.out.println("4. Статистика");
        System.out.println("5. Перевод");
        System.out.println("6. Выход");
        System.out.print("Команда: ");

        String choice = scanner.nextLine();

        switch (choice) {
            case "1": addTx(true); break;
            case "2": addTx(false); break;
            case "3": setBudget(); break;
            case "4": showStats(); break;
            case "5": transfer(); break;
            case "6": authService.logout(); break;
            default: System.out.println("Нет такой команды.");
        }
    }

    // Добавление транзакции
    private void addTx(boolean isIncome) {
        System.out.print("Категория: "); String cat = scanner.nextLine();
        double amt = readDouble("Сумма: ");
        if (amt > 0) financeService.addTransaction(cat, amt, isIncome, "Вручную");
    }

    // Установка бюджета
    private void setBudget() {
        System.out.print("Категория: "); String cat = scanner.nextLine();
        double lim = readDouble("Лимит: ");
        if (lim > 0) financeService.setBudget(cat, lim);
    }

    // Вывод всей статистики
    private void showStats() {
        User u = authService.getCurrentUser();
        double totalInc = 0;
        double totalExp = 0;

        // Считаем общие суммы циклом
        for (model.Transaction t : u.wallet.transactions) {
            if (t.isIncome) totalInc += t.amount;
            else totalExp += t.amount;
        }

        System.out.printf("Доходы: %.2f | Расходы: %.2f\n", totalInc, totalExp);
        System.out.println("Бюджеты:");

        // Проходим по всем установленным бюджетам
        for (String cat : u.wallet.budgets.keySet()) {
            double limit = u.wallet.budgets.get(cat);
            double spent = financeService.calculateTotalByCategory(u, cat, false);
            System.out.printf("- %s: Лимит %.2f, Потрачено %.2f\n", cat, limit, spent);
        }
    }

    private void transfer() {
        System.out.print("Логин получателя: "); String to = scanner.nextLine();
        double amt = readDouble("Сумма: ");
        if (amt > 0) financeService.transfer(to, amt);
    }

    // Метод для безопасного чтения чисел (конструкция try-catch)
    private double readDouble(String msg) {
        System.out.print(msg);
        try {
            return Double.parseDouble(scanner.nextLine());
        } catch (Exception e) {
            System.out.println("Ошибка: Нужно ввести число.");
            return -1;
        }
    }
}