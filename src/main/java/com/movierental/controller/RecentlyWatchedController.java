package com.movierental.controller;

import com.movierental.model.Movie;
import com.movierental.model.User;
import com.movierental.service.RecentlyWatchedService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Stack;

@Controller
@RequestMapping("/recently-watched")
public class RecentlyWatchedController {

    private final RecentlyWatchedService recentlyWatchedService;

    public RecentlyWatchedController(RecentlyWatchedService recentlyWatchedService) {
        this.recentlyWatchedService = recentlyWatchedService;
    }

    @GetMapping
    public String showRecentlyWatched(HttpSession session, Model model) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        Stack<Movie> movieStack = recentlyWatchedService.getRecentlyWatched(loggedInUser.getId());
        List<Movie> movieList = recentlyWatchedService.getRecentlyWatchedList(loggedInUser.getId());

        model.addAttribute("movieStack", movieStack);
        model.addAttribute("movieList", movieList);
        model.addAttribute("stackSize", movieStack.size());
        model.addAttribute("latestMovie", movieStack.isEmpty() ? null : movieStack.peek());
        return "recently-watched/history";
    }

    @PostMapping("/clear")
    public String clearHistory(HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        recentlyWatchedService.clearHistory(loggedInUser.getId());
        redirectAttributes.addFlashAttribute("success", "Watch history cleared.");
        return "redirect:/recently-watched";
    }

    @PostMapping("/remove/{movieId}")
    public String removeFromHistory(@PathVariable int movieId,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        recentlyWatchedService.removeFromHistory(loggedInUser.getId(), movieId);
        redirectAttributes.addFlashAttribute("success", "Movie removed from history.");
        return "redirect:/recently-watched";
    }

    @GetMapping("/latest")
    public String getLatestWatched(HttpSession session, Model model) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        Stack<Movie> stack = recentlyWatchedService.getRecentlyWatched(loggedInUser.getId());
        model.addAttribute("latestMovie", stack.isEmpty() ? null : stack.peek());
        return "recently-watched/latest";
    }

    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }
}
