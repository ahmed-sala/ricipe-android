package com.example.recipe_android_project.features.auth.data.mapper;

import com.example.recipe_android_project.features.auth.data.entities.UserEntity;
import com.example.recipe_android_project.features.auth.domain.model.User;

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
        user.setPendingSync(entity.isPendingSync());
        user.setPendingSyncAction(entity.getPendingSyncAction());
        user.setLastSyncedAt(entity.getLastSyncedAt());
        user.setCreatedAt(entity.getCreatedAt());
        user.setUpdatedAt(entity.getUpdatedAt());
        user.setPendingPasswordSync(entity.isPendingPasswordSync());
        user.setPendingRegistrationSync(entity.isPendingRegistrationSync());

        return user;
    }

}
