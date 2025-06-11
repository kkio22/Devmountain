package nbc.devmountain.domain.order.model;

import jakarta.persistence.*;
import lombok.*;
import nbc.devmountain.domain.user.model.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "Payment")
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long payId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderID")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    @Column(length = 255)
    private String paymentKey;

    @Enumerated(EnumType.STRING)
    private Method method;

    @Enumerated(EnumType.STRING)
    private Result result;

    @Enumerated(EnumType.STRING)
    private MembershipLevel membershipLevelChangedTo;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Payment(Order order, User user, String paymentKey, Method method, Result result, MembershipLevel membershipLevelChangedTo) {
        this.order = order;
        this.user = user;
        this.paymentKey = paymentKey;
        this.method = method;
        this.result = result;
        this.membershipLevelChangedTo = membershipLevelChangedTo;
    }

    public enum Method {
        CARD, TRANSFER, PHONE, SIMPLE_PAYMENT
    }

    public enum Result {
        SUCCESS, FAIL
    }

    public enum MembershipLevel {
        FREE, PRO
    }
}

