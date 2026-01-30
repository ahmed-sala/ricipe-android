package com.example.recipe_android_project.features.auth.data.mapper;

import com.example.recipe_android_project.features.auth.data.entities.UserEntity;
import com.example.recipe_android_project.features.auth.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserMapper {

    private UserMapper() {
    }
    public static User toDomain(UserEntity entity) {
        if (entity == null) return null;

        User user = new User();
        user.setId(entity.getId());
        user.setFullName(entity.getFullName());
        user.setEmail(entity.getEmail());
        user.setPassword(entity.getPassword());
        user.setLoggedIn(entity.isLoggedIn());
        user.setCreatedAt(entity.getCreatedAt());
        user.setUpdatedAt(entity.getUpdatedAt());

        return user;
    }

    public static List<User> toDomainListFromEntities(List<UserEntity> entities) {
        List<User> users = new ArrayList<>();
        if (entities != null) {
            for (UserEntity entity : entities) {
                User user = toDomain(entity);
                if (user != null) {
                    users.add(user);
                }
            }
        }
        return users;
    }

    // ==================== DOMAIN -> ENTITY ====================

    public static UserEntity toEntity(User user) {
        if (user == null) return null;

        UserEntity entity = new UserEntity();

        // Generate ID if not present
        if (user.getId() != null && !user.getId().isEmpty()) {
            entity.setId(user.getId());
        } else {
            entity.setId(UUID.randomUUID().toString());
        }

        entity.setFullName(user.getFullName());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setLoggedIn(user.isLoggedIn());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());

        return entity;
    }

    public static List<UserEntity> toEntityList(List<User> users) {
        List<UserEntity> entities = new ArrayList<>();
        if (users != null) {
            for (User user : users) {
                UserEntity entity = toEntity(user);
                if (entity != null) {
                    entities.add(entity);
                }
            }
        }
        return entities;
    }

    // ==================== HELPER: Create new user ====================

    public static User createNewUser(String fullName, String email, String password) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(password);
        user.setLoggedIn(false);
        user.setCreatedAt(System.currentTimeMillis());
        user.setUpdatedAt(System.currentTimeMillis());
        return user;
    }

    public static UserEntity createNewUserEntity(String fullName, String email, String password) {
        UserEntity entity = new UserEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setFullName(fullName);
        entity.setEmail(email);
        entity.setPassword(password);
        entity.setLoggedIn(false);
        entity.setCreatedAt(System.currentTimeMillis());
        entity.setUpdatedAt(System.currentTimeMillis());
        return entity;
    }
}
