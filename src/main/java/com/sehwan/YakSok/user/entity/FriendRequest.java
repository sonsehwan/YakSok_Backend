package com.sehwan.YakSok.user.entity;

import com.sehwan.YakSok.user.FriendRequestId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "Friend")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(FriendRequestId.class)
public class FriendRequest implements Serializable {
    @Id
    private long userId;
    @Id
    private long friendId;
    private long

}
