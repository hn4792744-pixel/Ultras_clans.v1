package me.uc.hussein.ultrasclans.model;

import java.util.Set;

public record ClanRank(String id, String name, String color, String prefix, long minCp, long maxCp, int priority, Set<String> permissions) {}
