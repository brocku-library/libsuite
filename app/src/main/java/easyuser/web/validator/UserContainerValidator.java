package easyuser.web.validator;

import static org.springframework.util.StringUtils.hasText;

import java.util.Date;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import easyuser.domain.UserContainer;
import easyuser.service.UserService;

@Component
public class UserContainerValidator implements Validator {

    public static final String DATE_FORMAT = "dd/MM/yyyy";

    @Autowired
    private UserService userService;

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return UserContainer.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        UserContainer userContainer = (UserContainer) target;

        if (!userService.fetchUserGroupsJsonStr().contains("\"" + userContainer.getUserGroup() + "\"")) {
            errors.rejectValue("userGroup", "user.group.invalid");
        }

        if (Objects.isNull(userContainer.getExpiryDate()) || !userContainer.getExpiryDate().after(new Date())) {
            errors.rejectValue("expiryDateStr", "user.expiry.invalid");
        }

        boolean isValidUserInput = userContainer.getUsers()
                .stream()
                .filter(user -> hasText(user.getEmail()) || hasText(user.getFirstName()) || hasText(user.getLastName()))
                .allMatch(user -> hasText(user.getEmail()) && hasText(user.getFirstName())
                        && hasText(user.getLastName()));

        if (!isValidUserInput) {
            errors.rejectValue("users", "user.invalid");
        }
    }
}
