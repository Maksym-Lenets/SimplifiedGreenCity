package greencity.controller;

import greencity.annotations.CurrentUser;
import greencity.constant.HttpStatuses;
import greencity.dto.user.UserVO;
import greencity.service.FriendService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@Validated
@AllArgsConstructor
@RestController
@RequestMapping("/friends")
public class FriendController {
    private final FriendService friendService;

    /**
     * Method for deleting user's friend.
     *
     * @param friendId id user friend.
     * @param userVO   {@link UserVO} user.
     * @author Marian Datsko
     */
    @ApiOperation(value = "Delete user's friend")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @DeleteMapping("/{friendId}")
    public ResponseEntity<ResponseEntity.BodyBuilder> deleteUserFriend(
        @ApiParam("Id friend of current user. Cannot be empty.") @PathVariable Long friendId,
        @ApiIgnore @CurrentUser UserVO userVO) {
        friendService.deleteUserFriendById(userVO.getId(), friendId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for add new user friend.
     *
     * @param friendId id user friend.
     * @param userVO   {@link UserVO} user.
     * @author Marian Datsko
     */
    @ApiOperation(value = "Add new user friend")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND),
    })
    @PostMapping("/{friendId}")
    public ResponseEntity<ResponseEntity.BodyBuilder> addNewFriend(
        @ApiParam("Id friend of current user. Cannot be empty.") @PathVariable Long friendId,
        @ApiIgnore @CurrentUser UserVO userVO) {
        friendService.addNewFriend(userVO.getId(), friendId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}