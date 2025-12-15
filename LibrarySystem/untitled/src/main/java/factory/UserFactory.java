package factory;

import model.Librarian;
import model.Member;
import model.User;

public class UserFactory {
    public static User createUser(String role, String tcNo, String username, String password) {
        if (role == null) {
            return null;
        }

        if (role.equalsIgnoreCase("MEMBER")) {
            return new Member(tcNo, username, password);
        }
        else if (role.equalsIgnoreCase("ADMIN")) {
            return new Librarian(tcNo, username, password);
        }

        return null;
    }
}