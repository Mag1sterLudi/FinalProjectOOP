package model;
import java.util.*;

// Привязанный к пользователю кошелек
public class Wallet {
    public double balance = 0.0; // Текущий баланс

    // Список операций
    public List<Transaction> transactions = new ArrayList<>();

    // Храним лимиты по категориям: "Еда" - 5000.0
    public Map<String, Double> budgets = new HashMap<>();

    public Wallet() {}
}