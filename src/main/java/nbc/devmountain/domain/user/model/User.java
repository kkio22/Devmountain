package nbc.devmountain.domain.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "User")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 255)
    private String name;

    @Column(name = "phone_number", length = 255)
    private String phoneNumber;

    private LocalDateTime deletedAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_level")
    private MembershipLevel membershipLevel;

    @Builder
    public User(String email, String password, String name, String phoneNumber, LoginType loginType, Role role, MembershipLevel membershipLevel) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.loginType = loginType;
        this.role = role;
        this.membershipLevel = membershipLevel;
    }

    public enum LoginType {
        GOOGLE, KAKAO, NAVER, EMAIL
    }

    public enum Role {
        USER, ADMIN
    }

    public enum MembershipLevel {
        FREE, PRO
    }
}


