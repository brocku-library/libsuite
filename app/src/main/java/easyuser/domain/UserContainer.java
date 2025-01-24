package easyuser.domain;

import static org.springframework.util.StringUtils.hasText;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import easyuser.web.validator.UserContainerValidator;

public class UserContainer {

    private String userGroup;
    private String expiryDateStr;
    private List<User> users;

    public UserContainer() {
        users = new ArrayList<>();
    }

    public List<User> getUsers() {
        return users;
    }

    public List<User> getLegitUsers() {
        return users.stream()
                .filter(user -> hasText(user.getEmail()) || hasText(user.getFirstName()) || hasText(user.getLastName()))
                .collect(Collectors.toList());
    }

    public String getUsersJson() {
        var legitUsers = getLegitUsers();

        if (legitUsers.isEmpty()) {
            legitUsers.add(new User());
        }

        return new Gson().toJson(legitUsers);
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public String getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(String userGroup) {
        this.userGroup = userGroup;
    }

    public String getExpiryDateStr() {
        return expiryDateStr;
    }

    public Date getExpiryDate() {
        try {
            DateFormat df = new SimpleDateFormat(UserContainerValidator.DATE_FORMAT);
            df.setLenient(false);
            return df.parse(expiryDateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    public void setExpiryDateStr(String expiryDateStr) {
        this.expiryDateStr = expiryDateStr.trim();
    }
}
