package com.clova.anifriends.domain.volunteer.service;

import com.clova.anifriends.domain.common.ImageRemover;
import com.clova.anifriends.domain.volunteer.Volunteer;
import com.clova.anifriends.domain.volunteer.dto.response.CheckDuplicateVolunteerEmailResponse;
import com.clova.anifriends.domain.volunteer.dto.response.FindVolunteerMyPageResponse;
import com.clova.anifriends.domain.volunteer.dto.response.FindVolunteerProfileResponse;
import com.clova.anifriends.domain.volunteer.exception.VolunteerNotFoundException;
import com.clova.anifriends.domain.volunteer.repository.VolunteerRepository;
import com.clova.anifriends.domain.volunteer.wrapper.VolunteerEmail;
import com.clova.anifriends.domain.volunteer.wrapper.VolunteerGender;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VolunteerService {

    private final VolunteerRepository volunteerRepository;
    private final ImageRemover imageRemover;

    @Transactional(readOnly = true)
    public CheckDuplicateVolunteerEmailResponse checkDuplicateVolunteerEmail(String email) {
        boolean isDuplicated = volunteerRepository.existsByEmail(new VolunteerEmail(email));
        return CheckDuplicateVolunteerEmailResponse.from(isDuplicated);
    }

    @Transactional
    public Long registerVolunteer(
        String email,
        String password,
        String name,
        String birthDate,
        String phoneNumber,
        String gender
    ) {
        Volunteer volunteer = new Volunteer(email, password, birthDate, phoneNumber, gender, name);

        volunteerRepository.save(volunteer);
        return volunteer.getVolunteerId();
    }

    @Transactional(readOnly = true)
    public FindVolunteerMyPageResponse findVolunteerMyPage(Long volunteerId) {
        return FindVolunteerMyPageResponse.from(getVolunteer(volunteerId));
    }

    @Transactional(readOnly = true)
    public FindVolunteerProfileResponse findVolunteerProfile(
        Long volunteerId
    ) {
        Volunteer foundVolunteer = getVolunteer(volunteerId);

        return FindVolunteerProfileResponse.from(
            foundVolunteer
        );
    }

    private Volunteer getVolunteer(Long volunteerId) {
        return volunteerRepository.findById(volunteerId)
            .orElseThrow(() -> new VolunteerNotFoundException("존재하지 않는 봉사자입니다."));
    }

    @Transactional
    public void updateVolunteerInfo(
        Long volunteerId,
        String name,
        VolunteerGender gender,
        LocalDate birthDate,
        String phoneNumber,
        String imageUrl) {
        Volunteer volunteer = getVolunteer(volunteerId);
        Volunteer updatedVolunteer = volunteer.updateVolunteerInfo(
            name,
            gender,
            birthDate,
            phoneNumber,
            imageUrl,
            imageRemover);
        volunteerRepository.save(updatedVolunteer);
    }
}
