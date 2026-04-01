package com.movierental.controller;

import com.movierental.model.Movie;
import com.movierental.model.User;
import com.movierental.service.MovieService;
import com.movierental.service.MoviePosterStorageService;
import com.movierental.service.ReviewService;
import com.movierental.service.RentalService;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/movies")
public class MovieController {

    private final MovieService movieService;
    private final MoviePosterStorageService moviePosterStorageService;
    private final ReviewService reviewService;
    private final RentalService rentalService;

    public MovieController(MovieService movieService,
                           MoviePosterStorageService moviePosterStorageService,
                           ReviewService reviewService,
                           RentalService rentalService) {
        this.movieService = movieService;
        this.moviePosterStorageService = moviePosterStorageService;
        this.reviewService = reviewService;
        this.rentalService = rentalService;
    }

    @GetMapping
    public String listMovies(Model model) {
        populateMovieGallery(model, movieService.getAllMovies(), "");
        return "movie/movies";
    }

    @GetMapping("/add")
    public String showAddMoviePage(HttpSession session,
                                   @RequestParam(required = false) String returnTo,
                                   Model model) {
        if (!isAdmin(session)) {
            return "redirect:/movies";
        }

        model.addAttribute("movie", new Movie());
        model.addAttribute("returnTo", normalizeReturnTo(returnTo));
        return "movie/add-movie";
    }

    @PostMapping("/add")
    public String addMovie(@ModelAttribute Movie movie,
                           @RequestParam(required = false) String returnTo,
                           @RequestParam(name = "posterImage", required = false) MultipartFile posterImage,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/movies";
        }

        String validation = movieService.validateMovieSubmission(movie);
        if (!"success".equals(validation)) {
            redirectAttributes.addFlashAttribute("error", validation);
            return redirectToAddPage(returnTo);
        }

        try {
            movie.setImagePath(moviePosterStorageService.storePoster(posterImage, movie.getTitle(), movie.getImagePath()));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return redirectToAddPage(returnTo);
        }

        String result = movieService.addMovie(movie);
        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Movie added successfully.");
            return redirectToMovieLanding(returnTo);
        }

        moviePosterStorageService.deletePoster(movie.getImagePath());
        redirectAttributes.addFlashAttribute("error", result);
        return redirectToAddPage(returnTo);
    }

    @GetMapping("/{id}")
    public String showMovieDetails(@PathVariable int id, HttpSession session, Model model) {
        Movie movie = movieService.getMovieById(id);
        if (movie == null) {
            return "redirect:/movies";
        }

        User user = getLoggedInUser(session);
        model.addAttribute("movie", movie);
        model.addAttribute("reviews", reviewService.getReviewsByMovie(id));
        model.addAttribute("averageRating", reviewService.getAverageRating(id));
        model.addAttribute("hasReviewed", user != null && reviewService.hasUserReviewedMovie(user.getId(), id));
        model.addAttribute("canDownload", user != null && rentalService.canUserDownloadMovie(user.getId(), id));
        return "movie/movie-details";
    }

    @GetMapping("/edit/{id}")
    public String showEditMoviePage(@PathVariable int id,
                                    HttpSession session,
                                    @RequestParam(required = false) String returnTo,
                                    Model model) {
        if (!isAdmin(session)) {
            return "redirect:/movies";
        }

        Movie movie = movieService.getMovieById(id);
        if (movie == null) {
            return "redirect:/movies";
        }

        model.addAttribute("movie", movie);
        model.addAttribute("returnTo", normalizeReturnTo(returnTo));
        return "movie/edit-movie";
    }

    @PostMapping("/edit/{id}")
    public String updateMovie(@PathVariable int id,
                              @ModelAttribute Movie movie,
                              @RequestParam(required = false) String returnTo,
                              @RequestParam(name = "posterImage", required = false) MultipartFile posterImage,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/movies";
        }

        Movie existingMovie = movieService.getMovieById(id);
        if (existingMovie == null) {
            redirectAttributes.addFlashAttribute("error", "Movie not found.");
            return "redirect:/movies";
        }

        movie.setId(id);
        String validation = movieService.validateMovieSubmission(movie);
        if (!"success".equals(validation)) {
            redirectAttributes.addFlashAttribute("error", validation);
            return redirectToEditPage(id, returnTo);
        }

        try {
            movie.setImagePath(moviePosterStorageService.storePoster(posterImage, movie.getTitle(), existingMovie.getImagePath()));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return redirectToEditPage(id, returnTo);
        }

        String result = movieService.updateMovie(movie);
        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Movie updated successfully.");
            return redirectToMovieUpdateLanding(id, returnTo);
        }

        if (!movie.getImagePath().equals(existingMovie.getImagePath())) {
            moviePosterStorageService.deletePoster(movie.getImagePath());
        }
        redirectAttributes.addFlashAttribute("error", result);
        return redirectToEditPage(id, returnTo);
    }

    @PostMapping("/delete/{id}")
    public String deleteMovie(@PathVariable int id,
                              @RequestParam(required = false) String returnTo,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/movies";
        }

        Movie movie = movieService.getMovieById(id);
        if (movieService.deleteMovie(id)) {
            if (movie != null) {
                moviePosterStorageService.deletePoster(movie.getImagePath());
            }
            redirectAttributes.addFlashAttribute("success", "Movie deleted successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Movie not found.");
        }
        return redirectToMovieLanding(returnTo);
    }

    @GetMapping("/search")
    public String searchMovies(@RequestParam(required = false) String keyword, Model model) {
        List<Movie> movies = movieService.searchMovies(keyword);
        populateMovieGallery(model, movies, keyword);
        return "movie/movies";
    }

    @GetMapping("/genre/{genre}")
    public String getMoviesByGenre(@PathVariable String genre, Model model) {
        populateMovieGallery(model, movieService.getMoviesByGenre(genre), genre);
        model.addAttribute("genre", genre);
        return "movie/movies";
    }

    private void populateMovieGallery(Model model, List<Movie> movies, String keyword) {
        List<Movie> safeMovies = movies == null ? List.of() : movies;
        Map<Integer, Double> averageRatings = new HashMap<>();
        Map<Integer, String> qualityLabels = new HashMap<>();

        for (Movie movie : safeMovies) {
            averageRatings.put(movie.getId(), reviewService.getAverageRating(movie.getId()));
            qualityLabels.put(movie.getId(), resolveQualityLabel(movie));
        }

        List<Movie> trendingMovies = safeMovies.stream()
                .sorted(Comparator
                        .comparingDouble((Movie movie) -> averageRatings.getOrDefault(movie.getId(), 0.0)).reversed()
                        .thenComparing(Movie::getReleaseDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER))
                .limit(8)
                .toList();

        model.addAttribute("movies", safeMovies);
        model.addAttribute("trendingMovies", trendingMovies);
        model.addAttribute("averageRatings", averageRatings);
        model.addAttribute("qualityLabels", qualityLabels);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
    }

    private String resolveQualityLabel(Movie movie) {
        String[] labels = {"WEB-DL", "WEB", "BLURAY", "HD"};
        int seed = movie.getTitle() == null ? movie.getId() : movie.getTitle().hashCode();
        if (movie.getDownloadLink() != null && !movie.getDownloadLink().isBlank()) {
            return labels[Math.floorMod(seed, 2)];
        }
        if (movie.getImagePath() != null && !movie.getImagePath().isBlank()) {
            return labels[2 + Math.floorMod(seed, 2)];
        }
        return "HD";
    }

    private boolean isAdmin(HttpSession session) {
        User user = getLoggedInUser(session);
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }

    private String normalizeReturnTo(String returnTo) {
        return "admin".equalsIgnoreCase(returnTo) ? "admin" : "";
    }

    private String redirectToAddPage(String returnTo) {
        return "admin".equalsIgnoreCase(returnTo)
                ? "redirect:/movies/add?returnTo=admin"
                : "redirect:/movies/add";
    }

    private String redirectToEditPage(int id, String returnTo) {
        return "admin".equalsIgnoreCase(returnTo)
                ? "redirect:/movies/edit/" + id + "?returnTo=admin"
                : "redirect:/movies/edit/" + id;
    }

    private String redirectToMovieLanding(String returnTo) {
        return "admin".equalsIgnoreCase(returnTo)
                ? "redirect:/admin/movies"
                : "redirect:/movies";
    }

    private String redirectToMovieUpdateLanding(int id, String returnTo) {
        return "admin".equalsIgnoreCase(returnTo)
                ? "redirect:/admin/movies"
                : "redirect:/movies/" + id;
    }
}
