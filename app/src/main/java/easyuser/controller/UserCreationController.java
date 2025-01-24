package easyuser.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import easyuser.domain.User;
import easyuser.domain.UserContainer;
import easyuser.service.UserService;
import easyuser.web.validator.UserContainerValidator;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserCreationController {

    Logger logger = LoggerFactory.getLogger(UserCreationController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserContainerValidator userContainerValidator;

    @InitBinder("userCont")
    public void userContainerBinder(WebDataBinder binder) {
        binder.addValidators(userContainerValidator);
    }

    @GetMapping
    public String createUser(ModelMap modelMap) {
        modelMap.addAttribute("userGroups", userService.fetchUserGroupsJsonStr());
        modelMap.addAttribute("userCont", new UserContainer());

        return "users";
    }

    @PostMapping
    public String saveUser(
            @Valid @ModelAttribute("userCont") UserContainer userContainer,
            BindingResult bindingResult,
            ModelMap modelMap, RedirectAttributes redirectAttributes) throws InterruptedException {

        if (bindingResult.hasErrors()) {
            modelMap.addAttribute("userGroups", userService.fetchUserGroupsJsonStr());
            modelMap.addAttribute("userCont", userContainer);

            return "users";
        }

        List<User> failedUsers = userService.performSanityCheckAndStore(userContainer);
        if (failedUsers.size() != 0) {
            redirectAttributes.addFlashAttribute("failedUsers", failedUsers);
        } else {
            redirectAttributes.addFlashAttribute("successMsg", "All users are stored successfully");
        }

        return "redirect:/user";
    }

    @CacheEvict(value = "almaUserGroupsJson", allEntries = true)
    @Scheduled(fixedDelay = 24, timeUnit = TimeUnit.HOURS)
    public void emptyAlmaUserGroupsJsonStrCache() {
        logger.debug("Cache evicted: almaUserGroupsJson");
    }
}
