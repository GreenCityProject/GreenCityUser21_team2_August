package greencity.controller;

import greencity.annotations.ApiPageable;
import greencity.annotations.CurrentUser;
import greencity.annotations.CurrentUserId;
import greencity.annotations.ImageValidation;
import greencity.constant.HttpStatuses;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.PageableDto;
import greencity.dto.filter.FilterUserDto;
import greencity.dto.shoppinglist.CustomShoppingListItemResponseDto;
import greencity.dto.ubs.UbsTableCreationDto;
import greencity.dto.user.*;
import greencity.enums.EmailNotification;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.service.EmailService;
import greencity.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
@Validated
@Slf4j
public class UserController {
    private final UserService userService;
    private final EmailService emailService;

    /**
     * The method which update user status. Parameter principal are ignored because
     * Spring automatically provide the Principal object.
     *
     * @param userStatusDto - dto with updated filed.
     * @return {@link UserStatusDto}
     * @author Rostyslav Khasanov
     */
    @Operation(summary = "Update status of user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = UserStatus.class))),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @PatchMapping("status")
    public ResponseEntity<UserStatusDto> updateStatus(
        @Valid @RequestBody UserStatusDto userStatusDto, @ApiIgnore Principal principal) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(
                userService.updateStatus(
                    userStatusDto.getId(), userStatusDto.getUserStatus(), principal.getName()));
    }

    /**
     * The method which update user role. Parameter principal are ignored because
     * Spring automatically provide the Principal object.
     *
     * @param id   of updated user
     * @param body contains new role
     * @return {@link UserRoleDto}
     * @author Rostyslav Khasanov
     */
    @Operation(summary = "Update role of user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = UserRoleDto.class))),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @PatchMapping("{id}/role")
    public ResponseEntity<UserRoleDto> updateRole(
        @PathVariable Long id,
        @NotNull @RequestBody Map<String, String> body,
        @ApiIgnore Principal principal) {
        Role role = Role.valueOf(body.get("role"));
        UserRoleDto userRoleDto = new UserRoleDto(id, role);
        return ResponseEntity.status(HttpStatus.OK)
            .body(
                userService.updateRole(
                    userRoleDto.getId(), userRoleDto.getRole(), principal.getName()));
    }

    /**
     * The method which return list of users by page. Parameter pageable ignored
     * because swagger ui shows the wrong params, instead they are explained in the
     * {@link ApiPageable}.
     *
     * @param pageable - pageable configuration.
     * @return list of {@link PageableDto}
     * @author Rostyslav Khasanov
     */
    @Operation(summary = "Get users by page")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = PageableDto.class))),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @ApiPageable
    @GetMapping("all")
    public ResponseEntity<PageableDto<UserForListDto>> getAllUsers(@ApiIgnore Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findByPage(pageable));
    }

    /**
     * The method which return array of existing roles.
     *
     * @return {@link RoleDto}
     * @author Rostyslav Khasanov
     */
    @Operation(summary = "Get all available roles")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = RoleDto.class))),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("roles")
    public ResponseEntity<RoleDto> getRoles() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getRoles());
    }

    /**
     * The method which return array of existing {@link EmailNotification}.
     *
     * @return {@link EmailNotification} array
     * @author Nazar Vladyka
     */
    @Operation(summary = "Get all available email notifications statuses")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = EmailNotification[].class))),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST)
    })
    @GetMapping("emailNotifications")
    public ResponseEntity<List<EmailNotification>> getEmailNotifications() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getEmailNotificationsStatuses());
    }

    /**
     * The method which return list of users by filter. Parameter pageable ignored
     * because swagger ui shows the wrong params, instead they are explained in the
     * {@link ApiPageable}.
     *
     * @param filterUserDto dto which contains fields with filter criteria.
     * @param pageable      - pageable configuration.
     * @return {@link PageableDto}
     * @author Rostyslav Khasanov
     */
    @Operation(summary = "Filter all user by search criteria")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = PageableDto.class))),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @ApiPageable
    @PostMapping("filter")
    public ResponseEntity<PageableDto<UserForListDto>> getUsersByFilter(
        @ApiIgnore Pageable pageable, @RequestBody FilterUserDto filterUserDto) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUsersByFilter(filterUserDto, pageable));
    }

    /**
     * Get {@link UserVO} dto by principal (email) from access token.
     *
     * @return {@link UserUpdateDto}.
     * @author Nazar Stasyuk
     */
    @Operation(summary = "Get User dto by principal (email) from access token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = UserUpdateDto.class))),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @GetMapping
    public ResponseEntity<UserUpdateDto> getUserByPrincipal(@ApiIgnore  Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = principal.getName();
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserUpdateDtoByEmail(email));
    }

    /**
     * Update {@link UserVO}.
     *
     * @return {@link ResponseEntity}.
     * @author Nazar Stasyuk
     */
    @Operation(summary = "Update User")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @PatchMapping
    public ResponseEntity<UserUpdateDto> updateUser(@Valid @RequestBody UserUpdateDto dto,
        @ApiIgnore @AuthenticationPrincipal Principal principal) {
        String email = principal.getName();
        return ResponseEntity.status(HttpStatus.OK).body(userService.update(dto, email));
    }

    /**
     * Method returns list of available (not ACTIVE) custom shopping list items for
     * user.
     *
     * @return {@link ResponseEntity}.
     * @author Vitalii Skolozdra
     */
    @Operation(summary = "Get available custom shopping list items for current user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
    })
    @GetMapping("/{userId}/{habitId}/custom-shopping-list-items/available")
    public ResponseEntity<List<CustomShoppingListItemResponseDto>> getAvailableCustomShoppingListItems(
        @Parameter(description = "Id of current user. Cannot be empty.") @PathVariable @CurrentUserId Long userId,
        @PathVariable Long habitId) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(userService.getAvailableCustomShoppingListItems(userId, habitId));
    }

    /**
     * Counts all users by user {@link UserStatus} ACTIVATED.
     *
     * @return amount of users with {@link UserStatus} ACTIVATED.
     * @author Shevtsiv Rostyslav
     */
    @Operation(summary = "Get all activated users amount")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
    })
    @GetMapping("/activatedUsersAmount")
    public ResponseEntity<Long> getActivatedUsersAmount() {
        return ResponseEntity.status(HttpStatus.OK)
            .body(userService.getActivatedUsersAmount());
    }

    /**
     * Update user profile picture {@link UserVO}.
     *
     * @return {@link ResponseEntity}.
     * @author Datsko Marian
     */
    @Operation(summary = "Update user profile picture")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
    })
    @PatchMapping(path = "/profilePicture")
    public ResponseEntity<HttpStatus> updateUserProfilePicture(
        @Parameter(description = "pass image as base64") @RequestPart(required = false) String base64,
        @Parameter(description = "Profile picture") @ImageValidation @RequestPart(required = false) MultipartFile image,
        @ApiIgnore @AuthenticationPrincipal Principal principal) {
        String email = principal.getName();
        userService.updateUserProfilePicture(image, email, base64);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Delete user profile picture {@link UserVO}.
     *
     * @return {@link ResponseEntity}.
     */
    @Operation(summary = "Delete user profile picture")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
    })
    @PatchMapping(path = "/deleteProfilePicture")
    public ResponseEntity<HttpStatus> deleteUserProfilePicture(
        @ApiIgnore @AuthenticationPrincipal Principal principal) {
        String email = principal.getName();
        userService.deleteUserProfilePicture(email);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for save user profile information {@link UserProfileDtoResponse}.
     *
     * @param userProfileDtoRequest - dto for {@link UserVO} entity.
     * @return dto {@link UserProfileDtoResponse} instance.
     * @author Marian Datsko.
     */
    @Operation(summary = "Save user profile information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
    })
    @PutMapping(path = "/profile")
    public ResponseEntity<String> save(
        @Parameter(required = true) @RequestBody @Valid UserProfileDtoRequest userProfileDtoRequest,
        @ApiIgnore Principal principal) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.saveUserProfile(userProfileDtoRequest,
            principal.getName()));
    }

    /**
     * Method returns user profile information.
     *
     * @return {@link UserProfileDtoResponse}.
     * @author Datsko Marian
     */
    @Operation(summary = "Get user profile information by id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/{userId}/profile/")
    public ResponseEntity<UserProfileDtoResponse> getUserProfileInformation(
        @Parameter(description = "Id of current user. Cannot be empty.") @PathVariable @CurrentUserId Long userId) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(userService.getUserProfileInformation(userId));
    }

    /**
     * The method checks by id if a {@link UserVO} is online.
     *
     * @return {@link ResponseEntity}.
     * @author Zhurakovskyi Yurii
     */
    @Operation(summary = "Check by id if the user is online")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404",description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("isOnline/{userId}/")
    public ResponseEntity<Boolean> checkIfTheUserIsOnline(
        @Parameter(description = "Id of the user. Cannot be empty.") @PathVariable Long userId) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(userService.checkIfTheUserIsOnline(userId));
    }

    /**
     * Method returns user profile statistics.
     *
     * @return {@link UserProfileStatisticsDto}.
     * @author Datsko Marian
     */
    @Operation(summary = "Get user profile statistics by id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
    })
    @GetMapping("/{userId}/profileStatistics/")
    public ResponseEntity<UserProfileStatisticsDto> getUserProfileStatistics(
        @Parameter(description = "Id of current user. Cannot be empty.") @PathVariable @CurrentUserId Long userId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(userService.getUserProfileStatistics(userId));
    }

    /**
     * Method find user by principal.
     *
     * @return {@link ResponseEntity}.
     * @author Orest Mamchuk
     */
    @Operation(summary = "Find current user by principal")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/findByEmail")
    public ResponseEntity<UserVO> findByEmail(@Valid @RequestParam String email) {
        String emailRegex = "^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);

        if (!pattern.matcher(email).matches()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        UserVO userVO = userService.findByEmail(email);
        if (userVO == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(userVO);
    }

    /**
     * Get {@link UserVO} by id.
     *
     * @return {@link UserUpdateDto}.
     * @author Orest Mamchuk
     */
    @Operation(summary = "Get User by id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/findById")
    public ResponseEntity<UserVO> findById(@RequestParam Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findById(id));
    }

    /**
     * Method that allow you to find {@link UserVO} for management.
     *
     * @return {@link UserUpdateDto}.
     * @author Orest Mamchuk
     */
    @Operation(summary = "Get User for management")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/findUserForManagement")
    @ApiPageable
    public ResponseEntity<PageableAdvancedDto<UserManagementDto>> findUserForManagementByPage(
        @ApiIgnore Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findUserForManagementByPage(pageable));
    }

    /**
     * Method that allow you to find {@link UserVO} by Id.
     *
     * @return {@link UserUpdateDto}.
     * @author Orest Mamchuk
     */
    @Operation(summary = "Get User by id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/searchBy")
    @ApiPageable
    public ResponseEntity<PageableAdvancedDto<UserManagementDto>> searchBy(
        @RequestParam(required = false, name = "query") String query,
        @ApiIgnore Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.searchBy(pageable, query));
    }

    /**
     * Method that updates user data.
     *
     * @param userDto dto with updated fields.
     * @author Orest Mamchuk
     */
    @Operation(summary = "update via UserManagement")
    @ApiResponses(value = @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED))
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void updateUserManagement(
        @PathVariable @NotNull Long id,
        @RequestBody UserManagementUpdateDto userDto) {
        userService.updateUser(id, userDto);
    }

    /**
     * Method that allow you to find all users {@link UserVO}.
     *
     * @return {@link UserVO list}.
     * @author Orest Mamchuk
     */
    @Operation(summary = "Get all Users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/findAll")
    public ResponseEntity<List<UserVO>> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findAll());
    }

    /**
     * Method creates record in ubs table.
     *
     * @return {@link UbsTableCreationDto}
     */
    @ApiIgnore
    @Operation(summary = "Creates uuid and returns it to ubs microservice.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/createUbsRecord")
    public ResponseEntity<UbsTableCreationDto> createUbsRecord(
        @ApiIgnore @CurrentUser UserVO userVO) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.createUbsRecord(userVO));
    }

    /**
     * Get {@link UserVO} id by email.
     *
     * @return {@link Long}.
     * @author Orest Mamchuk
     */
    @Operation(summary = "Get Id by email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/findIdByEmail")
    public ResponseEntity<Long> findIdByEmail(@RequestParam String email) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findIdByEmail(email));
    }

    /**
     * Update {@link UserVO} Last Activity Time.
     *
     * @param userVO {@link UserVO}.
     * @author Orest Mamchuk
     */
    @Operation(summary = "Update User Last Activity Time")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @PutMapping("/updateUserLastActivityTime/{date}")
    public ResponseEntity<Object> updateUserLastActivityTime(@ApiIgnore @CurrentUser UserVO userVO,
        @PathVariable(value = "date") @DateTimeFormat(
            pattern = "yyyy-MM-dd.HH:mm:ss.SSSSSS") LocalDateTime userLastActivityTime) {
        userService.updateUserLastActivityTime(userVO.getId(), userLastActivityTime);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for setting {@link UserVO}'s status to DEACTIVATED, so the user will
     * not be able to log in into the system.
     *
     * @param id          of the searched {@link UserVO}.
     * @param userReasons {@link List} of {@link String}.
     * @author Orest Mamchuk
     */
    @Operation(summary = "Deactivate user indicating the list of reasons for deactivation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @PutMapping("/deactivate")
    public ResponseEntity<ResponseEntity.BodyBuilder> deactivateUser(@RequestParam Long id,
        @RequestBody List<String> userReasons) {
        UserDeactivationReasonDto userDeactivationDto = userService.deactivateUser(id, userReasons);
        emailService.sendReasonOfDeactivation(userDeactivationDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for getting {@link String} user language.
     *
     * @param userVO {@link UserVO} the current user that wants to get his profile
     *               language
     * @return current user language {@link String}.
     * @author Vlad Pikhotskyi
     */
    @Operation(summary = "Get the current User language")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/lang")
    public ResponseEntity<String> getUserLang(@ApiIgnore @CurrentUser UserVO userVO) {
        return ResponseEntity.status(HttpStatus.OK).body(userVO.getLanguageVO().getCode());
    }

    /**
     * Method for getting a {@link List} of {@link String} - reasons for
     * deactivation of the current user.
     *
     * @param id        {@link Long} - user's id.
     * @param adminLang {@link String} - current administrator language.
     * @return {@link List} of {@link String} - reasons for deactivation of the
     *         current user.
     * @author Vlad Pikhotskyi
     */
    @Operation(summary = "Get list reasons of deactivating the user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/reasons")
    public ResponseEntity<List<String>> getReasonsOfDeactivation(
        @RequestParam("id") Long id, @RequestParam("admin") String adminLang) {
        List<String> list = userService.getDeactivationReason(id, adminLang);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    /**
     * Method that change user language.
     *
     * @param userVO     {@link UserVO} the current user that wants to change his
     *                   profile language
     * @param languageId {@link Long} language id.
     */
    @Operation(summary = "Update user language")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @PutMapping("/language/{languageId}")
    public ResponseEntity<Object> setUserLanguage(@ApiIgnore @CurrentUser UserVO userVO,
        @PathVariable Long languageId) {
        userService.updateUserLanguage(userVO.getId(), languageId);
        return ResponseEntity.ok().build();
    }

    /**
     * Method for setting {@link UserVO}'s status to ACTIVATED.
     *
     * @param id of the searched {@link UserVO}.
     * @author Orest Mamchuk
     */
    @Operation(summary = "Activate User")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @PutMapping("/activate")
    public ResponseEntity<Object> activateUser(@RequestParam Long id) {
        UserActivationDto userActivationDto = userService.setActivatedStatus(id);
        emailService.sendMessageOfActivation(userActivationDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for setting to a list of {@link UserVO} status DEACTIVATED, so the
     * users will not be able to log in into the system.
     *
     * @param listId {@link List} populated with ids of {@link UserVO} to be
     *               deleted.
     * @author Orest Mamchuk
     */
    @Operation(summary = "Deactivate all users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @PutMapping("/deactivateAll")
    public ResponseEntity<List<Long>> deactivateAllUsers(@RequestBody List<Long> listId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.deactivateAllUsers(listId));
    }

    /**
     * Method that allow you to save new {@link UserVO}.
     *
     * @param userVO for save User
     * @author Orest Mamchuk
     */
    @Operation(summary = "Save User")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
    })
    @PostMapping()
    public ResponseEntity<UserVO> saveUser(@RequestBody @CurrentUser UserVO userVO) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.save(userVO));
    }

    /**
     * Method that allow to search users by several summarys.
     *
     * @param pageable    {@link Pageable}
     * @param userViewDto {@link UserManagementViewDto} - stores values.
     */
    @Operation(summary = "Search Users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
    })
    @PostMapping("/search")
    public ResponseEntity<PageableAdvancedDto<UserManagementVO>> search(@ApiIgnore Pageable pageable,
        @RequestBody UserManagementViewDto userViewDto) {
        PageableAdvancedDto<UserManagementVO> found = userService.search(pageable, userViewDto);
        return ResponseEntity.status(HttpStatus.OK).body(found);
    }

    /**
     * Method that allow search users by their email notification.
     *
     * @param emailNotification enum with notification summary.
     * @return {@link List} of {@link UserVO}
     */
    @Operation(summary = "Search Users by email notification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
    })
    @GetMapping("/findAllByEmailNotification")
    public ResponseEntity<List<UserVO>> findAllByEmailNotification(@RequestParam EmailNotification emailNotification) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findAllByEmailNotification(emailNotification));
    }

    /**
     * Delete from the database users that have status 'DEACTIVATED' and last
     * visited the site 2 years ago.
     *
     * @return number of deleted rows
     */
    @Operation(summary = "Delete deactivated Users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
    })
    @PostMapping("/deleteDeactivatedUsers")
    public ResponseEntity<Integer> scheduleDeleteDeactivatedUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.scheduleDeleteDeactivatedUsers());
    }

    /**
     * Method that find all users' cities.
     *
     * @return {@link List} of cities
     */
    @Operation(summary = "Find all users cities")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
    })
    @GetMapping("/findAllUsersCities")
    public ResponseEntity<List<String>> findAllUsersCities() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findAllUsersCities());
    }
}
