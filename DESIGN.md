# ULTRAS CLANS - Architecture Notes

## Modules

- `service/ClanService`: authoritative in-memory clan state + persistence orchestration.
- `repository/ClanRepository`: SQLite persistence, transactions, invitations, requests, history, tasks, warps and alliances.
- `database/Database`: one write executor, WAL mode, foreign keys and schema creation.
- `gui/GuiManager`: GUI composition and session-aware actions.
- `listener/CoreListener`: GUI safety, prompts, chat and teleport damage cancellation.
- `listener/GameplayListener`: kills, deaths, block/task progress, join/quit and event hooks.
- `service/EventService`: challenge state machine, participant snapshot, rounds and block restoration.
- `service/StorageService`: persistent ItemStack inventories per clan/page.
- `service/EconomyService`: Vault first, command/PlaceholderAPI fallback.

## Compatibility

No NMS is used. The project intentionally targets APIs available on 1.21.11 and declares `api-version: 1.21`. Java 25 is the build toolchain; bytecode is released to Java 21.

## Concurrency

Bukkit/Paper objects remain on the server thread. SQLite writes are serialized through a dedicated executor. Service methods modify the authoritative cache and schedule persistence immediately.

## Data integrity

Clan UUIDs, player UUIDs and event UUIDs are used instead of display names as primary identifiers. Clan names are unique case-insensitively at the database layer.

## Event restoration

The event system prevents arbitrary destructive world changes by recording the original block state before player block changes. Event bounds are configured by `/uc clan admin event setrest1` and `setrest2`. The implementation should continue to reject uncontrolled explosions, fire and fluid mechanics if those mechanics are enabled for a future event mode.
