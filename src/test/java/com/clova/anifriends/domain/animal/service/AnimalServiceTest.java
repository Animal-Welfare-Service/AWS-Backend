package com.clova.anifriends.domain.animal.service;

import static com.clova.anifriends.domain.animal.support.fixture.AnimalFixture.animal;
import static com.clova.anifriends.domain.shelter.support.ShelterFixture.shelter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

import com.clova.anifriends.base.MockImageRemover;
import com.clova.anifriends.domain.animal.Animal;
import com.clova.anifriends.domain.animal.AnimalAge;
import com.clova.anifriends.domain.animal.AnimalSize;
import com.clova.anifriends.domain.animal.dto.request.RegisterAnimalRequest;
import com.clova.anifriends.domain.animal.dto.response.FindAnimalDetail;
import com.clova.anifriends.domain.animal.dto.response.FindAnimalsByShelterResponse;
import com.clova.anifriends.domain.animal.dto.response.FindAnimalsByVolunteerResponse;
import com.clova.anifriends.domain.animal.exception.AnimalNotFoundException;
import com.clova.anifriends.domain.animal.repository.AnimalRepository;
import com.clova.anifriends.domain.animal.support.fixture.AnimalFixture;
import com.clova.anifriends.domain.animal.wrapper.AnimalActive;
import com.clova.anifriends.domain.animal.wrapper.AnimalGender;
import com.clova.anifriends.domain.animal.wrapper.AnimalType;
import com.clova.anifriends.domain.common.ImageRemover;
import com.clova.anifriends.domain.shelter.Shelter;
import com.clova.anifriends.domain.shelter.exception.ShelterNotFoundException;
import com.clova.anifriends.domain.shelter.repository.ShelterRepository;
import com.clova.anifriends.domain.shelter.support.ShelterFixture;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    @InjectMocks
    AnimalService animalService;

    @Mock
    AnimalRepository animalRepository;

    @Mock
    ShelterRepository shelterRepository;

    @Spy
    ImageRemover imageRemover = new MockImageRemover();

    @Nested
    @DisplayName("registerAnimal 메서드 실행 시")
    class RegisterAnimalTest {

        Shelter shelter = ShelterFixture.shelter();
        List<String> imageUrls = List.of("www.aws.s3.com/2", "www.aws.s3.com/3");
        RegisterAnimalRequest registerAnimalRequest = new RegisterAnimalRequest(
            "name",
            LocalDate.now(),
            AnimalType.DOG.getName(),
            "품종",
            AnimalGender.FEMALE.getName(),
            false,
            AnimalActive.QUIET.getName(),
            0.7,
            "기타 정보",
            imageUrls
        );

        @Test
        @DisplayName("성공")
        void registerAnimal() {
            //given
            given(shelterRepository.findById(anyLong())).willReturn(Optional.ofNullable(shelter));

            //when
            animalService.registerAnimal(1L, registerAnimalRequest);

            //then
            then(animalRepository).should().save(any());
            then(animalRepository).should().save(any());
        }

        @Test
        @DisplayName("예외(ShelterNotFoundException): 존재하지 않는 보호소")
        void exceptionWhenShelterNotFound() {
            //given
            //when
            Exception exception = catchException(
                () -> animalService.registerAnimal(1L, registerAnimalRequest));

            //then
            assertThat(exception).isInstanceOf(ShelterNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findAnimalDetail 실행 시")
    class FindAnimalDetailTest {

        @Test
        @DisplayName("성공")
        void findAnimalDetail() {
            // given
            Shelter shelter = shelter();
            Animal animal = animal(shelter);
            FindAnimalDetail expected = FindAnimalDetail.from(animal);

            when(animalRepository.findById(anyLong())).thenReturn(Optional.of(animal));

            // when
            FindAnimalDetail result = animalService.findAnimalDetail(
                anyLong());

            // then
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
        }

        @Test
        @DisplayName("예외(NotFoundAnimalException): 존재하지 않는 보호 동물")
        void exceptionWhenAnimalIsNotExist() {
            // given
            when(animalRepository.findById(anyLong())).thenReturn(Optional.empty());

            // when
            Exception exception = catchException(
                () -> animalService.findAnimalDetail(anyLong()));

            // then
            assertThat(exception).isInstanceOf(AnimalNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findAnimalsByShelter 실행 시")
    class FindAnimalsByShelterTest {

        @Test
        @DisplayName("성공")
        void findAnimalsByShelter() {
            // given
            Long shelterId = 1L;
            String keyword = "animalName";
            AnimalType type = AnimalType.DOG;
            AnimalGender gender = AnimalGender.MALE;
            Boolean isNeutered = true;
            AnimalActive active = AnimalActive.ACTIVE;
            AnimalSize size = AnimalSize.SMALL;
            AnimalAge age = AnimalAge.BABY;
            Shelter shelter = shelter();
            Animal animal = animal(shelter);
            PageRequest pageRequest = PageRequest.of(0, 10);
            Page<Animal> pageResult = new PageImpl<>(List.of(animal));
            FindAnimalsByShelterResponse expected = FindAnimalsByShelterResponse.from(pageResult);

            given(
                animalRepository.findAnimalsByShelter(shelterId, keyword, type, gender, isNeutered,
                    active, size, age, pageRequest))
                .willReturn(pageResult);

            // when
            FindAnimalsByShelterResponse animalsByShelter = animalService.findAnimalsByShelter(
                shelterId, keyword, type, gender, isNeutered,
                active, size, age, pageRequest);

            // then
            assertThat(expected).usingRecursiveComparison().isEqualTo(animalsByShelter);
        }
    }

    @Nested
    @DisplayName("findAnimalsByVolunteer 실행 시")
    class FindAnimalsByVolunteer {

        @Test
        @DisplayName("성공")
        void findAnimalsByVolunteer() {
            // given
            String mockName = "animalName";
            String mockInformation = "animalInformation";
            String mockBreed = "animalBreed";
            List<String> mockImageUrls = List.of("www.aws.s3.com/2");

            AnimalType typeFilter = AnimalType.DOG;
            AnimalActive activeFilter = AnimalActive.ACTIVE;
            Boolean isNeuteredFilter = true;
            AnimalAge ageFilter = AnimalAge.ADULT;
            AnimalGender genderFilter = AnimalGender.MALE;
            AnimalSize sizeFilter = AnimalSize.MEDIUM;

            Shelter shelter = ShelterFixture.shelter();

            Animal matchAnimal = new Animal(
                shelter,
                mockName,
                LocalDate.now().minusMonths(ageFilter.getMinMonth()),
                typeFilter.getName(),
                mockBreed,
                genderFilter.getName(),
                isNeuteredFilter,
                activeFilter.getName(),
                sizeFilter.getMinWeight(),
                mockInformation,
                mockImageUrls
            );

            PageRequest pageRequest = PageRequest.of(0, 10);
            Page<Animal> pageResult = new PageImpl<>(List.of(matchAnimal), pageRequest, 1);

            FindAnimalsByVolunteerResponse expected = FindAnimalsByVolunteerResponse.from(
                pageResult);

            when(animalRepository.findAnimalsByVolunteer(typeFilter, activeFilter, isNeuteredFilter,
                ageFilter, genderFilter, sizeFilter, pageRequest))
                .thenReturn(pageResult);

            // when
            FindAnimalsByVolunteerResponse result = animalService.findAnimalsByVolunteer(
                typeFilter, activeFilter, isNeuteredFilter,
                ageFilter, genderFilter, sizeFilter, pageRequest);

            // then
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);

        }
    }

    @Nested
    @DisplayName("updateAnimalAdoptStatus 실행 시")
    class UpdateAnimalAdoptStatus {

        @Test
        @DisplayName("성공")
        void updateAnimalAdoptStatus() {
            // given
            Shelter shelter = ShelterFixture.shelter();
            boolean originStatus = true;
            boolean updateStatus = false;
            Animal animal = AnimalFixture.animal(shelter, originStatus);

            when(animalRepository.findByShelterIdAndAnimalId(anyLong(), anyLong()))
                .thenReturn(Optional.of(animal));

            // when
            Exception exception = catchException(
                () -> animalService.updateAnimalAdoptStatus(anyLong(), anyLong(), updateStatus));

            // then
            assertThat(exception).isNull();
        }
    }

    @Nested
    @DisplayName("updateAnimal 메서드 호출 시")
    class UpdateRecruitmentTest {

        Animal animal;

        @BeforeEach
        void setUp() {
            Shelter shelter = ShelterFixture.shelter();
            animal = AnimalFixture.animal(shelter);
        }

        @Test
        @DisplayName("성공")
        void updateRecruitment() {
            //given
            String mockName = "animalName";
            LocalDate mockBirthDate = LocalDate.now();
            String mockInformation = "animalInformation";
            String mockBreed = "animalBreed";
            List<String> mockImageUrls = List.of("www.aws.s3.com/2");

            AnimalType mockType = AnimalType.DOG;
            AnimalActive mockActive = AnimalActive.ACTIVE;
            Double mockWeight = 1.2;
            Boolean mockIsNeutered = true;
            AnimalGender mockGender = AnimalGender.MALE;

            given(animalRepository.findByAnimalIdAndShelterIdWithImages(anyLong(),
                anyLong())).willReturn(Optional.ofNullable(animal));

            //when
            animalService.updateAnimal(1L, 1L,
                mockName,
                mockBirthDate,
                mockType,
                mockBreed,
                mockGender,
                mockIsNeutered,
                mockActive,
                mockWeight,
                mockInformation,
                mockImageUrls);

            //then
            assertThat(animal.getName()).isEqualTo(mockName);
            assertThat(animal.getBirthDate()).isEqualTo(mockBirthDate);
            assertThat(animal.getType()).isEqualTo(mockType);
            assertThat(animal.getBreed()).isEqualTo(mockBreed);
            assertThat(animal.getGender()).isEqualTo(mockGender);
            assertThat(animal.isNeutered()).isEqualTo(mockIsNeutered);
            assertThat(animal.getActive()).isEqualTo(mockActive);
            assertThat(animal.getWeight()).isEqualTo(mockWeight);
            assertThat(animal.getInformation()).isEqualTo(mockInformation);
            assertThat(animal.getImages()).usingRecursiveComparison().isEqualTo(mockImageUrls);
        }

        @Test
        @DisplayName("예외(AnimalBadRequestException): 존재하지 않는 animalImage ID")
        void throwExceptionWhenAnimalImageIdIsNotExist() {
            //given
            String mockName = "animalName";
            LocalDate mockBirthDate = LocalDate.now();
            String mockInformation = "animalInformation";
            String mockBreed = "animalBreed";
            List<String> mockImageUrls = List.of("www.aws.s3.com/2");

            AnimalType mockType = AnimalType.DOG;
            AnimalActive mockActive = AnimalActive.ACTIVE;
            Double mockWeight = 1.2;
            Boolean mockIsNeutered = true;
            AnimalGender mockGender = AnimalGender.MALE;

            given(animalRepository.findByAnimalIdAndShelterIdWithImages(anyLong(),
                anyLong())).willReturn(Optional.empty());

            //when
            Exception exception = catchException(
                () -> animalService.updateAnimal(1L, 1L,
                    mockName,
                    mockBirthDate,
                    mockType,
                    mockBreed,
                    mockGender,
                    mockIsNeutered,
                    mockActive,
                    mockWeight,
                    mockInformation,
                    mockImageUrls));

            //then
            assertThat(exception).isInstanceOf(AnimalNotFoundException.class);
        }
    }
}
