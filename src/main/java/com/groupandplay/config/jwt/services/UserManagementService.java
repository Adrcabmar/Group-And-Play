package com.groupandplay.config.jwt.services;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.groupandplay.config.jwt.JWTUtils;
import com.groupandplay.dto.ReqRes;
import com.groupandplay.dto.UserDTO;
import com.groupandplay.user.User;
import com.groupandplay.user.UserRepository;

import jakarta.validation.ConstraintViolationException;

@Service
public class UserManagementService {
    

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ReqRes register(ReqRes registrationRequest) {
        ReqRes resp = new ReqRes();
        try {
            User user = new User();
            
            user.setFirstName(registrationRequest.getFirstName());
            user.setLastName(registrationRequest.getLastName());
            user.setUsername(registrationRequest.getUsername());
            user.setEmail(registrationRequest.getEmail());
            
            user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
            user.setRole(registrationRequest.getRole());
            User userResult = userRepo.save(user);
            if (userResult.getId()>0) {
                resp.setUser(new UserDTO(userResult));
                resp.setMessage("Usuario guardado exitosamente");
                resp.setStatusCode(200);
            }

        } catch (DataIntegrityViolationException e) {
        // Manejo de excepciones por violaciones de integridad (ej. usuario ya existe)
        resp.setStatusCode(400);
        resp.setError("El usuario con este nombre de usuario o correo electrónico.");
        } catch (ConstraintViolationException e) {
            // Manejo de errores relacionados con restricciones (ej. campos vacíos)
            resp.setStatusCode(400);
            resp.setError("Faltan campos obligatorios o son inválidos.");
        } catch (Exception e) {
            // Excepción general para otros errores
            resp.setStatusCode(500);
            resp.setError("Ocurrió un error inesperado: " + e.getMessage());
        }
        return resp;
    }

    public ReqRes login(ReqRes loginRequest) {
        ReqRes response = new ReqRes();
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
    
            var user = userRepo.findByUsername(loginRequest.getUsername()).orElseThrow();
            var jwt = jwtUtils.generateToken(user);
            var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);
    
            UserDTO userDTO = new UserDTO(user);
    
            response.setStatusCode(200);
            response.setToken(jwt);
            response.setRole(user.getRole()); 
            response.setRefreshToken(refreshToken);
            response.setExpirationTime("24Hrs");
            response.setMessage("Inicio de sesión exitoso");
    
            response.setUser(userDTO);
    
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    public ReqRes refreshToken(ReqRes refreshTokenReqiest){
        ReqRes response = new ReqRes();
        try{
            String ourUsername = jwtUtils.extractUsername(refreshTokenReqiest.getToken());
            User user = userRepo.findByUsername(ourUsername).orElseThrow();
            if (jwtUtils.isTokenValid(refreshTokenReqiest.getToken(), user)) {
                var jwt = jwtUtils.generateToken(user);
                response.setStatusCode(200);
                response.setToken(jwt);
                response.setRefreshToken(refreshTokenReqiest.getToken());
                response.setExpirationTime("24Hr");
                response.setMessage("Token actualizado exitosamente");
            }
            response.setStatusCode(200);
            return response;

        }catch (Exception e){
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            return response;
        }
    }

    public ReqRes getAllUsers() {
        ReqRes reqRes = new ReqRes();

        try {
            List<User> result = userRepo.findAll();
            if (!result.isEmpty()) {
                reqRes.setUsersList(result);
                reqRes.setStatusCode(200);
                reqRes.setMessage("Successful");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("No se encontraron usuarios");
            }
            return reqRes;
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred: " + e.getMessage());
            return reqRes;
        }
    }

    public ReqRes getUsersById(Integer id) {
        ReqRes reqRes = new ReqRes();
        try {
            User userById = userRepo.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            reqRes.setUser(new UserDTO(userById));
            reqRes.setStatusCode(200);
            reqRes.setMessage("Usuario con id '" + id + "' encontrado exitosamente");
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Ocurrió un error: " + e.getMessage());
        }
        return reqRes;
    }

    public ReqRes deleteUser(Integer userId) {
        ReqRes reqRes = new ReqRes();
        try {
            Optional<User> userOptional = userRepo.findById(userId);
            if (userOptional.isPresent()) {
                userRepo.deleteById(userId);
                reqRes.setStatusCode(200);
                reqRes.setMessage("Usuario eliminado exitosamente");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("Usuario no encontrado para eliminación");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Ocurrió un error al eliminar el usuario: " + e.getMessage());
        }
        return reqRes;
    }

    public ReqRes updateUser(Integer userId, User updatedUser) {
        ReqRes reqRes = new ReqRes();
        try {
            Optional<User> userOptional = userRepo.findById(userId);
            if (userOptional.isPresent()) {
                User existingUser = userOptional.get();
                existingUser.setFirstName(updatedUser.getFirstName());
                existingUser.setLastName(updatedUser.getLastName());
                existingUser.setUsername(updatedUser.getUsername());
                existingUser.setEmail(updatedUser.getEmail());
                existingUser.setRole(updatedUser.getRole());

                // Check if password is present in the request
                if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                    // Encode the password and update it
                    existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
                }

                User savedUser = userRepo.save(existingUser);
                reqRes.setUser(new UserDTO(savedUser));
                reqRes.setStatusCode(200);
                reqRes.setMessage("Usuario actualizado exitosamente");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("Usuario no encontrado para actualización");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Ocurrió un error al actualizar el usuario: " + e.getMessage());
        }
        return reqRes;
    }

    public ReqRes getMyInfo(String username){
        ReqRes reqRes = new ReqRes();
        try {
            Optional<User> userOptional = userRepo.findByUsername(username);
            if (userOptional.isPresent()) {
                reqRes.setUser(new UserDTO(userOptional.get()));
                reqRes.setStatusCode(200);
                reqRes.setMessage("successful");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("Usuario no encontrado para actualización");
            }

        }catch (Exception e){
            reqRes.setStatusCode(500);
            reqRes.setMessage("Ocurrió un error al obtener la información del usuario: " + e.getMessage());
        }
        return reqRes;

    }
}
