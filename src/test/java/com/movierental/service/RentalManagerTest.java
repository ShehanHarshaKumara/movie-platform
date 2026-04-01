package com.movierental.service;

import com.movierental.model.Rental;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class RentalManagerTest {

    @Test
    void rentalManagerDelegatesRentalResponsibilities() {
        RentalService rentalService = mock(RentalService.class);
        RentalManager rentalManager = new RentalManager(rentalService);
        Rental rental = new Rental();
        rental.setId(12);
        rental.setUserId(3);
        rental.setMovieId(9);

        given(rentalService.rentMovie(3, 9, 5)).willReturn("success");
        given(rentalService.returnMovie(12)).willReturn(true);
        given(rentalService.getActiveRentalsByUser(3)).willReturn(List.of(rental));
        given(rentalService.getRentalHistoryByUser(3)).willReturn(List.of(rental));
        given(rentalService.calculateRentalFee(9, 5)).willReturn(24.95);
        given(rentalService.calculateTotalRentalFee(3)).willReturn(36.95);
        given(rentalService.getRentalById(12)).willReturn(rental);
        given(rentalService.getAllRentals()).willReturn(List.of(rental));
        given(rentalService.getOverdueRentals()).willReturn(List.of(rental));

        assertEquals("success", rentalManager.rentMovie(3, 9, 5));
        assertEquals(true, rentalManager.returnMovie(12));
        assertEquals(1, rentalManager.trackRentedMovies(3).size());
        assertEquals(1, rentalManager.getRentalHistory(3).size());
        assertEquals(24.95, rentalManager.calculateRentalFee(9, 5));
        assertEquals(36.95, rentalManager.calculateTotalRentalFee(3));
        assertEquals(12, rentalManager.getRentalById(12).getId());
        assertEquals(1, rentalManager.getAllRentals().size());
        assertEquals(1, rentalManager.getOverdueRentals().size());
    }
}
