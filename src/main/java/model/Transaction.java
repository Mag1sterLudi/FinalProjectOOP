package model;
import java.time.LocalDateTime;

// Класс для хранения данных одной операции (доход или расход)
public class Transaction {
    public String category;
    public double amount; // Используем double, так проще считать
    public boolean isIncome; // true - доход, false - расход
    public String description;
    public LocalDateTime date;

    public Transaction() {} // Пустой конструктор для библиотеки Jackson

    public Transaction(String category, double amount, boolean isIncome, String description) {
        this.category = category;
        this.amount = amount;
        this.isIncome = isIncome;
        this.description = description;
        this.date = LocalDateTime.now(); // Дата ставится автоматически
    }
}