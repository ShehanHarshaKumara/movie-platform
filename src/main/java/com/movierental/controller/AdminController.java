package com.movierental.controller;

import com.movierental.model.User;
import com.movierental.service.MovieService;
import com.movierental.service.RentalService;
import com.movierental.service.ReportService;
import com.movierental.service.ReviewService;
import com.movierental.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final MovieService movieService;
    private final RentalService rentalService;
    private final ReviewService reviewService;
    private final ReportService reportService;

    public AdminController(UserService userService,
                           MovieService movieService,
                           RentalService rentalService,
                           ReviewService reviewService,
                           ReportService reportService) {
        this.userService = userService;
        this.movieService = movieService;
        this.rentalService = rentalService;
        this.reviewService = reviewService;
        this.reportService = reportService;
    }

    @GetMapping
    public String adminHome(HttpSession session) {
        if (isAdmin(session)) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/admin/login";
    }

    @GetMapping("/login")
    public String showAdminLoginPage(HttpSession session) {
        if (isAdmin(session)) {
            return "redirect:/admin/dashboard";
        }
        return "admin/login";
    }

    @PostMapping("/login")
    public String loginAsAdmin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        User user = userService.authenticate(username, password);
        if (user == null) {
            model.addAttribute("error", "Invalid admin username or password.");
            return "admin/login";
        }

        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            model.addAttribute("error", "This account does not have administrator access.");
            return "admin/login";
        }

        session.setAttribute("loggedInUser", user);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return redirectToAdminLogin();
        }

        ReportService.Report report = reportService.generateReport();
        model.addAttribute("report", report);
        model.addAttribute("totalUsers", userService.getAllUsers().size());
        model.addAttribute("totalMovies", movieService.getAllMovies().size());
        model.addAttribute("activeRentals", rentalService.getActiveRentalsCount());
        model.addAttribute("pendingReviews", reviewService.getPendingReviews().size());
        model.addAttribute("overdueRentals", rentalService.getOverdueRentals().size());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return redirectToAdminLogin();
        }

        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @GetMapping("/movies")
    public String manageMovies(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return redirectToAdminLogin();
        }

        model.addAttribute("movies", movieService.getAllMovies());
        return "admin/movies";
    }

    @PostMapping("/users/toggle/{id}")
    public String toggleUserStatus(@PathVariable int id,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        User admin = getLoggedInUser(session);
        if (admin == null || !"ADMIN".equalsIgnoreCase(admin.getRole())) {
            return redirectToAdminLogin();
        }

        if (admin.getId() == id) {
            redirectAttributes.addFlashAttribute("error", "You cannot deactivate your own admin account.");
            return "redirect:/admin/users";
        }

        User user = userService.getUserById(id);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/admin/users";
        }

        user.setActive(!user.isActive());
        userService.updateUser(user);
        redirectAttributes.addFlashAttribute("success", "User status updated.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable int id,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User admin = getLoggedInUser(session);
        if (admin == null || !"ADMIN".equalsIgnoreCase(admin.getRole())) {
            return redirectToAdminLogin();
        }

        if (admin.getId() == id) {
            redirectAttributes.addFlashAttribute("error", "You cannot delete your own admin account.");
            return "redirect:/admin/users";
        }

        if (userService.deleteUser(id)) {
            redirectAttributes.addFlashAttribute("success", "User removed successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "User not found.");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/rentals")
    public String manageRentals(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return redirectToAdminLogin();
        }

        model.addAttribute("rentals", rentalService.getAllRentals());
        model.addAttribute("movieLookup", movieService.getMovieLookup());
        model.addAttribute("userLookup", userService.getUserLookup());
        return "admin/rentals";
    }

    @GetMapping("/reviews")
    public String manageReviews(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return redirectToAdminLogin();
        }

        model.addAttribute("allReviews", reviewService.getAllReviews());
        model.addAttribute("pendingReviews", reviewService.getPendingReviews());
        return "admin/manage-reviews";
    }

    @PostMapping("/reviews/approve/{id}")
    public String approveReview(@PathVariable int id,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return redirectToAdminLogin();
        }

        if (reviewService.approveReview(id)) {
            redirectAttributes.addFlashAttribute("success", "Review approved.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Review not found.");
        }
        return "redirect:/admin/reviews";
    }

    @PostMapping("/reviews/delete/{id}")
    public String deleteReview(@PathVariable int id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return redirectToAdminLogin();
        }

        if (reviewService.deleteReview(id)) {
            redirectAttributes.addFlashAttribute("success", "Review removed.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Review not found.");
        }
        return "redirect:/admin/reviews";
    }

    @GetMapping("/reports")
    public String showReports(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return redirectToAdminLogin();
        }

        model.addAttribute("report", reportService.generateReport());
        model.addAttribute("monthlyRevenue", reportService.getMonthlyRevenue(java.time.LocalDate.now().getYear()));
        return "admin/reports";
    }

    @GetMapping("/reports/user/{userId}")
    public String getUserReport(@PathVariable int userId, HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return redirectToAdminLogin();
        }

        model.addAttribute("userReport", reportService.getUserReport(userId));
        return "admin/user-report";
    }

    private boolean isAdmin(HttpSession session) {
        User user = getLoggedInUser(session);
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }

    private String redirectToAdminLogin() {
        return "redirect:/admin/login";
    }
}
