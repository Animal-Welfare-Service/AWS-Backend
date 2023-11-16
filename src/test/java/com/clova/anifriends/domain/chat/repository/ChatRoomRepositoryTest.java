package com.clova.anifriends.domain.chat.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.clova.anifriends.base.BaseRepositoryTest;
import com.clova.anifriends.domain.auth.jwt.UserRole;
import com.clova.anifriends.domain.chat.ChatMessage;
import com.clova.anifriends.domain.chat.ChatRoom;
import com.clova.anifriends.domain.chat.repository.response.FindChatRoomResult;
import com.clova.anifriends.domain.shelter.Shelter;
import com.clova.anifriends.domain.shelter.support.ShelterFixture;
import com.clova.anifriends.domain.volunteer.Volunteer;
import com.clova.anifriends.domain.volunteer.support.VolunteerFixture;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ChatRoomRepositoryTest extends BaseRepositoryTest {

    @Nested
    @DisplayName("findChatRoomsByVolunteer 메서드 실행 시")
    class FindChatRoomsByVolunteerTest {

        Volunteer volunteer;
        List<Shelter> shelters;
        List<ChatRoom> chatRooms;

        @BeforeEach
        void setUp() {
            volunteer = VolunteerFixture.volunteer();
            shelters = ShelterFixture.shelters(5);
            chatRooms = shelters.stream()
                .map(shelter -> new ChatRoom(volunteer, shelter))
                .toList();
        }

        @Test
        @DisplayName("성공")
        void findChatRoomsByVolunteer() {
            //given
            LocalDateTime now = LocalDateTime.now();
            shelterRepository.saveAll(shelters);
            volunteerRepository.save(volunteer);
            chatRoomRepository.saveAll(chatRooms);
            List<ChatMessage> oldMessages = IntStream.range(0, shelters.size())
                .mapToObj(i -> new ChatMessage(chatRooms.get(i), shelters.get(i).getShelterId(),
                    UserRole.ROLE_SHELTER, "첫 번째"))
                .toList();
            List<ChatMessage> secondOldMessages = IntStream.range(0, shelters.size())
                .mapToObj(i -> new ChatMessage(chatRooms.get(i), shelters.get(i).getShelterId(),
                    UserRole.ROLE_SHELTER, "두 번째"))
                .toList();
            List<ChatMessage> recentMessages = IntStream.range(0, shelters.size())
                .mapToObj(i -> new ChatMessage(chatRooms.get(i), shelters.get(i).getShelterId(),
                    UserRole.ROLE_SHELTER, "세 번째"))
                .toList();
            chatMessageRepository.saveAll(oldMessages);
            chatMessageRepository.saveAll(secondOldMessages);
            chatMessageRepository.saveAll(recentMessages);
            oldMessages.forEach(message -> ReflectionTestUtils.setField(message, "createdAt",
                now.minusDays(2)));
            secondOldMessages.forEach(message -> ReflectionTestUtils.setField(message, "createdAt",
                now.minusDays(1)));
            entityManager.flush();

            //when
            List<FindChatRoomResult> chatRooms
                = chatRoomRepository.findChatRoomsByVolunteer(volunteer);

            //then
            assertThat(chatRooms)
                .hasSize(5)
                .allSatisfy(chatRoom -> {
                    assertThat(chatRoom.getCreatedAt())
                        .isCloseTo(now, within(5, ChronoUnit.SECONDS));
                });
        }
    }
}
