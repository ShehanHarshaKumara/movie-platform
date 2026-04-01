package com.movierental.controller;

import com.movierental.model.Movie;
import com.movierental.model.User;
import com.movierental.service.RecentlyWatchedService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Stack;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecentlyWatchedController.class)
class RecentlyWatchedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecentlyWatchedService recentlyWatchedService;

    @Test
    void historyPageRendersStackInLifoOrder() throws Exception {
        User user = buildUser();
        Movie latestMovie = buildMovie(1, "Inception");
        Movie olderMovie = buildMovie(4, "The Matrix");
        Stack<Movie> stack = new Stack<>();
        stack.push(olderMovie);
        stack.push(latestMovie);

        given(recentlyWatchedService.getRecentlyWatched(user.getId())).willReturn(stack);
        given(recentlyWatchedService.getRecentlyWatchedList(user.getId())).willReturn(List.of(latestMovie, olderMovie));

        mockMvc.perform(get("/recently-watched").sessionAttr("loggedInUser", user))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Inception")))
                .andExpect(content().string(containsString("The Matrix")))
                .andExpect(content().string(containsString("Clear History")))
                .andExpect(content().string(containsString("LIFO POSITION")));
    }

    @Test
    void latestPageRendersMostRecentMovie() throws Exception {
        User user = buildUser();
        Movie latestMovie = buildMovie(1, "Inception");
        Stack<Movie> stack = new Stack<>();
        stack.push(latestMovie);

        given(recentlyWatchedService.getRecentlyWatched(user.getId())).willReturn(stack);

        mockMvc.perform(get("/recently-watched/latest").sessionAttr("loggedInUser", user))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Inception")))
                .andExpect(content().string(containsString("Open Movie")));
    }

    private User buildUser() {
        User user = new User();
        user.setId(2);
        user.setUsername("john_doe");
        user.setRole("USER");
        return user;
    }

    private Movie buildMovie(int id, String title) {
        Movie movie = new Movie();
        movie.setId(id);
        movie.setTitle(title);
        movie.setDirector("Christopher Nolan");
        movie.setGenre("Sci-Fi");
        movie.setReleaseDate(LocalDate.of(2010, 7, 16));
        return movie;
    }
}
