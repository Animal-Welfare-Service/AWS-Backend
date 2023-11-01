package com.clova.anifriends.domain.volunteer.support;

import com.clova.anifriends.domain.volunteer.dto.request.RegisterVolunteerRequest;
import com.clova.anifriends.domain.volunteer.dto.response.GetVolunteerMyPageResponse;
import com.clova.anifriends.domain.volunteer.wrapper.VolunteerGender;
import java.time.LocalDate;

public class VolunteerDtoFixture {

    private static final Long VOLUNTEER_ID = 1L;
    private static final String EMAIL = "asdf@gmail.com";
    private static final String PASSWORD = "asdf1234";
    private static final String BIRTH_DATE = "1999-03-23";
    private static final String PHONE_NUMBER = "01012345678";
    private static final String GENDER = VolunteerGender.MALE.getName();
    private static final Integer TEMPERATURE = 36;
    private static final String NAME = "김봉사";
    private static final String IMAGE_URL = "image/url";
    private static final Long VOLUNTEER_COUNT = 2L;

    public static RegisterVolunteerRequest registerVolunteerRequest() {
        return new RegisterVolunteerRequest(EMAIL, PASSWORD, NAME, BIRTH_DATE, PHONE_NUMBER,
            GENDER);
    }

    public static GetVolunteerMyPageResponse getVolunteerMyPageResponse() {
        return new GetVolunteerMyPageResponse(EMAIL, NAME, LocalDate.parse(BIRTH_DATE),
            PHONE_NUMBER, TEMPERATURE,
            VOLUNTEER_COUNT, IMAGE_URL);
    }
}
