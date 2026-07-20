package com.xyra.schemecraft.model;

public record ProfileUpdateRequest(
        String accountId,
        String bio,
        String bannerPath,
        String profileImagePath
) {}