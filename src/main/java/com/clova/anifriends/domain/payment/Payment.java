package com.clova.anifriends.domain.payment;

import static java.util.Objects.isNull;

import com.clova.anifriends.domain.common.BaseTimeEntity;
import com.clova.anifriends.domain.donation.Donation;
import com.clova.anifriends.domain.donation.exception.DonationNotFoundException;
import com.clova.anifriends.domain.payment.vo.PaymentStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id
    @Column(name = "payment_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @OneToOne(cascade = CascadeType.ALL)
    private Donation donation;

    @Column(name = "payment_key")
    private String paymentKey;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "order_name")
    private String orderName;

    @Column(name = "status")
    private PaymentStatus status = PaymentStatus.PENDING;

    public Payment(Donation donation) {
        validateDonation(donation);

        this.donation = donation;
        this.orderId = UUID.randomUUID();
        this.orderName = generateOrderName(donation);
    }

    private void validateDonation(Donation donation) {
        if (isNull(donation)) {
            throw new DonationNotFoundException("후원 정보가 존재하지 않습니다.");
        }
    }

    private String generateOrderName(Donation donation) {
        return donation.getShelter().getName() + " 후원금";
    }
}
