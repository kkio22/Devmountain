package nbc.devmountain.domain.user.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc.devmountain.domain.category.model.UserCategory;

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

    // 카테고리 연관관계 추가
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "userCategoryId")
    private List<UserCategory> userCategory = new ArrayList<>();

    @Builder
    public User(String email, String password, String name, String phoneNumber, LoginType loginType, Role role, MembershipLevel membershipLevel, List<UserCategory> userCategory) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.loginType = loginType;
        this.role = role;
        this.membershipLevel = membershipLevel;
        this.userCategory = userCategory;
    }

    public void updateProfile(String password, String name, String phoneNumber, List<UserCategory> userCategory) {
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.userCategory = userCategory;
    }

    public enum LoginType {
        GOOGLE, KAKAO, NAVER, EMAIL, APPLE
    }

    public enum Role {
        USER, ADMIN
    }

    public enum MembershipLevel {
        FREE, PRO
    }
}


