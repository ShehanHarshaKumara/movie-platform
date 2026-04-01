package com.movierental.controller;

import com.movierental.model.Movie;
import com.movierental.model.Rental;
import com.movierental.model.User;
import com.movierental.service.MovieService;
import com.movierental.service.RentalService;
import com.movierental.service.RecentlyWatchedService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/rentals")
public class RentalController {

    private final RentalService rentalService;
    private final MovieService movieService;
    private final RecentlyWatchedService recentlyWatchedService;

    public RentalController(RentalService rentalService,
                            MovieService movieService,
                            RecentlyWatchedService recentlyWatchedService) {
        this.rentalService = rentalService;
        this.movieService = movieService;
        this.recentlyWatchedService = recentlyWatchedService;
    }

    @GetMapping("/rent/{movieId}")
    public String showRentPage(@PathVariable int movieId, HttpSession session, Model model) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        Movie movie = movieService.getMovieById(movieId);
        if (movie == null || movie.getAvailableCopies() <= 0) {
            return "redirect:/movies";
        }
        if (rentalService.getActiveRentalByUserAndMovie(loggedInUser.getId(), movieId) != null) {
            return "redirect:/movies/" + movieId;
        }

        model.addAttribute("movie", movie);
        model.addAttribute("defaultDays", 3);
        model.addAttribute("estimatedFee", rentalService.calculateRentalFee(movieId, 3));
        return "rental/rent-movie";
    }

    @PostMapping("/rent/{movieId}")
    public String rentMovie(@PathVariable int movieId,
                            @RequestParam int days,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        String result = rentalService.validateRentalRequest(loggedInUser.getId(), movieId, days);
        if ("success".equals(result)) {
            return "redirect:/rentals/payment/" + movieId + "?days=" + days;
        }

        redirectAttributes.addFlashAttribute("error", result);
        return "redirect:/rentals/rent/" + movieId;
    }

    @GetMapping("/payment/{movieId}")
    public String showPaymentPage(@PathVariable int movieId,
                                  @RequestParam int days,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        String validation = rentalService.validateRentalRequest(loggedInUser.getId(), movieId, days);
        if (!"success".equals(validation)) {
            redirectAttributes.addFlashAttribute("error", validation);
            return "redirect:/rentals/rent/" + movieId;
        }

        Movie movie = movieService.getMovieById(movieId);
        model.addAttribute("movie", movie);
        model.addAttribute("days", days);
        model.addAttribute("estimatedFee", rentalService.calculateRentalFee(movieId, days));
        return "rental/payment";
    }

    @PostMapping("/payment/{movieId}")
    public String processPayment(@PathVariable int movieId,
                                 @RequestParam int days,
                                 @RequestParam String cardholderName,
                                 @RequestParam String cardNumber,
                                 @RequestParam String expiryDate,
                                 @RequestParam String cvv,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        String rentalValidation = rentalService.validateRentalRequest(loggedInUser.getId(), movieId, days);
        if (!"success".equals(rentalValidation)) {
            redirectAttributes.addFlashAttribute("error", rentalValidation);
            return "redirect:/rentals/rent/" + movieId;
        }

        String result = rentalService.rentMovie(loggedInUser.getId(), movieId, days);
        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Payment successful. Rental activated and movie download unlocked.");
            return "redirect:/movies/" + movieId;
        }

        redirectAttributes.addFlashAttribute("error", result);
        return "redirect:/rentals/payment/" + movieId + "?days=" + days;
    }

    @GetMapping("/my-rentals")
    public String showMyRentals(HttpSession session, Model model) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        List<Rental> activeRentals = rentalService.getActiveRentalsByUser(loggedInUser.getId());
        List<Rental> rentalHistory = rentalService.getRentalHistoryByUser(loggedInUser.getId());
        Map<Integer, Boolean> downloadableRentals = activeRentals.stream()
                .collect(Collectors.toMap(Rental::getId,
                        rental -> rentalService.canUserDownloadMovie(loggedInUser.getId(), rental.getMovieId())));

        model.addAttribute("activeRentals", activeRentals);
        model.addAttribute("rentalHistory", rentalHistory);
        model.addAttribute("downloadableRentals", downloadableRentals);
        model.addAttribute("movieLookup", movieService.getMovieLookup());
        model.addAttribute("totalSpent", rentalService.calculateTotalRentalFee(loggedInUser.getId()));
        return "rental/rentals";
    }

    @GetMapping("/return/{rentalId}")
    public String showReturnPage(@PathVariable int rentalId, HttpSession session, Model model) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        Rental rental = rentalService.getRentalById(rentalId);
        if (rental == null || rental.getUserId() != loggedInUser.getId()) {
            return "redirect:/rentals/my-rentals";
        }

        model.addAttribute("rental", rental);
        model.addAttribute("movie", movieService.getMovieById(rental.getMovieId()));
        return "rental/return-movie";
    }

    @PostMapping("/return/{rentalId}")
    public String returnMovie(@PathVariable int rentalId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        Rental rental = rentalService.getRentalById(rentalId);
        if (rental == null || rental.getUserId() != loggedInUser.getId()) {
            redirectAttributes.addFlashAttribute("error", "Rental not found.");
            return "redirect:/rentals/my-rentals";
        }

        if (rentalService.returnMovie(rentalId)) {
            recentlyWatchedService.addToRecentlyWatched(loggedInUser.getId(), rental.getMovieId());
            redirectAttributes.addFlashAttribute("success", "Movie returned successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Movie could not be returned.");
        }
        return "redirect:/rentals/my-rentals";
    }

    @GetMapping("/watch/{movieId}")
    public String watchMovie(@PathVariable int movieId, HttpSession session) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        recentlyWatchedService.addToRecentlyWatched(loggedInUser.getId(), movieId);
        return "redirect:/movies/" + movieId;
    }

    @GetMapping("/download/{movieId}")
    public String downloadMovie(@PathVariable int movieId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        Movie movie = movieService.getMovieById(movieId);
        if (movie == null) {
            redirectAttributes.addFlashAttribute("error", "Movie not found.");
            return "redirect:/movies";
        }

        if (!rentalService.canUserDownloadMovie(loggedInUser.getId(), movieId)) {
            redirectAttributes.addFlashAttribute("error", "Complete your rental payment before downloading this movie.");
            return "redirect:/movies/" + movieId;
        }

        return "redirect:" + movie.getDownloadLink();
    }

    @GetMapping("/all")
    public String viewAllRentals(HttpSession session, Model model) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null || !"ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/users/login";
        }

        model.addAttribute("rentals", rentalService.getAllRentals());
        model.addAttribute("overdueRentals", rentalService.getOverdueRentals());
        model.addAttribute("movieLookup", movieService.getMovieLookup());
        return "rental/all-rentals";
    }

    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }
}
