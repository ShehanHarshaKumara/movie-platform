package com.movierental.controller;

import com.movierental.model.Movie;
import com.movierental.model.Review;
import com.movierental.model.User;
import com.movierental.service.MovieService;
import com.movierental.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final MovieService movieService;

    public ReviewController(ReviewService reviewService, MovieService movieService) {
        this.reviewService = reviewService;
        this.movieService = movieService;
    }

    @GetMapping("/add/{movieId}")
    public String showAddReviewPage(@PathVariable int movieId, HttpSession session, Model model) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        Movie movie = movieService.getMovieById(movieId);
        if (movie == null) {
            return "redirect:/movies";
        }

        if (reviewService.hasUserReviewedMovie(loggedInUser.getId(), movieId)) {
            return "redirect:/movies/" + movieId;
        }

        Review review = new Review();
        review.setMovieId(movieId);
        review.setMovieTitle(movie.getTitle());
        model.addAttribute("review", review);
        model.addAttribute("movie", movie);
        return "review/add-review";
    }

    @PostMapping("/add/{movieId}")
    public String addReview(@PathVariable int movieId,
                            @ModelAttribute Review review,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        review.setMovieId(movieId);
        review.setUserId(loggedInUser.getId());
        review.setUsername(loggedInUser.getUsername());

        Movie movie = movieService.getMovieById(review.getMovieId());
        if (movie == null) {
            redirectAttributes.addFlashAttribute("error", "Movie not found.");
            return "redirect:/movies";
        }

        review.setMovieTitle(movie.getTitle());
        String result = reviewService.addReview(review);
        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Review added successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }
        return "redirect:/movies/" + review.getMovieId();
    }

    @GetMapping("/edit/{id}")
    public String showEditReviewPage(@PathVariable int id, HttpSession session, Model model) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        Review review = reviewService.getReviewById(id);
        if (review == null || review.getUserId() != loggedInUser.getId()) {
            return "redirect:/movies";
        }

        model.addAttribute("review", review);
        model.addAttribute("movie", movieService.getMovieById(review.getMovieId()));
        return "review/edit-review";
    }

    @PostMapping("/edit/{id}")
    public String updateReview(@PathVariable int id,
                               @ModelAttribute Review updatedReview,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        Review existingReview = reviewService.getReviewById(id);
        if (existingReview == null || existingReview.getUserId() != loggedInUser.getId()) {
            return "redirect:/movies";
        }

        existingReview.setRating(updatedReview.getRating());
        existingReview.setComment(updatedReview.getComment());

        if (reviewService.updateReview(existingReview)) {
            redirectAttributes.addFlashAttribute("success", "Review updated successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Review could not be updated.");
        }
        return "redirect:/movies/" + existingReview.getMovieId();
    }

    @PostMapping("/delete/{id}")
    public String deleteReview(@PathVariable int id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        Review review = reviewService.getReviewById(id);
        if (review != null && (review.getUserId() == loggedInUser.getId()
                || "ADMIN".equalsIgnoreCase(loggedInUser.getRole()))) {
            reviewService.deleteReview(id);
            redirectAttributes.addFlashAttribute("success", "Review deleted.");
            return "redirect:/movies/" + review.getMovieId();
        }

        redirectAttributes.addFlashAttribute("error", "You are not allowed to delete that review.");
        return "redirect:/movies";
    }

    @GetMapping("/movie/{movieId}")
    public String getMovieReviews(@PathVariable int movieId,
                                  HttpSession session,
                                  Model model) {
        Movie movie = movieService.getMovieById(movieId);
        if (movie == null) {
            return "redirect:/movies";
        }

        User loggedInUser = getLoggedInUser(session);
        model.addAttribute("movie", movie);
        model.addAttribute("reviews", reviewService.getReviewsByMovie(movieId));
        model.addAttribute("averageRating", reviewService.getAverageRating(movieId));
        model.addAttribute("hasReviewed",
                loggedInUser != null && reviewService.hasUserReviewedMovie(loggedInUser.getId(), movieId));
        return "review/movie-reviews";
    }

    @GetMapping("/my")
    public String getMyReviews(HttpSession session, Model model) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        model.addAttribute("reviews", reviewService.getReviewsByUser(loggedInUser.getId()));
        return "review/reviews";
    }

    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }
}
