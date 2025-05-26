package com.groupandplay.config.jwt;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class JWTHandshakeInterceptor implements HandshakeInterceptor {

    private final JWTUtils jwtUtils;

    public JWTHandshakeInterceptor(JWTUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();

            String tokenParam = httpServletRequest.getParameter("token");


            if (tokenParam != null && !tokenParam.isBlank()) {
                try {
                    String username = jwtUtils.extractUsername(tokenParam);
                    System.out.println("[Handshake] Token válido. Usuario: " + username);
                    attributes.put("username", username);
                    return true;
                } catch (Exception e) {
                    System.out.println("[Handshake] Token inválido: " + e.getMessage());
                }
            } else {
                System.out.println("[Handshake] Token no encontrado en parámetros");
            }
        }

        response.setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
    }
}
