package me.uc.hussein.ultrasclans.model;

import java.util.UUID;

public record Invitation(UUID id, UUID clanId, UUID sender, UUID receiver, long createdAt, long expiresAt) {}
