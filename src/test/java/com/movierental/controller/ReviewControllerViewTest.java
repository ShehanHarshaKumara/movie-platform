package com.movierental.controller;

import com.movierental.model.Movie;
import com.movierental.model.User;
import com.movierental.service.MovieService;
import com.movierental.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
class ReviewControllerViewTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private MovieService movieService;

    @Test
    void addReviewPagePostsToMovieSpecificEndpoint() throws Exception {
        User user = new User();
        user.setId(4);
        user.setUsername("jane");
        user.setRole("USER");

        Movie movie = new Movie();
        movie.setId(7);
        movie.setTitle("Arrival");
        movie.setDirector("Denis Villeneuve");
        movie.setGenre("Sci-Fi");
        movie.setReleaseDate(LocalDate.of(2016, 11, 11));

        given(movieService.getMovieById(7)).willReturn(movie);
        given(reviewService.hasUserReviewedMovie(4, 7)).willReturn(false);

        mockMvc.perform(get("/reviews/add/7").sessionAttr("loggedInUser", user))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/reviews/add/7\"")))
                .andExpect(content().string(containsString("name=\"movieId\"")))
                .andExpect(content().string(containsString("value=\"7\"")));
    }

    @Test
    void addReviewPostSubmitsWithoutMethodNotAllowed() throws Exception {
        User user = new User();
        user.setId(4);
        user.setUsername("jane");
        user.setRole("USER");

        Movie movie = new Movie();
        movie.setId(7);
        movie.setTitle("Arrival");

        given(movieService.getMovieById(7)).willReturn(movie);
        given(reviewService.addReview(any())).willReturn("success");

        mockMvc.perform(post("/reviews/add/7")
                        .sessionAttr("loggedInUser", user)
                        .param("rating", "5")
                        .param("comment", "Excellent movie"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/movies/7"));
    }
}
