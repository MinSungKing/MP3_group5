package za.co.neilson.alarm.group;

import java.io.Serializable;

/**
 * Created by 14 on 5/3/2016.
 */
public class User implements Serializable{
    String id;
    String pw;
    boolean isSuperUser;

    public User(String id, String pw) {
        this.id = id;
        this.pw = pw;
        this.isSuperUser = false;
    }

    public String getId() {
        return id;
    }

    public String getPw() {
        return pw;
    }
}
