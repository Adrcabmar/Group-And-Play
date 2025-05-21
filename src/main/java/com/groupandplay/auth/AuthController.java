package com.groupandplay.auth;

import com.groupandplay.config.JWTAuthFilter;
import com.groupandplay.user.User;
import com.groupandplay.user.UserRepository;
import com.groupandplay.user.UserService;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTAuthFilter jwtAuthFilter;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private User getCurrentUserLogged() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> user = userService.getUserByUsername(request.getUsername());

        if (user.isPresent() && passwordEncoder.matches(request.getPassword(), user.get().getPassword())) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        }
    }

    @GetMapping("/discord/callback")
    public ResponseEntity<?> linkDiscordAccount(
            @RequestParam("code") String code,
            @RequestParam("state") String userIdString) {

        String redirectUri = "http://localhost:8080/api/auth/discord/callback";
        String frontendRedirectOk = "http://localhost:5173/my-profile?discord=success";
        String frontendRedirectError = "http://localhost:5173/my-profile?discord=error";

        try {
            Integer userId = Integer.parseInt(userIdString);
            User currentUser = userRepository.findById(userId).orElseThrow();

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", "1374722658574401596");
            body.add("client_secret", "qzukoSfzTPF8aYUItFLRvScwK1m0_bPv");
            body.add("grant_type", "authorization_code");
            body.add("code", code);
            body.add("redirect_uri", redirectUri);
            body.add("scope", "identify");

            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(body, headers);
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                    "https://discord.com/api/oauth2/token",
                    tokenRequest,
                    Map.class);

            if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
                return redirect(frontendRedirectError);
            }

            String accessToken = (String) tokenResponse.getBody().get("access_token");

            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);
            HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

            ResponseEntity<Map> userResponse = restTemplate.exchange(
                    "https://discord.com/api/users/@me",
                    HttpMethod.GET,
                    userRequest,
                    Map.class);

            if (!userResponse.getStatusCode().is2xxSuccessful()) {
                return redirect(frontendRedirectError);
            }

            Map<String, Object> discordUser = userResponse.getBody();
            String username = (String) discordUser.get("username");
            String discriminator = (String) discordUser.get("discriminator");
            String discordName = discriminator.equals("0")
                    ? username
                    : username + "#" + discriminator;
            currentUser.setDiscordName(discordName);
            userRepository.save(currentUser);

            return redirect(frontendRedirectOk);

        } catch (Exception e) {
            e.printStackTrace();
            return redirect(frontendRedirectError);
        }
    }

    @PatchMapping("/discord/unlink")
    public ResponseEntity<?> unlinkDiscordAccount() {
        try {
            User user = getCurrentUserLogged();
            user.setDiscordName(null);
            userRepository.save(user);
            return ResponseEntity.ok("Cuenta de Discord desvinculada correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al desvincular Discord");
        }
    }

    private ResponseEntity<?> redirect(String uri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(java.net.URI.create(uri));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

}
