package com.groupandplay.invitation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.groupandplay.dto.InvitationDTO;
import com.groupandplay.group.Group;
import com.groupandplay.group.GroupService;
import com.groupandplay.user.User;
import com.groupandplay.user.UserService;

import jakarta.validation.Valid;

@Service
public class InvitationService {

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private InvitationRepository invitationRepository;

    public Page<InvitationDTO> getMyInvitations(User user, int page, int size) throws IllegalArgumentException {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Invitation> pageResult = invitationRepository.findAllByReceiver(user, pageable);
        return pageResult.map(InvitationDTO::new);
    }

    public Invitation createInvitation(InvitationDTO invitationDTO, User creator) throws IllegalArgumentException {
        Invitation invitation = new Invitation();

        User receiver = userService.getUserByUsername(invitationDTO.getReceiverUsername())
                .orElseThrow(() -> new IllegalArgumentException("Error al encontrar el usuario destino"));

        invitation.setReceiver(receiver);
        invitation.setSender(creator);
        invitation.setGroupInvitation(invitationDTO.getGroupInvitation());

        if (invitationDTO.getGroupInvitation() == true) {
            if (invitationDTO.getGroupId() == null) {
                throw new IllegalArgumentException("Una invitación de grupo debe incluir un ID de grupo.");
            }
            invitation.setGroup(groupService.findById(invitationDTO.getGroupId()));
        } else {
            invitation.setGroup(null);
        }

        return invitation;
    }

    public void acceptInvitation(Integer invitationId, User user) throws IllegalArgumentException {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado la invitación."));

        if (!invitation.getReceiver().equals(user)) {
            throw new IllegalArgumentException("No puedes aceptar una invitación que no es para ti.");
        }

        if (invitation.getGroupInvitation() == true) {
            if (invitation.getGroup() == null) {
                throw new IllegalArgumentException("La invitación de grupo no tiene un grupo asociado.");
            }
            Group group = invitation.getGroup();
            groupService.joinGroup(user, group);
        } else {
            User sender = invitation.getSender();
            user.addFriend(sender);
        }

        invitationRepository.delete(invitation);
    }

    public void rejectInvitation(Integer invitationId, User user) throws IllegalArgumentException {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado la invitación."));

        if (!invitation.getReceiver().equals(user)) {
            throw new IllegalArgumentException("No puedes rechazar una invitación que no es para ti.");
        }

        invitationRepository.delete(invitation);
    }

}
