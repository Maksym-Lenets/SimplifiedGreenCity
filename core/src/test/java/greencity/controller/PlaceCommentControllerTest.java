package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.dto.comment.AddCommentDto;
import greencity.dto.user.UserVO;
import greencity.entity.User;
import greencity.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.security.Principal;
import static greencity.ModelUtils.getUserVO;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PlaceCommentControllerTest {

    private MockMvc mockMvc;

    private AddCommentDto addCommentDto;
    private static final String placeCommentLinkFirstPart = "/place";
    private static final String placeCommentLinkSecondPart = "/comments";

    private static final String content = "{\n" +
        "  \"estimate\": {\n" +
        "    \"rate\": 1\n" +
        "  },\n" +
        "  \"photos\": [\n" +
        "    {\n" +
        "      \"name\": \"string\"\n" +
        "    }\n" +
        "  ],\n" +
        "  \"text\": \"string\"\n" +
        "}";

    private static final String getContent = "{\n" +
        "  \"estimate\": {\n" +
        "    \"rate\": 1\n" +
        "  },\n" +
        "  \"photos\": [\n" +
        "    {\n" +
        "      \"name\": \"test\"\n" +
        "    }\n" +
        "  ],\n" +
        "  \"text\": \"test\"\n" +
        "}";

    @Test
    void saveTest() throws Exception {
        Principal principal = ModelUtils.getPrincipal();
        User user = ModelUtils.getUser();
        UserVO userVO = getUserVO();

        user.setUserStatus(UserStatus.ACTIVATED);
        userVO.setUserStatus(UserStatus.ACTIVATED);

        mockMvc.perform(post(placeCommentLinkFirstPart + "/{placeId}" +
            placeCommentLinkSecondPart, 1)
                .principal(principal)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());

        ObjectMapper mapper = new ObjectMapper();
        addCommentDto = mapper.readValue(content, AddCommentDto.class);
    }

    @Test
    void saveBadRequestTest() throws Exception {
        mockMvc.perform(post(placeCommentLinkFirstPart + "/{placeId}" +
            placeCommentLinkSecondPart, 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void findAllTest() throws Exception {
        int pageNumber = 5;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        mockMvc.perform(get(placeCommentLinkSecondPart + "?page=5"))
            .andExpect(status().isOk());
    }

    @Test
    void findByIdTest() throws Exception {
        mockMvc.perform(get(placeCommentLinkSecondPart + "/{id}", 1))
            .andExpect(status().isOk());

    }

    @Test
    void findByIdFailedTest() throws Exception {
        mockMvc.perform(get(placeCommentLinkSecondPart + "/{id}", "invalidID"))
            .andExpect(status().isBadRequest());

    }

    @Test
    void deleteByIdTest() throws Exception {
        this.mockMvc.perform(delete(placeCommentLinkSecondPart + "?id={id}", 1))
            .andExpect(status().isOk());

    }

    @Test
    void deleteByIdFailedTest() throws Exception {
        mockMvc.perform(delete(placeCommentLinkSecondPart + "?id={id}", "invalidID"))
            .andExpect(status().isBadRequest());

    }
}
