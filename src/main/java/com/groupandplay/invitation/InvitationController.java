package com.groupandplay.invitation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.groupandplay.dto.GroupDTO;
import com.groupandplay.dto.InvitationDTO;
import com.groupandplay.user.User;
import com.groupandplay.user.UserRepository;
import com.groupandplay.user.UserService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvitationService invitationService;

    private User getCurrentUserLogged() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @GetMapping("/all-invitations")
    public ResponseEntity<?> getMyInvitations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        User user = getCurrentUserLogged();
        Page<InvitationDTO> invitations = invitationService.getMyInvitations(user, page, size);
        return ResponseEntity.ok(invitations);
    }

    @GetMapping("/friend-invitations")
    public ResponseEntity<?> getMyFriendInvitations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        User user = getCurrentUserLogged();
        Page<InvitationDTO> invitations = invitationService.getFriendInvitations(user, page, size);
        return ResponseEntity.ok(invitations);
    }

    @GetMapping("/group-invitations")
    public ResponseEntity<?> getMyGroupInvitations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        User user = getCurrentUserLogged();
        Page<InvitationDTO> invitations = invitationService.getGroupInvitations(user, page, size);
        return ResponseEntity.ok(invitations);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createInvitation(@Valid @RequestBody InvitationDTO invitationDTO)
            throws IllegalArgumentException {

        User creator = getCurrentUserLogged();

        Invitation newInvitation = invitationService.createInvitation(invitationDTO, creator);
        return ResponseEntity.ok(new InvitationDTO(newInvitation));
    }

    @PostMapping("/accept/{invitationId}")
    public ResponseEntity<?> acceptInvitation(@PathVariable Integer invitationId) {
        User user = getCurrentUserLogged();
        invitationService.acceptInvitation(invitationId, user.getId());
        return ResponseEntity.ok("Invitación aceptada correctamente.");
    }

    @PostMapping("/reject/{invitationId}")
    public ResponseEntity<?> rejectInvitation(@PathVariable Integer invitationId) {
        User user = getCurrentUserLogged();
        invitationService.rejectInvitation(invitationId, user);
        return ResponseEntity.ok("Invitación rechazada correctamente.");
    }

}
