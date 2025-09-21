package com.ecom.validation.entity;

import com.ecom.validation.model.User;
import com.ecom.validation.model.UserDevices;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "validation")
public class Validation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Instant creatAt;
    private Instant expireAt;
    private Boolean active = false;
    private String code;
    private String email;
    private String username;
    private String type;
    @Transient
    private User user;
    private Long userId;
    @Transient
    private UserDevices userdevices;
    private Long deviceId;

}
