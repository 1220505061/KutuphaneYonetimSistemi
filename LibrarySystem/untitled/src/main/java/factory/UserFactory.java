package factory;

import model.Librarian;
import model.Member;
import model.User;

public class UserFactory {
    public static User createUser(String role, String tcNo, String username, String password) {
        if (role.equalsIgnoreCase("MEMBER")) {
            return new Member(username, password, tcNo);
        } else if (role.equalsIgnoreCase("ADMIN")) {
            return new Librarian(username, password, tcNo);
        }
        return null;
    }
}