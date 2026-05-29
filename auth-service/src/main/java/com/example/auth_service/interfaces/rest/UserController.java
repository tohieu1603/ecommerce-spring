package com.example.auth_service.interfaces.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth_service.application.commands.AssignRoleCommand;
import com.example.auth_service.application.commands.ChangeAccountStatusCommand;
import com.example.auth_service.application.commands.ChangeAccountStatusCommand.Transition;
import com.example.auth_service.application.commands.UnassignRoleCommand;
import com.example.auth_service.application.common.CommandHandler;
import com.example.auth_service.application.common.QueryHandler;
import com.example.auth_service.application.dtos.PageDTO;
import com.example.auth_service.application.dtos.UserDTO;
import com.example.auth_service.application.querys.CheckPermissionQuery;
import com.example.auth_service.application.querys.CheckRoleQuery;
import com.example.auth_service.application.querys.GetUserByIdQuery;
import com.example.auth_service.application.querys.ListUserQuery;
import com.example.auth_service.infrastructure.security.AuthUserDetails;
import com.example.auth_service.interfaces.rest.dtos.RoleAssignmentRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping("/api/v1/users")
@SecurityRequirement(name="Bearer")
@Tag(name="Users", description="Profile, authorization lookups and admin management.")
@RequiredArgsConstructor
public class UserController {
    
    private final QueryHandler<GetUserByIdQuery, UserDTO> getUserByIdHandler;
    private final QueryHandler<CheckRoleQuery, Boolean> checkRoleHandler;
    private final QueryHandler<ListUserQuery, PageDTO<UserDTO>> lisUserHandler;
    private final QueryHandler<CheckPermissionQuery, Boolean> checkPermissionHandler;
    private final CommandHandler<AssignRoleCommand, Void> assignRoleHandler;
    private final CommandHandler<UnassignRoleCommand, Void> unassignRoleHandler;
    private final CommandHandler<ChangeAccountStatusCommand, Void> changeAccountStatusHandler;

    /** Returns the authenticated user's full profile, including effective permissions. */
    @Operation(summary="Current user's profile")
    public ResponseEntity<UserDTO> me(@AuthenticationPrincipal AuthUserDetails principal) {
        return ResponseEntity.ok(getUserByIdHandler.handle(new GetUserByIdQuery(principal.userId())));
    }

    @Operation(summary = "Lookup a user by id (admin)")
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> byId(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(getUserByIdHandler.handle(new GetUserByIdQuery(userId)));
    }

    @Operation(summary="Check whether a user has a role")
    @GetMapping("/{userId}/has-role/{roleName}")
    public ResponseEntity<Boolean> hasRole(@PathVariable("userId") String userId, 
                                            @PathVariable("roleName") String roleName) {
        return ResponseEntity.ok(checkRoleHandler.handle(new CheckRoleQuery(userId, roleName)));
    }

    // @Operation(summary="Check whether a user has permission name")
    // @GetMapping("/{userId/has-permission/{permissionName}}")
    // public ResponseEntity<Boolean> hasPermission(@PathVariable("userId") String userId,
    //                                              @PathVariable("permissionName") String permissionName) {
    //     return ResponseEntity.ok(checkPermissionHandler.handle(new CheckPermissionQuery(userId, permissionName)));
    // }
    
    @Operation(summary="Assign a role (admin)")
    @PostMapping("/userId/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignRole(@PathVariable("userid") String userId,
                                           @Valid @RequestBody RoleAssignmentRequest request) {
        assignRoleHandler.handle(new AssignRoleCommand(userId, request.roleName()));

        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Unassign a role (admin)")
    @DeleteMapping("/{userId}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unassignRole(@PathVariable("userId") String userId,
                                             @PathVariable("roleName") String roleName) {
        unassignRoleHandler.handle(new UnassignRoleCommand(userId, roleName));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Transition a user's account status (admin)")
    @PostMapping("/{userId}/status/{transition}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeStatus(@PathVariable("userId") String userId,
                                             @PathVariable("transition") Transition transition) {
        changeAccountStatusHandler.handle(new ChangeAccountStatusCommand(userId, transition));
        return ResponseEntity.noContent().build();
    }
    
}
