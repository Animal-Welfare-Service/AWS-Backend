package com.clova.anifriends.domain.recruitment.repository;

import static com.clova.anifriends.domain.applicant.wrapper.ApplicantStatus.ATTENDANCE;
import static com.clova.anifriends.domain.shelter.support.ShelterFixture.shelter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.clova.anifriends.base.BaseRepositoryTest;
import com.clova.anifriends.domain.applicant.Applicant;
import com.clova.anifriends.domain.recruitment.Recruitment;
import com.clova.anifriends.domain.recruitment.support.fixture.RecruitmentFixture;
import com.clova.anifriends.domain.shelter.Shelter;
import com.clova.anifriends.domain.shelter.support.ShelterFixture;
import com.clova.anifriends.domain.volunteer.Volunteer;
import com.clova.anifriends.domain.volunteer.support.VolunteerFixture;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

class RecruitmentRepositoryTest extends BaseRepositoryTest {

    @Nested
    @DisplayName("findCompletedRecruitments 메서드 실행 시")
    class FindCompletedRecruitmentsResponseTestResponse {

        @BeforeEach
        void setUp() {
            List<Shelter> shelters = ShelterFixture.createShelters(20);
            List<Recruitment> recruitments = RecruitmentFixture.createRecruitments(shelters);
            shelters.forEach(shelter -> entityManager.persist(shelter));
            recruitments.forEach(recruitment -> entityManager.persist(recruitment));
        }

        @Test
        @DisplayName("성공")
        void findCompletedRecruitments() {
            //given
            Shelter shelter = ShelterFixture.shelter();
            Recruitment expected = RecruitmentFixture.recruitment(shelter);
            Volunteer volunteer = VolunteerFixture.volunteer();
            entityManager.persist(shelter);
            entityManager.persist(expected);
            entityManager.persist(volunteer);
            Applicant applicant = new Applicant(expected, volunteer);
            ReflectionTestUtils.setField(applicant, "status", ATTENDANCE);
            entityManager.persist(applicant);
            PageRequest pageRequest = PageRequest.of(0, 10);

            //when
            Page<Recruitment> recruitments = recruitmentRepository.findCompletedRecruitments(
                volunteer.getVolunteerId(), pageRequest);

            //then
            assertThat(recruitments).hasSize(1);
            Recruitment recruitment = recruitments.getContent().get(0);
            assertThat(recruitment).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("findRecruitments 메서드 실행 시")
    class FindRecruitmentsTest {

        //todo: 다양한 케이스에 대한 테스트를 작성할 것
        @Test
        @DisplayName("성공: 모든 인자가 null")
        void findRecruitmentsWhenArgsAreNull() {
            //given
            Shelter shelter = ShelterFixture.shelter();
            Recruitment recruitment = RecruitmentFixture.recruitment(shelter);
            PageRequest pageRequest = PageRequest.of(0, 10);
            shelterRepository.save(shelter);
            recruitmentRepository.save(recruitment);

            //when
            Page<Recruitment> recruitments = recruitmentRepository.findRecruitments(null, null,
                null, null, false, false, false,
                pageRequest);

            //then
            assertThat(recruitments.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("성공: 모든 인자가 주어졌을 때")
        void findRecruitmentsWhenArgsAreNotNull() {
            //given
            Shelter shelter = ShelterFixture.shelter();
            Recruitment recruitment = RecruitmentFixture.recruitment(shelter);
            PageRequest pageRequest = PageRequest.of(0, 10);
            String keyword = shelter.getName();
            LocalDate dateCondition = recruitment.getStartTime().toLocalDate();
            boolean isClosed = false;
            boolean titleFilter = true;
            boolean contentFilter = true;
            boolean shelterNameFilter = true;
            shelterRepository.save(shelter);
            recruitmentRepository.save(recruitment);

            //when
            Page<Recruitment> recruitments = recruitmentRepository.findRecruitments(keyword,
                dateCondition, dateCondition, isClosed, titleFilter, contentFilter,
                shelterNameFilter, pageRequest);

            //then
            assertThat(recruitments.getTotalElements()).isEqualTo(1);
            Recruitment findRecruitment = recruitments.getContent().get(0);
            assertThat(findRecruitment.getTitle()).isEqualTo(recruitment.getTitle());
        }
    }

    @Nested
    @DisplayName("findRecruitmentsByShelterOrderByCreatedAt 메서드 실행 시")
    class FindRecruitmentsByShelterOrderByCreatedAtTest {

        @Test
        @DisplayName("성공: 모든 인자가 주어졌을 때")
        void FindRecruitmentsByShelterOrderByCreatedAt() {
            // given
            Shelter shelter = shelter();
            setField(shelter, "shelterId", 1L);

            shelterRepository.save(shelter);

            Recruitment recruitment1 = new Recruitment(
                shelter,
                "a",
                10,
                "d",
                LocalDateTime.now().plusMonths(2),
                LocalDateTime.now().plusMonths(2).plusHours(3),
                LocalDateTime.now().plusDays(1),
                List.of()
            );

            Recruitment recruitment2 = new Recruitment(
                shelter,
                "ab",
                10,
                "de",
                LocalDateTime.now().plusMonths(3),
                LocalDateTime.now().plusMonths(3).plusHours(3),
                LocalDateTime.now().plusMonths(1),
                List.of()
            );
            recruitment2.closeRecruitment();

            Recruitment recruitment3 = new Recruitment(
                shelter,
                "abc",
                10,
                "def",
                LocalDateTime.now().plusMonths(4),
                LocalDateTime.now().plusMonths(4).plusHours(3),
                LocalDateTime.now().plusMonths(2),
                List.of()
            );

            recruitmentRepository.saveAll(List.of(recruitment1, recruitment2, recruitment3));

            String keyword = "ab";
            LocalDate startDate = LocalDate.now().plusMonths(3);
            LocalDate endDate = LocalDate.now().plusMonths(5);
            boolean content = true;
            boolean title = true;
            PageRequest pageable = PageRequest.of(0, 10);

            // when
            Page<Recruitment> recruitments = recruitmentRepository.findRecruitmentsByShelterOrderByCreatedAt(
                shelter.getShelterId(),
                keyword,
                startDate,
                endDate,
                content,
                title,
                pageable
            );

            // then
            assertThat(recruitments).contains(recruitment2, recruitment3);
        }

        @Test
        @DisplayName("성공: 모든 인자가 null")
        void findRecruitmentsWhenArgsAreNull() {
            // given
            Shelter shelter = shelter();
            setField(shelter, "shelterId", 1L);

            shelterRepository.save(shelter);

            Recruitment recruitment1 = new Recruitment(
                shelter,
                "a",
                10,
                "d",
                LocalDateTime.now().plusMonths(2),
                LocalDateTime.now().plusMonths(2).plusHours(3),
                LocalDateTime.now().plusDays(1),
                List.of()
            );

            Recruitment recruitment2 = new Recruitment(
                shelter,
                "ab",
                10,
                "de",
                LocalDateTime.now().plusMonths(3),
                LocalDateTime.now().plusMonths(3).plusHours(3),
                LocalDateTime.now().plusMonths(1),
                List.of()
            );
            recruitment2.closeRecruitment();

            Recruitment recruitment3 = new Recruitment(
                shelter,
                "abc",
                10,
                "def",
                LocalDateTime.now().plusMonths(4),
                LocalDateTime.now().plusMonths(4).plusHours(3),
                LocalDateTime.now().plusMonths(2),
                List.of()
            );

            recruitmentRepository.saveAll(List.of(recruitment1, recruitment2, recruitment3));

            PageRequest pageable = PageRequest.of(0, 10);

            // when
            Page<Recruitment> recruitments = recruitmentRepository.findRecruitmentsByShelterOrderByCreatedAt(
                shelter.getShelterId(),
                null,
                null,
                null,
                null,
                null,
                pageable
            );

            // then
            assertThat(recruitments).contains(recruitment1, recruitment2, recruitment3);
        }
    }

    @Nested
    @DisplayName("findRecruitmentsByShelterId 메서드 실행 시")
    class FindRecruitmentsByShelterIdTest {

        @Test
        @DisplayName("성공")
        void findRecruitmentsByShelterId() {
            // given
            Shelter shelter = shelter();
            setField(shelter, "shelterId", 1L);

            shelterRepository.save(shelter);

            Recruitment recruitment1 = new Recruitment(
                shelter,
                "a",
                10,
                "d",
                LocalDateTime.now().plusMonths(2),
                LocalDateTime.now().plusMonths(2).plusHours(3),
                LocalDateTime.now().plusDays(1),
                List.of()
            );

            Recruitment recruitment2 = new Recruitment(
                shelter,
                "ab",
                10,
                "de",
                LocalDateTime.now().plusMonths(3),
                LocalDateTime.now().plusMonths(3).plusHours(3),
                LocalDateTime.now().plusMonths(1),
                List.of()
            );

            Recruitment recruitment3 = new Recruitment(
                shelter,
                "abc",
                10,
                "def",
                LocalDateTime.now().plusMonths(4),
                LocalDateTime.now().plusMonths(4).plusHours(3),
                LocalDateTime.now().plusMonths(2),
                List.of()
            );

            recruitmentRepository.saveAll(List.of(recruitment1, recruitment2, recruitment3));

            PageRequest pageable = PageRequest.of(0, 10);

            // when
            Page<Recruitment> recruitments = recruitmentRepository.findRecruitmentsByShelterId(
                shelter.getShelterId(),
                pageable
            );

            // then
            assertThat(recruitments).contains(recruitment1, recruitment2, recruitment3);
        }
    }
}
