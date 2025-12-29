package service;

import model.Transaction;
import model.User;

// Основная логика: добавление операций, проверка бюджетов, переводы
public class FinanceService {
    private final AuthService authService;

    public FinanceService(AuthService authService) {
        this.authService = authService;
    }

    // Добавление дохода или расхода
    public void addTransaction(String cat, double amount, boolean isIncome, String desc) {
        User user = authService.getCurrentUser();

        // Проверка ввода
        if (amount <= 0) {
            System.out.println("Сумма должна быть положительной.");
            return;
        }

        // Если это расход, проверяем, хватит ли денег
        if (!isIncome && user.wallet.balance < amount) {
            System.out.println("Внимание: Расход превышает текущий баланс!");
        }

        // Проверка лимита бюджета (только для расходов)
        if (!isIncome && user.wallet.budgets.containsKey(cat)) {
            double limit = user.wallet.budgets.get(cat);

            // Считаем, сколько уже потратили в этой категории
            double spent = calculateTotalByCategory(user, cat, false);

            // Если новая трата превысит лимит - предупреждаем
            if ((spent + amount) > limit) {
                System.out.println("Внимание: Превышен бюджет по категории '" + cat + "'!");
            }
        }

        // Добавляем операцию в список
        user.wallet.transactions.add(new Transaction(cat, amount, isIncome, desc));

        // Обновляем баланс (+ или -)
        if (isIncome) user.wallet.balance += amount;
        else user.wallet.balance -= amount;

        authService.saveData(); // Сохраняем в файл
    }

    // Метод для подсчета суммы по категории через цикл
    public double calculateTotalByCategory(User user, String cat, boolean isIncome) {
        double total = 0.0;
        for (Transaction t : user.wallet.transactions) {
            // Складываем, только если совпадает категория и тип (доход/расход)
            if (t.isIncome == isIncome && t.category.equalsIgnoreCase(cat)) {
                total += t.amount;
            }
        }
        return total;
    }

    // Установка бюджета
    public void setBudget(String cat, double limit) {
        if (limit < 0) {
            System.out.println("Лимит не может быть отрицательным.");
            return;
        }
        authService.getCurrentUser().wallet.budgets.put(cat, limit);
        authService.saveData();
        System.out.println("Бюджет установлен.");
    }

    // Перевод денег между пользователями
    public void transfer(String toLogin, double amount) {
        User currentUser = authService.getCurrentUser();

        // Проверки
        if (toLogin.equals(currentUser.login)) {
            System.out.println("Нельзя перевести самому себе.");
            return;
        }

        User toUser = authService.findUser(toLogin);
        if (toUser == null) {
            System.out.println("Пользователь не найден.");
            return;
        }

        if (currentUser.wallet.balance < amount) {
            System.out.println("Недостаточно средств.");
            return;
        }

        // Снимаем у отправителя
        addTransaction("Перевод", amount, false, "Перевод " + toLogin);

        // Начисляем получателю (через создание транзакции)
        toUser.wallet.transactions.add(new Transaction("Перевод", amount, true, "От " + currentUser.login));
        toUser.wallet.balance += amount;

        authService.saveData(); // Сохраняем изменения для обоих
        System.out.println("Перевод выполнен.");
    }
}