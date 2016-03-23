package net.team88.dotor.account;

/**
 * Created by Eun Leem on 2/17/2016.
 */
public class LoginRequest {
    public LoginRequest(Account account) {
        username = account.getUsername();
        password = account.getPassword();
    }
    String username;
    String password;
}
