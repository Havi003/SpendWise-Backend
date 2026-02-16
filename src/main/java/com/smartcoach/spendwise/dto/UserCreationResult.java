package com.smartcoach.spendwise.dto;

import com.smartcoach.spendwise.domain.entity.User;

public record UserCreationResult(User user, boolean isNew) {}
