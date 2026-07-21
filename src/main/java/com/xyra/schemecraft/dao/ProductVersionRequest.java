package com.xyra.schemecraft.dao;

public record ProductVersionRequest(
        String productId,
        String changelog,
        String filePath,
        String minecraftVersion,
        String version
) {}