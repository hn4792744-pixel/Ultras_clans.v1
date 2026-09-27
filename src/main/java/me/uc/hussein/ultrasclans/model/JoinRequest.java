package me.uc.hussein.ultrasclans.model;

import java.util.UUID;

public record JoinRequest(UUID id, UUID clanId, UUID player, long createdAt, long expiresAt) {}
