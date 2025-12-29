package model;

// Пользователь
public class User {
    public String login;
    public String password;
    public Wallet wallet; // У каждого юзера свой кошелек

    public User() {}

    public User(String login, String password) {
        this.login = login;
        this.password = password;
        this.wallet = new Wallet(); // При регистрации создаем пустой кошелек
    }
}