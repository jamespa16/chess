package dataaccess;

import model.UserData;

public interface UserDAO {
    public void createUser();
    public UserData getUser(String username);
    public void clear();
}
