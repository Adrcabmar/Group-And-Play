package com.groupandplay.invitation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.groupandplay.dto.InvitationDTO;
import com.groupandplay.group.Group;
import com.groupandplay.group.GroupService;
import com.groupandplay.user.User;
import com.groupandplay.user.UserRepository;
import com.groupandplay.user.UserService;

import jakarta.validation.Valid;

@Service
public class InvitationService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupService groupService;

    @Autowired
    private InvitationRepository invitationRepository;

    @Transactional(readOnly = true)
    public Page<InvitationDTO> getMyInvitations(User user, int page, int size) throws IllegalArgumentException {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Invitation> pageResult = invitationRepository.findAllByReceiver(user, pageable);
        return pageResult.map(InvitationDTO::new);
    }

    @Transactional
    public Invitation createInvitation(InvitationDTO invitationDTO, User creator) throws IllegalArgumentException {
        Invitation invitation = new Invitation();
    
        User receiver = userService.getUserByUsername(invitationDTO.getReceiverUsername())
                .orElseThrow(() -> new IllegalArgumentException("No existe el usuario destino."));
    
        if (creator.equals(receiver)) {
            throw new IllegalArgumentException("No puedes enviarte una invitación a ti mismo.");
        }
    
        invitation.setReceiver(receiver);
        invitation.setSender(creator);
        invitation.setGroupInvitation(invitationDTO.getGroupInvitation());
    
        if (invitationDTO.getGroupInvitation() == true) {
            //Si es invitacion de grupo
            if (invitationDTO.getGroupId() == null) {
                throw new IllegalArgumentException("Una invitación de grupo debe incluir un ID de grupo.");
            }
    
            Group group = groupService.findById(invitationDTO.getGroupId());
    
            if (groupService.isMemberOfGroup(receiver, group)) {
                throw new IllegalArgumentException("El usuario ya pertenece a este grupo.");
            }

            if (invitationRepository.existsByReceiverAndGroupAndGroupInvitation(receiver, group, true)) {
                throw new IllegalArgumentException("El usuario ya tiene una invitación a este grupo.");
            }
    
            invitation.setGroup(group);
    
        } else {
            //Si es invitacion de amistad
            if (userRepository.areUsersFriends(creator, receiver)) {
                throw new IllegalArgumentException("Ya sois amigos.");
            }

            if (invitationRepository.existsByReceiverAndSenderAndGroupInvitation(receiver, creator, false)) {
                throw new IllegalArgumentException("Ya has enviado una invitación de amistad a este usuario.");
            }
    
            invitation.setGroup(null);
        }
    
        return invitationRepository.save(invitation);
    }
    
    @Transactional
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

            Group group = groupService.findById(invitation.getGroup().getId());
    
            if (groupService.isMemberOfGroup(user, group)) {
                throw new IllegalArgumentException("El usuario ya pertenece a este grupo.");
            }
            groupService.joinGroup(user, group);

        } else {
            User sender = invitation.getSender();
            if (user.getFriends().contains(sender)) {
                throw new IllegalArgumentException("Ya sois amigos.");
            }
            user.addFriend(sender);
        }

        invitationRepository.delete(invitation);
    }

    @Transactional
    public void rejectInvitation(Integer invitationId, User user) throws IllegalArgumentException {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado la invitación."));

        if (!invitation.getReceiver().equals(user)) {
            throw new IllegalArgumentException("No puedes rechazar una invitación que no es para ti.");
        }

        invitationRepository.delete(invitation);
    }

}
