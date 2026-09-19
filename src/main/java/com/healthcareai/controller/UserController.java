package com.healthcareai.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthcareai.dto.UpdateUserStatusRequest;
import com.healthcareai.dto.UserPasswordResetResponse;
import com.healthcareai.dto.UserResponse;
import com.healthcareai.entity.User;
import com.healthcareai.service.UserService;
import com.healthcareai.tenant.TenantContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Staff/team management within the caller's own clinic: list existing
 * staff, deactivate/reactivate an account, and recover a locked-out staff
 * member's password. Restricted to {@code ROLE_ADMIN} (see
 * {@code SecurityConfig}). Creating a new staff member is still done via
 * {@code POST /api/auth/register} (unchanged).
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Admin-only staff/team management within the caller's own clinic")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "List every staff member (doctors, receptionists, admins) in the caller's clinic.")
    public ResponseEntity<List<UserResponse>> list() {
        UUID tenantId = TenantContext.getCurrentTenantId();
        return ResponseEntity.ok(userService.findByTenant(tenantId).stream().map(this::toResponse).toList());
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activate or deactivate a staff member's account. An admin cannot deactivate "
            + "their own account, and a clinic must always keep at least one active admin.")
    public ResponseEntity<UserResponse> updateStatus(@PathVariable UUID id,
                                                       @Valid @RequestBody UpdateUserStatusRequest request,
                                                       Authentication authentication) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        User user = userService.setStaffEnabled(tenantId, id, request.enabled(), authentication.getName());
        return ResponseEntity.ok(toResponse(user));
    }

    @PostMapping("/{id}/reset-password")
    @Operation(summary = "Generate a new temporary password for a staff member in the caller's clinic and "
            + "set it immediately, returned once in this response (not emailed, not stored in plaintext) "
            + "so the admin can relay it directly.")
    public ResponseEntity<UserPasswordResetResponse> resetPassword(@PathVariable UUID id) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        return ResponseEntity.ok(userService.resetStaffPassword(tenantId, id));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole().name(),
                user.isEnabled(), user.getCreatedAt());
    }
}
