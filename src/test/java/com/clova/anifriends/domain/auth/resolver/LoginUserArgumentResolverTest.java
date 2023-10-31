package com.clova.anifriends.domain.auth.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clova.anifriends.base.BaseControllerTest;
import com.clova.anifriends.domain.auth.support.AuthFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

class LoginUserArgumentResolverTest extends BaseControllerTest {

    @Nested
    @DisplayName("@LoginUser를 사용하는 경우")
    class LoginUserTest {

        String accessToken = AuthFixture.accessToken();

        @Test
        @DisplayName("성공: 액세스 토큰이 포함되어 있으면")
        void loginUser() throws Exception {
            //given

            //when
            ResultActions resultActions = mockMvc.perform(get("/test/login-user")
                .header(AUTHORIZATION, accessToken));

            //then
            resultActions.andExpect(status().isOk())
                .andDo(print());
        }

        @Test
        @DisplayName("예외: 액세스 토큰이 포함되어 있지 않으면")
        void exceptionWhenNotContainsAccessToken() throws Exception {
            //given
            //when
            //then
            mockMvc.perform(get("/test/login-user"))
                .andExpect(
                    result -> {
                        Exception resolvedException = result.getResolvedException();
                        assertThat(resolvedException).isInstanceOf(
                            IllegalArgumentException.class);
                    }
                ).andDo(print());
        }
    }
}