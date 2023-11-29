package com.clova.anifriends.domain.applicant.service;

import com.clova.anifriends.domain.applicant.dto.FindApplicantsResponse;
import com.clova.anifriends.domain.applicant.dto.FindApplicantsResponse.FindApplicantResponse;
import com.clova.anifriends.domain.applicant.dto.response.FindApplyingVolunteersResponse;
import com.clova.anifriends.domain.applicant.dto.response.FindApplyingVolunteersResponse.FindApplyingVolunteerResponse;
import com.clova.anifriends.domain.applicant.repository.response.FindApplicantResult;
import com.clova.anifriends.domain.applicant.repository.response.FindApplyingVolunteerResult;
import com.clova.anifriends.domain.recruitment.Recruitment;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApplicantMapper {

    public static FindApplyingVolunteersResponse resultToResponse(
        List<FindApplyingVolunteerResult> findApplyingVolunteers) {
        List<FindApplyingVolunteerResponse> responses = findApplyingVolunteers.stream()
            .map(result -> new FindApplyingVolunteerResponse(
                result.getShelterId(),
                result.getRecruitmentId(),
                result.getApplicantId(),
                result.getRecruitmentTitle(),
                result.getShelterName(),
                result.getApplicantStatus(),
                result.getApplicantIsWritedReview(),
                result.getRecruitmentStartTime()))
            .toList();
        return new FindApplyingVolunteersResponse(responses);
    }

    public static FindApplicantsResponse resultToResponse(
        List<FindApplicantResult> findApplicants, Recruitment recruitment) {
        List<FindApplicantResponse> responses = findApplicants.stream()
            .map(result -> new FindApplicantResponse(
                result.getVolunteerId(),
                result.getApplicantId(),
                result.getVolunteerName(),
                result.getVolunteerBirthDate(),
                result.getVolunteerGender(),
                result.getCompletedVolunteerCount(),
                result.getVolunteerTemperature(),
                result.getApplicantStatus().convertToApprovalStatus().name()
            ))
            .toList();
        return new FindApplicantsResponse(responses, recruitment.getCapacity());
    }
}
