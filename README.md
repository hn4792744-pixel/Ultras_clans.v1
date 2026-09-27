# ULTRAS CLANS v1

**Plugin:** `Ultras_clans`  
**Version:** `v1`  
**Author:** `UC_Hussein`  
**Platform:** Paper / Paper-compatible servers  
**Build JDK:** Java 25  
**Bytecode target:** Java 21 for the widest 1.21.x → 26.2 runtime range.

## Compatibility decision

This project intentionally compiles against the stable Bukkit/Paper API line from **1.21.11** and declares `api-version: 1.21`. This avoids loading the plugin only on 26.2 and keeps the implementation on APIs that exist across the requested range. Paper 26.1+ requires Java 25 at runtime, while 1.20–1.21.11 uses Java 21. The same Java 21 bytecode can run on Java 25.

This is the safest single-JAR strategy. It does **not** promise that every future 26.2-only API feature is available; the code deliberately avoids version-specific APIs where possible.

Paper's current documentation confirms that `plugin.yml` accepts API versions from 1.13 through 26.2 and that 26.2 uses Java 25. See the official Paper docs before changing the target API.

## Current v1 implementation

Implemented as real server logic rather than GUI-only mockups:

- SQLite database with WAL, foreign keys, transactions and indexed member/log tables.
- Clan creation with economy check and refund on failed persistence.
- Public/private clans, browse/search, member lists and clan information.
- Clan owner, member roles, internal permission checks and owner transfer.
- Join cooldown and three-day membership history snapshots.
- Invitations and private-clan join requests with expiry.
- Clan CP, automatic CP rank calculation and anti-repeat kill rewards.
- Member level/XP/contribution statistics.
- Clan level derived from CP.
- Clan bank with capacity, partial deposits and atomic-ish synchronized operations.
- Persistent file-backed clan storage with GUI session validation.
- Clan spawn storage and safe teleport countdown framework.
- Clan warps with persistent locations and teleport cancellation framework.
- Clan TPA with expiry/cooldown and movement/damage cancellation.
- Clan chat and personal clan-tag toggle.
- Daily/weekly/monthly rotating shared tasks with persistent progress and CP rewards.
- Clan alliances with persistent links and capacity checks.
- Clan event framework with wagers in money/CP, participant snapshot at accept time, rounds, spectator elimination, disconnect grace, event spawns and block-change restoration inside configured bounds.
- PlaceholderAPI integration with the `ultras_clan` identifier.
- Vault economy integration plus a configurable command/PlaceholderAPI fallback mode.
- Arabic/English message files and MiniMessage formatting.
- GUI session validation and inventory-holder checks.
- Automatic database backup archives.
- GitHub Actions build using Java 25 + Gradle 9.6.

## Build

### GitHub Actions

Push the project to GitHub. The workflow in `.github/workflows/build.yml` installs Java 25 and Gradle 9.6, then runs:

```bash
gradle clean build --no-daemon
```

The plugin artifact is produced in:

```text
build/libs/Ultras_clans-v1.jar
```

### Termux

If Gradle 9.6 is already installed:

```bash
git clone YOUR_REPOSITORY_URL
cd Ultras_clans_v1
gradle clean build
```

## Installation

1. Install a supported Paper server.
2. Use Java 21 for 1.21.x or Java 25 for Paper 26.1+ / 26.2.
3. Install Vault + an economy plugin if you want normal `$` economy integration.
4. Optionally install PlaceholderAPI.
5. Put the built JAR in `plugins/`.
6. Start the server once.
7. Configure `plugins/Ultras_clans/config.yml`.

## Main commands

Player:

- `/clan`
- `/clans [search]`
- `/clan create`
- `/clan invite <player>`
- `/clan invites`
- `/clan accept [invite-id]`
- `/clan deny [invite-id]`
- `/clan requests [accept <request-id>]`
- `/clan leave [confirm]`
- `/clan kick <player>`
- `/clan promote <player>`
- `/clan demote <player>`
- `/clan bank [deposit|withdraw <amount>]`
- `/clan warp [create|<id>]`
- `/clan setspawn`
- `/clan spawn`
- `/clan tpa <player>`
- `/clan tpa_accept`
- `/clan chat [on|off]`
- `/clan tag [on|off]`
- `/clan tasks`
- `/clan alliance add|accept|remove <clan>`
- `/clan challenge <clan> [money] [cp] [minutes] [rounds]`
- `/clan event accept <event-id>`
- `/clan event leave`

Admin root:

```text
/uc clan admin ...
```

Permission:

```text
ultras.clans.admin
```

## PlaceholderAPI

Identifier: `ultras_clan`

Examples:

```text
%ultras_clan_name%
%ultras_clan_color%
%ultras_clan_tag%
%ultras_clan_rank%
%ultras_clan_rank_name%
%ultras_clan_cp%
%ultras_clan_level%
%ultras_clan_members%
%ultras_clan_online%
%ultras_clan_bank%
%ultras_clan_bank_capacity%
%ultras_clan_storage_level%
%ultras_clan_warps%
%ultras_clan_alliances%
```

## Important safety design

- Database writes run through a single DB executor to avoid SQLite write races.
- GUI actions validate the actual `InventoryHolder`, player session and current clan state.
- Clan CP is server-side; GUI values are never trusted.
- Bank transactions reject negative/oversized values and use synchronized operations.
- Owner cannot be kicked by normal member operations.
- Clan deletion and owner dissolution require explicit confirmation for player-facing destructive actions.
- Event block changes are snapshotted before modification and restored from the original state when the event ends.
- Event rewards are awarded once per event state transition.
- Teleport countdowns cancel on movement/damage.
- Database uses WAL + foreign keys and backups are generated on startup/shutdown when enabled.

## Honest v1 scope note

This repository is a **working production-oriented foundation**, but it is not honest to call every item in the very large specification fully implemented yet. The remaining advanced areas are deliberately not faked:

- full per-clan custom rank editor with 14 editable ranks and persistent per-rank permissions;
- full item-wager inventory workflow and durable item escrow;
- full multi-category Top system including aggregated external UC currency;
- complete Admin GUI action matrix and searchable audit-log GUI;
- full MySQL/MariaDB repository implementation;
- complete GUI YAML slot/item renderer (v1 currently centralizes the main layouts in Java while titles/sizes are configurable);
- full event persistence/recovery across a hard server crash.

These are listed explicitly so the project is not presented as complete when those parts still need engineering.
