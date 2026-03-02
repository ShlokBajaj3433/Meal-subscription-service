package com.mealsubscription.controller;

import com.mealsubscription.dto.request.MealRequest;
import com.mealsubscription.dto.request.RegisterRequest;
import com.mealsubscription.dto.response.MealResponse;
import com.mealsubscription.entity.DietaryType;
import com.mealsubscription.security.JwtTokenProvider;
import com.mealsubscription.service.MealService;
import com.mealsubscription.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Serves Thymeleaf HTML pages for Selenium E2E testing.
 *
 * Authentication flow:
 *  POST /web/login   → sets jwt_token cookie (JS reads it for API calls) → redirect /dashboard
 *  POST /web/register → redirect /login
 *
 * Admin operations use direct service calls (no REST layer) for reliable Selenium behaviour.
 */
@Controller
@RequiredArgsConstructor
public class FrontendController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final MealService mealService;

    // ── Auth pages ────────────────────────────────────────────────────────────

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/web/login")
    public String webLogin(@RequestParam String email,
                           @RequestParam String password,
                           HttpServletResponse response,
                           Model model) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            String token = jwtTokenProvider.generateToken(auth);

            // Store JWT in a regular (non-HttpOnly) cookie so JS on each page can read it
            Cookie cookie = new Cookie("jwt_token", token);
            cookie.setPath("/");
            cookie.setMaxAge(86_400);  // 24 h
            cookie.setHttpOnly(false); // JS must be able to read value for API call headers
            response.addCookie(cookie);

            return "redirect:/dashboard";
        } catch (Exception ex) {
            model.addAttribute("error", "Invalid email or password");
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/web/register")
    public String webRegister(@RequestParam String name,
                               @RequestParam String email,
                               @RequestParam String password,
                               Model model) {
        try {
            userService.register(new RegisterRequest(name, email, password));
            return "redirect:/login";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
    }

    // ── User pages ────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "dashboard";
    }

    @GetMapping("/meals")
    public String mealsPage(@RequestParam(required = false) String dietary,
                             Model model) {
        DietaryType dietaryType = parseDietary(dietary);
        PageRequest pageable = PageRequest.of(0, 50, Sort.by("name"));
        List<MealResponse> meals = mealService.listAvailable(dietaryType, pageable).getContent();
        model.addAttribute("meals", meals);
        model.addAttribute("dietary", dietary != null ? dietary : "");
        return "meals";
    }

    // ── Admin pages ───────────────────────────────────────────────────────────

    @GetMapping("/admin/meals")
    public String adminMealsPage(Model model) {
        PageRequest pageable = PageRequest.of(0, 100, Sort.by("name"));
        List<MealResponse> meals = mealService.listAvailable(null, pageable).getContent();
        model.addAttribute("meals", meals);
        model.addAttribute("dietaryTypes", DietaryType.values());
        return "admin-meals";
    }

    @PostMapping("/web/admin/meals/create")
    public String adminCreateMeal(@RequestParam String name,
                                   @RequestParam(required = false) String description,
                                   @RequestParam String dietaryType,
                                   @RequestParam Integer calories,
                                   @RequestParam Long priceCents) {
        DietaryType type = DietaryType.valueOf(dietaryType);
        MealRequest request = new MealRequest(name, description, type, calories, priceCents, null, true);
        mealService.create(request);
        return "redirect:/admin/meals";
    }

    @PostMapping("/web/admin/meals/{id}/delete")
    public String adminDeleteMeal(@PathVariable Long id) {
        mealService.delete(id);
        return "redirect:/admin/meals";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private DietaryType parseDietary(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return DietaryType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

