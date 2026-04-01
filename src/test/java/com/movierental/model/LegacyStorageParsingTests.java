package com.movierental.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyStorageParsingTests {

    @Test
    void userParserSupportsLegacyRecordsWithoutOptionalFields() {
        User user = User.fromString("9,tester,Password@1,tester@example.com,Test User");

        assertEquals(9, user.getId());
        assertEquals("tester", user.getUsername());
        assertEquals("USER", user.getRole());
        assertEquals("", user.getImagePath());
        assertTrue(user.isActive());
    }

    @Test
    void userParserSupportsProfileImageField() {
        User user = User.fromString("9,tester,Password@1,tester@example.com,Test User,USER,/uploads/profiles/tester.png,2026-03-31T10:00:00,true");

        assertEquals("/uploads/profiles/tester.png", user.getImagePath());
        assertEquals(LocalDateTime.of(2026, 3, 31, 10, 0), user.getRegistrationDate());
        assertTrue(user.isActive());
    }

    @Test
    void userParserSupportsNewFormatWithoutUploadedImageYet() {
        User user = User.fromString("9,tester,Password@1,tester@example.com,Test User,USER,,2026-03-31T10:00:00,true");

        assertEquals("", user.getImagePath());
        assertEquals(LocalDateTime.of(2026, 3, 31, 10, 0), user.getRegistrationDate());
        assertTrue(user.isActive());
    }

    @Test
    void movieParserBackfillsCopyCountsWhenLegacyRowsMissOneValue() {
        Movie movie = Movie.fromString("8,Arrival,Denis Villeneuve,Sci-Fi,2016-11-11,4.99,3");

        assertEquals(3, movie.getAvailableCopies());
        assertEquals(3, movie.getTotalCopies());
        assertEquals("", movie.getDescription());
    }

    @Test
    void movieParserSupportsDownloadLinkAndPosterPathFields() {
        Movie movie = Movie.fromString("10,Interstellar,Christopher Nolan,Sci-Fi,2014-11-07,5.99,4,4,Space exploration,https://example.com/interstellar,/uploads/posters/interstellar.png");

        assertEquals("https://example.com/interstellar", movie.getDownloadLink());
        assertEquals("/uploads/posters/interstellar.png", movie.getImagePath());
    }

    @Test
    void rentalParserAcceptsLegacyNullReturnDateValues() {
        Rental rental = Rental.fromString("1,2,1,2024-01-15T10:00:00,null,2024-01-18T10:00:00,11.97,false,0.0");

        assertEquals(1, rental.getId());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), rental.getRentalDate());
        assertNull(rental.getReturnDate());
        assertFalse(rental.isReturned());
        assertEquals(11.97, rental.getRentalFee());
    }

    @Test
    void reviewParserSupportsRowsWithoutApprovalFlag() {
        Review review = Review.fromString("3,2,4,john_doe,The Matrix,5,Excellent sci-fi,2024-01-17T16:20:00");

        assertEquals(5, review.getRating());
        assertEquals("Excellent sci-fi", review.getComment());
        assertEquals(LocalDateTime.of(2024, 1, 17, 16, 20), review.getReviewDate());
        assertTrue(review.isApproved());
    }
}
