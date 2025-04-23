package com.groupandplay.user;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.groupandplay.game.Game;
import com.groupandplay.group.Group;
import com.groupandplay.model.Person;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends Person implements UserDetails {
    
    @ManyToMany(fetch = FetchType.EAGER)    
    @JoinTable(
        name = "users_groups",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private Set<Group> groups;

    @Column(name = "role", nullable = false)
    @NotBlank
    private String role;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl = "/resources/images/defecto.png";
    
    @ManyToOne
    @JoinColumn(name = "fav_game_id")
    private Game favGame;

    @Override
    public Collection< ? extends GrantedAuthority> getAuthorities() {

        return List.of( new SimpleGrantedAuthority(role));
    }
}
