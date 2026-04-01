package com.movierental.controller;

import com.movierental.model.Movie;
import com.movierental.model.User;
import com.movierental.service.MovieService;
import com.movierental.service.RentalService;
import com.movierental.service.ReportService;
import com.movierental.service.ReviewService;
import com.movierental.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerViewTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private MovieService movieService;

    @MockBean
    private RentalService rentalService;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private ReportService reportService;

    @Test
    void adminMoviesPageShowsMovieManagementActions() throws Exception {
        User admin = new User();
        admin.setId(1);
        admin.setUsername("admin");
        admin.setRole("ADMIN");

        Movie movie = new Movie();
        movie.setId(7);
        movie.setTitle("Arrival");
        movie.setDirector("Denis Villeneuve");
        movie.setGenre("Sci-Fi");
        movie.setReleaseDate(LocalDate.of(2016, 11, 11));
        movie.setRentalPrice(4.99);
        movie.setAvailableCopies(3);
        movie.setTotalCopies(5);
        movie.setDownloadLink("https://example.com/arrival");

        given(movieService.getAllMovies()).willReturn(List.of(movie));

        mockMvc.perform(get("/admin/movies").sessionAttr("loggedInUser", admin))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Movie Management")))
                .andExpect(content().string(containsString("Arrival")))
                .andExpect(content().string(containsString("Open Link")))
                .andExpect(content().string(containsString("View Details")))
                .andExpect(content().string(containsString("Add New Movie")));
    }
}
