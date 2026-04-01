package com.movierental.controller;

import com.movierental.model.Movie;
import com.movierental.model.User;
import com.movierental.service.MoviePosterStorageService;
import com.movierental.service.MovieService;
import com.movierental.service.ReviewService;
import com.movierental.service.RentalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieController.class)
class MovieControllerViewTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    @MockBean
    private MoviePosterStorageService moviePosterStorageService;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private RentalService rentalService;

    @Test
    void moviesPageShowsClickablePosterCardsWithMovieTitles() throws Exception {
        Movie movie = new Movie();
        movie.setId(1);
        movie.setTitle("Interstellar");
        movie.setDirector("Christopher Nolan");
        movie.setGenre("Sci-Fi");
        movie.setReleaseDate(LocalDate.of(2014, 11, 7));
        movie.setRentalPrice(5.99);
        movie.setAvailableCopies(4);
        movie.setTotalCopies(4);
        movie.setDescription("Space exploration epic");
        movie.setDownloadLink("https://example.com/interstellar");
        movie.setImagePath("/uploads/posters/interstellar.png");

        User admin = new User();
        admin.setId(1);
        admin.setUsername("admin");
        admin.setRole("ADMIN");

        given(movieService.getAllMovies()).willReturn(List.of(movie));
        given(reviewService.getAverageRating(1)).willReturn(8.8);

        mockMvc.perform(get("/movies").sessionAttr("loggedInUser", admin))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Trending Now")))
                .andExpect(content().string(containsString("All Movies")))
                .andExpect(content().string(containsString("/uploads/posters/interstellar.png")))
                .andExpect(content().string(containsString("Interstellar")))
                .andExpect(content().string(containsString("href=\"/movies/1\"")))
                .andExpect(content().string(containsString("8.8")))
                .andExpect(content().string(containsString("movie-poster-card")))
                .andExpect(content().string(not(containsString("Download"))))
                .andExpect(content().string(containsString("Manage Movies")));
    }
}
