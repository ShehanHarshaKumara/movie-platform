package com.movierental.controller;

import com.movierental.model.Movie;
import com.movierental.model.User;
import com.movierental.service.MovieService;
import com.movierental.service.RentalService;
import com.movierental.service.RecentlyWatchedService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RentalController.class)
class RentalControllerViewTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RentalService rentalService;

    @MockBean
    private MovieService movieService;

    @MockBean
    private RecentlyWatchedService recentlyWatchedService;

    @Test
    void rentMoviePagePostsToMovieSpecificEndpoint() throws Exception {
        User user = new User();
        user.setId(3);
        user.setUsername("alex");
        user.setRole("USER");

        Movie movie = new Movie();
        movie.setId(9);
        movie.setTitle("Dune");
        movie.setDirector("Denis Villeneuve");
        movie.setGenre("Sci-Fi");
        movie.setReleaseDate(LocalDate.of(2021, 10, 22));
        movie.setRentalPrice(4.99);
        movie.setAvailableCopies(5);

        given(movieService.getMovieById(9)).willReturn(movie);
        given(rentalService.calculateRentalFee(9, 3)).willReturn(14.97);
        given(rentalService.getActiveRentalByUserAndMovie(3, 9)).willReturn(null);

        mockMvc.perform(get("/rentals/rent/9").sessionAttr("loggedInUser", user))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/rentals/rent/9\"")))
                .andExpect(content().string(containsString("name=\"movieId\"")))
                .andExpect(content().string(containsString("value=\"9\"")));
    }

    @Test
    void confirmRentalRedirectsToPaymentPage() throws Exception {
        User user = new User();
        user.setId(3);
        user.setUsername("alex");
        user.setRole("USER");

        given(rentalService.validateRentalRequest(3, 9, 3)).willReturn("success");

        mockMvc.perform(post("/rentals/rent/9")
                        .sessionAttr("loggedInUser", user)
                        .param("days", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rentals/payment/9?days=3"));
    }

    @Test
    void paymentPageShowsCardForm() throws Exception {
        User user = new User();
        user.setId(3);
        user.setUsername("alex");
        user.setRole("USER");

        Movie movie = new Movie();
        movie.setId(9);
        movie.setTitle("Dune");
        movie.setRentalPrice(4.99);

        given(rentalService.validateRentalRequest(3, 9, 3)).willReturn("success");
        given(rentalService.calculateRentalFee(9, 3)).willReturn(14.97);
        given(movieService.getMovieById(9)).willReturn(movie);

        mockMvc.perform(get("/rentals/payment/9")
                        .sessionAttr("loggedInUser", user)
                        .param("days", "3"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/rentals/payment/9\"")))
                .andExpect(content().string(containsString("name=\"cardholderName\"")))
                .andExpect(content().string(containsString("Pay &amp; Unlock Download")))
                .andExpect(content().string(containsString("name=\"paymentShadowName\"")))
                .andExpect(content().string(containsString("autocomplete=\"off\"")))
                .andExpect(content().string(containsString("autocomplete=\"new-password\"")));
    }
}
