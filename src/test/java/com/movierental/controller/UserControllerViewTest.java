package com.movierental.controller;

import com.movierental.model.User;
import com.movierental.service.UserProfileImageStorageService;
import com.movierental.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerViewTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserProfileImageStorageService userProfileImageStorageService;

    @Test
    void registerPageDoesNotShowLongPasswordRulesText() throws Exception {
        mockMvc.perform(get("/users/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create Account")))
                .andExpect(content().string(not(containsString("Password must be at least 8 characters long"))));
    }

    @Test
    void loginPageDisablesBrowserAutofill() throws Exception {
        mockMvc.perform(get("/users/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("autocomplete=\"off\"")))
                .andExpect(content().string(containsString("name=\"loginShadowUser\"")))
                .andExpect(content().string(containsString("name=\"loginShadowPass\"")))
                .andExpect(content().string(containsString("name=\"username\"")))
                .andExpect(content().string(containsString("autocomplete=\"new-password\"")));
    }

    @Test
    void profilePageShowsManagementLinks() throws Exception {
        User user = new User();
        user.setId(2);
        user.setUsername("john_doe");
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setImagePath("/uploads/profiles/john-doe.png");
        user.setRole("USER");
        user.setRegistrationDate(LocalDateTime.of(2026, 3, 30, 10, 0));
        user.setActive(true);

        given(userService.getUserById(2)).willReturn(user);

        mockMvc.perform(get("/users/profile").sessionAttr("loggedInUser", user))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Browse Movies")))
                .andExpect(content().string(containsString("My Rentals")))
                .andExpect(content().string(containsString("Watch History")))
                .andExpect(content().string(containsString("My Reviews")))
                .andExpect(content().string(containsString("name=\"profileImage\"")))
                .andExpect(content().string(containsString("enctype=\"multipart/form-data\"")))
                .andExpect(content().string(containsString("/uploads/profiles/john-doe.png")));
    }

    @Test
    void loggedInUserIsRedirectedAwayFromLoginAndRegisterPages() throws Exception {
        User user = new User();
        user.setId(2);
        user.setUsername("john_doe");
        user.setRole("USER");

        mockMvc.perform(get("/users/login").sessionAttr("loggedInUser", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/movies"));

        mockMvc.perform(get("/users/register").sessionAttr("loggedInUser", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/movies"));
    }

    @Test
    void profileImageUploadRedirectsBackToProfile() throws Exception {
        User user = new User();
        user.setId(2);
        user.setUsername("john_doe");
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setRole("USER");

        MockMultipartFile image = new MockMultipartFile(
                "profileImage",
                "john.png",
                "image/png",
                "fake-image".getBytes()
        );

        given(userService.getUserById(2)).willReturn(user);
        given(userProfileImageStorageService.storeProfileImage(any(), eq("john_doe"), eq("")))
                .willReturn("/uploads/profiles/john.png");
        given(userService.updateProfileImage(2, "/uploads/profiles/john.png")).willReturn("success");

        mockMvc.perform(multipart("/users/profile/image")
                        .file(image)
                        .sessionAttr("loggedInUser", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/profile"));
    }
}
