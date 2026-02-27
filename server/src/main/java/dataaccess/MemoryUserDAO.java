package dataaccess;

import model.UserData;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class MemoryUserDAO implements UserDAO {
    private Collection<UserData> db;
    public MemoryUserDAO(){
        db = new HashSet<>();
    }

    @Override
    public void createUser(UserData user) {
        db.add(user);
    }

    @Override
    public UserData getUser(String username) {
        for (UserData user : db) {
            if (Objects.equals(user.username(), username)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public void clear() {
        db.clear();
    }
}
