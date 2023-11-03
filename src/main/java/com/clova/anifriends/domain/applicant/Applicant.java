package com.clova.anifriends.domain.applicant;

import com.clova.anifriends.domain.applicant.exception.ApplicantBadRequestException;
import com.clova.anifriends.domain.applicant.exception.ApplicantConflictException;
import com.clova.anifriends.domain.applicant.wrapper.ApplicantStatus;
import com.clova.anifriends.domain.common.BaseTimeEntity;
import com.clova.anifriends.domain.recruitment.Recruitment;
import com.clova.anifriends.domain.review.Review;
import com.clova.anifriends.domain.volunteer.Volunteer;
import com.clova.anifriends.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "applicant")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Applicant extends BaseTimeEntity {

    @Id
    @Column(name = "applicant_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitment_id")
    private Recruitment recruitment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id")
    private Volunteer volunteer;

    @OneToOne(mappedBy = "applicant", fetch = FetchType.LAZY)
    private Review review;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ApplicantStatus status;

    public Applicant(
        Recruitment recruitment,
        Volunteer volunteer
    ) {
        validateRecruitment(recruitment);
        checkConcurrency(recruitment);
        this.recruitment = recruitment;
        validateVolunteer(volunteer);
        this.volunteer = volunteer;
        this.status = ApplicantStatus.PENDING;
    }

    public ApplicantStatus getStatus() {
        return status;
    }

    public Recruitment getRecruitment() {
        return recruitment;
    }

    public Volunteer getVolunteer() {
        return volunteer;
    }

    private void validateRecruitment(Recruitment recruitment) {
        if (recruitment == null) {
            throw new ApplicantBadRequestException("봉사는 필수 입력 항목입니다.");
        }
        if (recruitment.isClosed() || recruitment.getDeadline().isBefore(LocalDateTime.now())) {
            throw new ApplicantBadRequestException("모집이 마감된 봉사입니다.");
        }
    }

    private void validateVolunteer(Volunteer volunteer) {
        if (volunteer == null) {
            throw new ApplicantBadRequestException("봉사자는 필수 입력 항목입니다.");
        }
    }

    private void checkConcurrency(Recruitment recruitment) {
        if (recruitment.getApplicantCount() >= recruitment.getCapacity()) {
            throw new ApplicantConflictException(ErrorCode.CONCURRENCY, "모집 인원이 초과되었습니다.");
        }
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public boolean hasReview() {
        return review != null;
    }
}
