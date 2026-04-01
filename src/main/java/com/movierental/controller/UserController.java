package com.movierental.controller;

import com.movierental.model.User;
import com.movierental.service.UserProfileImageStorageService;
import com.movierental.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserProfileImageStorageService userProfileImageStorageService;

    public UserController(UserService userService,
                          UserProfileImageStorageService userProfileImageStorageService) {
        this.userService = userService;
        this.userProfileImageStorageService = userProfileImageStorageService;
    }

    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser != null) {
            return redirectToLandingPage(loggedInUser);
        }
        return "user/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        User user = userService.authenticate(username, password);
        if (user == null) {
            model.addAttribute("error", "Invalid username or password.");
            return "user/login";
        }

        session.setAttribute("loggedInUser", user);
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/movies";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Logged out successfully.");
        return "redirect:/users/login";
    }

    @GetMapping("/register")
    public String showRegisterPage(HttpSession session, Model model) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser != null) {
            return redirectToLandingPage(loggedInUser);
        }

        model.addAttribute("user", new User());
        return "user/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user,
                           @RequestParam String confirmPassword,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (!safeEquals(user.getPassword(), confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("user", user);
            return "user/register";
        }

        String result = userService.registerUser(user);
        if (!"success".equals(result)) {
            model.addAttribute("error", result);
            model.addAttribute("user", user);
            return "user/register";
        }

        redirectAttributes.addFlashAttribute("success", "Registration successful. Please sign in.");
        return "redirect:/users/login";
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        model.addAttribute("user", userService.getUserById(loggedInUser.getId()));
        return "user/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String username,
                                @RequestParam String fullName,
                                @RequestParam String email,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        String result = userService.updateProfile(loggedInUser.getId(), username, fullName, email);
        if ("success".equals(result)) {
            session.setAttribute("loggedInUser", userService.getUserById(loggedInUser.getId()));
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }

        return "redirect:/users/profile";
    }

    @PostMapping("/profile/password")
    public String updatePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        String result = userService.changePassword(loggedInUser.getId(), currentPassword, newPassword, confirmPassword);
        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Password updated successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }

        return "redirect:/users/profile";
    }

    @PostMapping("/profile/image")
    public String updateProfileImage(@RequestParam("profileImage") MultipartFile profileImage,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        User existingUser = userService.getUserById(loggedInUser.getId());
        if (existingUser == null) {
            return "redirect:/users/login";
        }

        try {
            String imagePath = userProfileImageStorageService.storeProfileImage(
                    profileImage,
                    existingUser.getUsername(),
                    existingUser.getImagePath()
            );

            String result = userService.updateProfileImage(existingUser.getId(), imagePath);
            if ("success".equals(result)) {
                session.setAttribute("loggedInUser", userService.getUserById(existingUser.getId()));
                redirectAttributes.addFlashAttribute("success", "Profile image updated successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", result);
            }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }

        return "redirect:/users/profile";
    }

    @GetMapping("/search")
    public String searchUsers(@RequestParam(required = false) String keyword,
                              HttpSession session,
                              Model model) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null || !"ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/users/login";
        }

        List<User> users = (keyword == null || keyword.isBlank())
                ? userService.getAllUsers()
                : userService.searchUsers(keyword);
        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "user/users";
    }

    @GetMapping
    public String listUsers(HttpSession session, Model model) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null || !"ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/users/login";
        }

        model.addAttribute("users", userService.getAllUsers());
        return "user/users";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable int id,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null || !"ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/users/login";
        }

        if (loggedInUser.getId() == id) {
            redirectAttributes.addFlashAttribute("error", "You cannot delete your own account.");
            return "redirect:/users";
        }

        if (userService.deleteUser(id)) {
            redirectAttributes.addFlashAttribute("success", "User deleted successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "User not found.");
        }
        return "redirect:/users";
    }

    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }

    private boolean safeEquals(String left, String right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }

    private String redirectToLandingPage(User user) {
        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/movies";
    }
}
