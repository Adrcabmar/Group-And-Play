package com.groupandplay.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.groupandplay.invitation.Invitation;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InvitationDTO {
    private Integer id;
    @NotNull
    private Boolean groupInvitation;
    private String senderUsername;
    private String receiverUsername;
    private String date;
    private Integer groupId;
    private String gameName;

    public InvitationDTO(Invitation invitation) {
        this.id = invitation.getId();

        this.groupInvitation = invitation.getGroupInvitation();

        this.senderUsername = invitation.getSender().getUsername();

        this.receiverUsername = invitation.getReceiver().getUsername();

        this.date = invitation.getDate().toString();

        this.groupId = invitation.getGroup() != null ? invitation.getGroup().getId() : null;

        this.gameName = invitation.getGroup() != null ? invitation.getGroup().getGame().getName() : null;

    }

    public static List<InvitationDTO> fromEntities(List<Invitation> invitations) {
    return invitations.stream()
                      .map(InvitationDTO::new)
                      .collect(Collectors.toList());
}
}

