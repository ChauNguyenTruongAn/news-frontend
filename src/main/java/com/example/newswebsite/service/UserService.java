package com.example.newswebsite.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import com.example.newswebsite.domain.Role;
import com.example.newswebsite.domain.User;
import com.example.newswebsite.repository.RoleRepository;
import com.example.newswebsite.repository.UserRepository;
import com.example.newswebsite.util.EditorRequestStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public User findOrCreateUser(String googleId, String name, String email, String avatarUrl) {
        User user = userRepository.findByGoogleId(googleId);
        if (user == null) {
            Role role = roleRepository.findByRoleName("user")
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setRoleName("user");
                        newRole.setDescription("Default user role");
                        return roleRepository.save(newRole);
                    });
            user = new User();
            user.setGoogleId(googleId);
            user.setName(name);
            user.setEmail(email);
            user.setAvatarUrl(avatarUrl);
            user.setRole(role);
            user.setEditorRequestStatus(EditorRequestStatus.NONE);
            user.setCreatedDate(LocalDateTime.now());
            user.setEditor(false);
            userRepository.save(user);
        }
        return user;
    }

    public User saveOrUpdateUser(OidcUser oidcUser) {
        String googleId = oidcUser.getSubject();
        Optional<User> existingUser = Optional.ofNullable(userRepository.findByGoogleId(googleId));

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        User user = new User();
        Role role = roleRepository.findByRoleName("user")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setRoleName("user");
                    newRole.setDescription("Default user role");
                    return roleRepository.save(newRole);
                });
        user = new User();
        user.setGoogleId(googleId);
        user.setName(oidcUser.getFullName());
        user.setEmail(oidcUser.getEmail());
        user.setAvatarUrl(oidcUser.getPicture());
        user.setRole(role);
        user.setEditorRequestStatus(EditorRequestStatus.NONE);
        user.setCreatedDate(LocalDateTime.now());
        user.setEditor(false);
        return userRepository.save(user);
    }

    public User requestEditorRole(String googleId) {
        User user = userRepository.findByGoogleId(googleId);
        if (!user.getRole().getRoleName().equals("user")) {
            throw new RuntimeException("Only users can request editor role");
        }
        if (user.getEditorRequestStatus() != EditorRequestStatus.NONE) {
            throw new RuntimeException("Editor request already submitted");
        }
        user.setEditorRequestStatus(EditorRequestStatus.PENDING);
        return userRepository.save(user);
    }

    public User approveEditorRole(String googleId, String adminGoogleId) {
        User admin = userRepository.findByGoogleId(adminGoogleId);
        if (admin == null || !admin.getRole().getRoleName().equals("admin")) {
            throw new RuntimeException("Unauthorized: Only admins can approve editor requests");
        }

        User user = userRepository.findByGoogleId(googleId);
        if (user.getEditorRequestStatus() != EditorRequestStatus.PENDING || user == null) {
            throw new RuntimeException("No pending editor request for this user");
        }

        user.setEditorRequestStatus(EditorRequestStatus.APPROVED);

        // Create role editor if not existing
        Optional<Role> role = roleRepository.findByRoleName("editor");
        if (!role.isPresent()) {
            Role saveRole = new Role();
            saveRole.setRoleName("editor");
            saveRole.setDescription("Role have to request to admin");
            roleRepository.save(saveRole);
            user.setRole(saveRole);
        } else {
            user.setRole(role.get());
        }

        user.setEditor(true); // Cấp quyền editor
        return userRepository.save(user);
    }

    public User findByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId);
    }

    /**
     * Lấy danh sách tất cả người dùng
     * 
     * @return Danh sách tất cả người dùng
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Chỉnh sửa vai trò của người dùng
     * 
     * @param targetGoogleId Google ID của người dùng cần chỉnh sửa quyền
     * @param roleName       Tên vai trò mới ("user", "editor", "admin")
     * @param adminGoogleId  Google ID của admin thực hiện thay đổi
     * @return Người dùng đã được cập nhật
     * @throws RuntimeException Nếu người thực hiện không phải admin hoặc vai trò
     *                          không tồn tại
     */
    public User updateUserRole(String targetGoogleId, String roleName, String adminGoogleId) {
        // Xác thực admin
        User admin = userRepository.findByGoogleId(adminGoogleId);
        if (admin == null || !admin.getRole().getRoleName().equals("admin")) {
            throw new RuntimeException("Unauthorized: Only admins can update user roles");
        }

        // Tìm người dùng cần thay đổi quyền
        User targetUser = userRepository.findByGoogleId(targetGoogleId);
        if (targetUser == null) {
            throw new RuntimeException("User not found with googleId: " + targetGoogleId);
        }

        // Tìm vai trò mới
        Role newRole = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        // Cập nhật vai trò
        targetUser.setRole(newRole);

        // Cập nhật trạng thái isEditor nếu cần
        if (roleName.equals("editor")) {
            targetUser.setEditor(true);
            targetUser.setEditorRequestStatus(EditorRequestStatus.APPROVED);
        } else if (roleName.equals("user")) {
            targetUser.setEditor(false);
            targetUser.setEditorRequestStatus(EditorRequestStatus.NONE);
        }

        // Lưu thay đổi
        return userRepository.save(targetUser);
    }
}
