package com.groupandplay.invitation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.groupandplay.group.Group;
import com.groupandplay.user.User;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Integer> {

    Page<Invitation> findAllByReceiver(User receiver, Pageable pageable);

    void deleteAllByGroup(Group group);

    boolean existsByReceiverAndGroupAndGroupInvitation(User receiver, Group group, boolean groupInvitation);

    boolean existsByReceiverAndSenderAndGroupInvitation(User receiver, User sender, boolean groupInvitation);

    Page<Invitation> findAllByReceiverAndGroupInvitation(User receiver, boolean groupInvitation, Pageable pageable);

}
