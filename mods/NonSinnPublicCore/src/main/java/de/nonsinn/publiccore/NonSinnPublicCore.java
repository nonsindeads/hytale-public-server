package de.nonsinn.publiccore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.CraftRecipeEvent;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.DropItemEvent;
import com.hypixel.hytale.server.core.event.events.ecs.InteractivelyPickupItemEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.event.events.player.DrainPlayerFromWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import de.glymera.plotworld.GlymeraPlotWorld;
import net.cfh.vault.VaultUnlocked;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import net.milkbowl.vault2.economy.Economy;
import net.milkbowl.vault2.economy.EconomyResponse;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class NonSinnPublicCore extends JavaPlugin {
    private static final String HUB_WORLD = "himmelsinsel";
    private static final String PLAYER_GROUP = "spieler";
    private static final String PLOT_DEED_ID = "GlymeraPlotWorld_Deed";
    private static final String ECONOMY_CONTEXT = "NonSinnPublicCore";
    private static final String GUEST_HUD_KEY = "nonsinnGuestNotice";
    private static final String OWNER_GUEST_TEST_FLAG = "owner-as-guest.flag";
    private static final UUID OWNER_UUID = UUID.fromString("2e07651a-2a27-4165-a440-5b3f7abb3392");
    private static final long NOTICE_INTERVAL_MS = 3_000L;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<UUID, QuizSession> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastGuardNotice = new ConcurrentHashMap<>();
    private final Map<UUID, Long> joinTimes = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> welcomeSent = new ConcurrentHashMap<>();
    private final AtomicInteger minuteCounter = new AtomicInteger(0);
    private final AtomicInteger broadcastIndex = new AtomicInteger(0);
    private ScheduledExecutorService announcementExecutor;

    private final Map<String, Approval> approvals = new ConcurrentHashMap<>();
    private final Map<String, Integer> propertyProgress = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> propertyTransactions = new ConcurrentHashMap<>();

    private QuestionsConfig questions;
    private Path approvalsPath;
    private PropertyPricing propertyPricing;
    private Path propertyProgressPath;

    public NonSinnPublicCore(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        try {
            loadData();
        } catch (IOException exception) {
            throw new IllegalStateException("PublicCore-Konfiguration konnte nicht geladen werden", exception);
        }

        getCommandRegistry().registerCommand(new UnlockCommand(this));
        getCommandRegistry().registerCommand(new AnswerCommand(this));
        getCommandRegistry().registerCommand(new RulesCommand(this));
        getCommandRegistry().registerCommand(new HandbookCommand());
        getCommandRegistry().registerCommand(new WebsiteCommand(this));
        getCommandRegistry().registerCommand(new PropertyCommand(this));
        getCommandRegistry().registerCommand(new EconomyRewardCommand(this));
        getCommandRegistry().registerCommand(new BuildSpawnPlazaCommand(this));

        getEventRegistry().registerGlobal(PlayerConnectEvent.class, this::onPlayerConnect);
        getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, this::onPlayerDisconnect);

        getEventRegistry().registerGlobal(AllWorldsLoadedEvent.class, this::onAllWorldsLoaded);
        getEventRegistry().registerGlobal(AddPlayerToWorldEvent.class, this::onPlayerAddedToWorld);
        getEventRegistry().registerGlobal(DrainPlayerFromWorldEvent.class, this::onPlayerDrain);
        getEventRegistry().registerGlobal(PlayerInteractEvent.class, this::onPlayerInteract);

        getEntityStoreRegistry().registerSystem(new GuestBreakBlockSystem(this));
        getEntityStoreRegistry().registerSystem(new GuestDamageBlockSystem(this));
        getEntityStoreRegistry().registerSystem(new GuestPlaceBlockSystem(this));
        getEntityStoreRegistry().registerSystem(new GuestUseBlockSystem(this));
        getEntityStoreRegistry().registerSystem(new GuestDropItemSystem(this));
        getEntityStoreRegistry().registerSystem(new GuestPickupItemSystem(this));
        getEntityStoreRegistry().registerSystem(new GuestCraftSystem(this));

        announcementExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "NonSinnPublicCore-Announcer");
            t.setDaemon(true);
            return t;
        });
        announcementExecutor.scheduleAtFixedRate(this::tickAnnouncements, 60, 60, TimeUnit.SECONDS);

        getLogger().atInfo().log(
                "NonSinnPublicCore geladen: Regelversion %s, %d Fragen, %d Freigaben",
                questions.rulesVersion,
                questions.questions.size(),
                approvals.size()
        );
    }

    /**
     * Console-only bridge used by trusted quest rewards. Players can neither
     * invoke this command themselves nor choose arbitrary amounts through the
     * bounty UI; the authored quest asset supplies every argument.
     */
    private static final class EconomyRewardCommand extends AbstractAsyncCommand {
        private final NonSinnPublicCore plugin;
        private final RequiredArg<String> playerArg;
        private final RequiredArg<String> amountArg;
        private final RequiredArg<String> sourceArg;

        private EconomyRewardCommand(NonSinnPublicCore plugin) {
            super("nspceconomyreward", "Interne Goldbelohnung fuer Serverauftraege");
            this.plugin = plugin;
            this.playerArg = withRequiredArg("player", "Zielspieler", ArgTypes.STRING);
            this.amountArg = withRequiredArg("amount", "Goldbetrag", ArgTypes.STRING);
            this.sourceArg = withRequiredArg("source", "Belohnungsquelle", ArgTypes.STRING);
        }

        @Override
        public boolean canGeneratePermission() {
            return false;
        }

        @Override
        protected CompletableFuture<Void> executeAsync(CommandContext context) {
            if (context.isPlayer()) {
                context.sendMessage(Message.raw("Dieser interne Befehl ist nur fuer Serverbelohnungen verfuegbar."));
                plugin.getLogger().atWarning().log("Spieleraufruf von nspceconomyreward wurde abgewiesen");
                return CompletableFuture.completedFuture(null);
            }

            String playerName = playerArg.get(context);
            String source = sourceArg.get(context);
            if (source == null || !source.matches("[a-z0-9_:-]{1,96}")) {
                plugin.getLogger().atWarning().log("Ungueltige Goldquelle abgewiesen: %s", source);
                return CompletableFuture.completedFuture(null);
            }

            int amount;
            try {
                amount = Integer.parseInt(amountArg.get(context));
            } catch (NumberFormatException exception) {
                plugin.getLogger().atWarning().log("Ungueltiger Goldbetrag fuer %s", playerName);
                return CompletableFuture.completedFuture(null);
            }
            if (amount < 1 || amount > 1_000) {
                plugin.getLogger().atWarning().log(
                        "Goldbetrag ausserhalb der sicheren Grenzen: %d fuer %s", amount, playerName
                );
                return CompletableFuture.completedFuture(null);
            }

            PlayerRef target = Universe.get().getPlayerByUsername(playerName, NameMatching.EXACT_IGNORE_CASE);
            if (target == null) {
                plugin.getLogger().atWarning().log(
                        "Goldbelohnung konnte nicht zugestellt werden: %s ist offline (%s)", playerName, source
                );
                return CompletableFuture.completedFuture(null);
            }

            Economy economy = VaultUnlocked.economyObj();
            if (economy == null) {
                plugin.getLogger().atSevere().log("Goldbelohnung fehlgeschlagen: Vault-Economy nicht verfuegbar");
                return CompletableFuture.completedFuture(null);
            }

            UUID uuid = target.getUuid();
            if (!economy.hasAccount(uuid)) {
                economy.createAccount(uuid, target.getUsername(), true);
            }
            EconomyResponse response = economy.deposit(
                    ECONOMY_CONTEXT + ":bounty",
                    uuid,
                    BigDecimal.valueOf(amount)
            );
            if (response == null || !response.transactionSuccess()) {
                plugin.getLogger().atSevere().log(
                        "Goldbelohnung abgelehnt: %d Gold fuer %s (%s)", amount, target.getUsername(), source
                );
                return CompletableFuture.completedFuture(null);
            }

            target.sendMessage(Message.raw("Auftragsbelohnung: +" + amount + " Gold"));
            plugin.getLogger().atInfo().log(
                    "Goldbelohnung: %d Gold fuer %s aus %s", amount, target.getUsername(), source
            );
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    protected void start() {
        // LuckPerms publishes its API during its start phase. The declared
        // dependency guarantees that its start runs before ours.
        bootstrapPermissions();
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {}
            buildBauweltSpawnPlaza();
        });
    }

    private void bootstrapPermissions() {
        LuckPerms luckPerms = LuckPermsProvider.get();
        GroupManager groups = luckPerms.getGroupManager();

        Group guest = ensureGroup(groups, "gast");
        Group player = ensureGroup(groups, PLAYER_GROUP);
        Group moderator = ensureGroup(groups, "moderator");
        Group admin = ensureGroup(groups, "admin");
        Group owner = ensureGroup(groups, "owner");
        Group defaultGroup = ensureGroup(groups, "default");

        addParent(defaultGroup, guest);
        removePermission(player, "glymerapermissions.restricted");
        addPermission(player, "nonsinnpubliccore.unlocked");
        addPermission(player, "questlinesclaims.claim.use", false);
        addPermission(player, "questlinesclaims.claim.use", true, "world", "default");
        addParent(moderator, player);
        addParent(admin, moderator);
        addParent(owner, admin);
        addPermission(owner, "*");
        addPermission(owner, "questlinesclaims.claim.use");

        groups.saveGroup(defaultGroup).join();
        groups.saveGroup(player).join();
        groups.saveGroup(moderator).join();
        groups.saveGroup(admin).join();
        groups.saveGroup(owner).join();

        User ownerUser = luckPerms.getUserManager().loadUser(OWNER_UUID, "NonSinn").join();
        boolean ownerGuestTest = Files.exists(getDataDirectory().resolve(OWNER_GUEST_TEST_FLAG));
        if (ownerGuestTest) {
            ownerUser.data().remove(InheritanceNode.builder(owner).build());
            if (isGuest(OWNER_UUID)) {
                ownerUser.data().remove(InheritanceNode.builder(player).build());
                ownerUser.setPrimaryGroup(defaultGroup.getName());
            } else {
                ownerUser.data().add(InheritanceNode.builder(player).build());
                ownerUser.setPrimaryGroup(player.getName());
            }
        } else {
            ownerUser.data().add(InheritanceNode.builder(owner).build());
            ownerUser.setPrimaryGroup(owner.getName());
        }
        luckPerms.getUserManager().saveUser(ownerUser).join();

        getLogger().atInfo().log(ownerGuestTest
                ? "LuckPerms-Basis sichergestellt; NonSinn testet temporaer als Gast"
                : "LuckPerms-Basis sichergestellt: gast, spieler, moderator, admin, owner; NonSinn=owner");
    }

    private Group ensureGroup(GroupManager groups, String name) {
        return groups.createAndLoadGroup(name).join();
    }

    private void addParent(Group child, Group parent) {
        child.data().add(InheritanceNode.builder(parent).build());
    }

    private void addPermission(Group group, String permission) {
        group.data().add(PermissionNode.builder(permission).build());
    }

    private void addPermission(Group group, String permission, boolean value) {
        group.data().add(PermissionNode.builder(permission).value(value).build());
    }

    private void addPermission(
            Group group,
            String permission,
            boolean value,
            String contextKey,
            String contextValue
    ) {
        group.data().add(
                PermissionNode.builder(permission)
                        .value(value)
                        .withContext(contextKey, contextValue)
                        .build()
        );
    }

    private void removePermission(Group group, String permission) {
        group.data().remove(PermissionNode.builder(permission).build());
    }

    @Override
    protected void shutdown() {
        if (announcementExecutor != null) {
            announcementExecutor.shutdownNow();
        }
        saveApprovals();

        savePropertyProgress();
    }

    boolean isGuest(UUID uuid) {
        if (uuid == null) {
            return true;
        }
        if (PermissionsModule.get().hasPermission(uuid, "*")) {
            return false;
        }
        Approval approval = approvals.get(uuid.toString());
        return approval == null || !questions.rulesVersion.equals(approval.rulesVersion);
    }

    void notifyGuestGuard(PlayerRef playerRef) {
        long now = System.currentTimeMillis();
        Long previous = lastGuardNotice.put(playerRef.getUuid(), now);
        if (previous == null || now - previous >= NOTICE_INTERVAL_MS) {
            playerRef.sendMessage(Message.raw(
                    "Als Gast kannst du hier noch nichts benutzen. Lies die Regeln und nutze /freischalten."
            ));
        }
    }

    private void onPlayerDisconnect(PlayerDisconnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        if (playerRef != null) {
            UUID uuid = playerRef.getUuid();
            joinTimes.remove(uuid);
            welcomeSent.remove(uuid);
            lastGuardNotice.remove(uuid);
        }
    }

    private void tickAnnouncements() {
        try {
            int minute = minuteCounter.incrementAndGet();
            long now = System.currentTimeMillis();

            // 1. Persoenliche Willkommens-Nachricht nach ~5 Minuten Online-Zeit
            for (PlayerRef playerRef : Universe.get().getPlayers()) {
                if (playerRef == null) {
                    continue;
                }
                UUID uuid = playerRef.getUuid();
                Long joinTime = joinTimes.get(uuid);
                if (joinTime != null && !welcomeSent.getOrDefault(uuid, false)) {
                    if (now - joinTime >= 300_000L) {
                        welcomeSent.put(uuid, true);
                        playerRef.sendMessage(Message.raw(
                                "[Der Waldbrand] Willkommen! Schau dir mit /handbuch alle Guides an. Bei Fragen oder Vorschlaegen: https://nonsindeads.github.io/hytale-public-server/"
                        ));
                    }
                }
            }

            // 2. Rotierende globale Server-Ankuendigungen alle 15 Minuten
            if (minute % 15 == 0) {
                int idx = broadcastIndex.getAndIncrement() % 3;
                String msg;
                switch (idx) {
                    case 0:
                        msg = "[Der Waldbrand] Server-Neustarts finden taeglich um 00:00, 08:00 und 16:00 Uhr statt (inkl. automatischem Backup & Performance-Optimierung).";
                        break;
                    case 1:
                        msg = "[Der Waldbrand] Moechtest du mit ins Team und den Server mitgestalten? Wir suchen Builder, Modder & Guides! Discord & GitHub: https://nonsindeads.github.io/hytale-public-server/";
                        break;
                    default:
                        msg = "[Der Waldbrand] Nuetzliche Befehle: /handbuch (Spieler-Guide), /claim (Survival-Land sichern), /grundstueck (64x64 Bauwelt) & /money (Gold & Haendler).";
                        break;
                }
                for (PlayerRef playerRef : Universe.get().getPlayers()) {
                    if (playerRef != null) {
                        playerRef.sendMessage(Message.raw(msg));
                    }
                }
            }
        } catch (Exception ex) {
            getLogger().atWarning().withCause(ex).log("Fehler beim Ausfuehren der Server-Ankuendigungen");
        }
    }

    private void onPlayerConnect(PlayerConnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        if (playerRef != null) {
            joinTimes.putIfAbsent(playerRef.getUuid(), System.currentTimeMillis());
        }

        if (!isGuest(playerRef.getUuid())) {
            return;
        }
        World hub = Universe.get().getWorld(HUB_WORLD);
        if (hub != null) {
            event.setWorld(hub);
            var spawn = hub.getWorldConfig().getSpawnProvider().getSpawnPoint(hub, playerRef.getUuid());
            TransformComponent transform = event.getHolder().getComponent(TransformComponent.getComponentType());
            if (spawn != null && transform != null) {
                transform.setPosition(spawn.getPosition());
                transform.setRotation(spawn.getRotation());
                getLogger().atInfo().log(
                        "Gast %s sicher nach Glutwacht versetzt: %.1f / %.1f / %.1f",
                        playerRef.getUsername(),
                        spawn.getPosition().x,
                        spawn.getPosition().y,
                        spawn.getPosition().z
                );
            }
        } else {
            getLogger().atWarning().log("Gast-Hub '%s' ist nicht geladen", HUB_WORLD);
        }
    }

    private void onAllWorldsLoaded(AllWorldsLoadedEvent event) {
        World hubWorld = Universe.get().getWorld(HUB_WORLD);
        if (hubWorld != null) {
            hubWorld.getWorldConfig().setPvpEnabled(false);
            hubWorld.getWorldConfig().setFallDamageEnabled(false);
            hubWorld.getWorldConfig().setSpawningNPC(false);
            hubWorld.getWorldConfig().markChanged();
        }
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {}
            buildBauweltSpawnPlaza();
        });
    }

    private void onPlayerAddedToWorld(AddPlayerToWorldEvent event) {
        PlayerRef playerRef = event.getHolder().getComponent(PlayerRef.getComponentType());
        Player player = event.getHolder().getComponent(Player.getComponentType());
        if (playerRef == null || player == null) {
            return;
        }
        if (isGuest(playerRef.getUuid())) {
            if (player.getHudManager().getCustomHud(GUEST_HUD_KEY) == null) {
                player.getHudManager().addCustomHud(playerRef, new GuestNoticeHud(playerRef));
            }
        } else {
            player.getHudManager().removeCustomHud(playerRef, GUEST_HUD_KEY);
        }
    }

    private void hideGuestNotice(PlayerRef playerRef) {
        Player player = playerRef.getComponent(Player.getComponentType());
        if (player != null) {
            player.getHudManager().removeCustomHud(playerRef, GUEST_HUD_KEY);
        }
    }

    private void onPlayerDrain(DrainPlayerFromWorldEvent event) {
        PlayerRef playerRef = event.getHolder().getComponent(PlayerRef.getComponentType());
        if (playerRef == null || !isGuest(playerRef.getUuid())) {
            return;
        }
        World hub = Universe.get().getWorld(HUB_WORLD);
        if (hub == null || event.getWorld() == hub) {
            return;
        }
        event.setWorld(hub);
        event.setTransform(hub.getWorldConfig().getSpawnProvider().getSpawnPoint(hub, playerRef.getUuid()));
    }

    private void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getPlayer() == null || event.getPlayer().getPlayerRef() == null) {
            return;
        }
        PlayerRef playerRef = event.getPlayer().getPlayerRef();
        if (isGuest(playerRef.getUuid())) {
            event.setCancelled(true);
            notifyGuestGuard(playerRef);
        }
    }

    private void startQuiz(PlayerRef playerRef) {
        UUID uuid = playerRef.getUuid();
        if (!isGuest(uuid)) {
            ensurePlayerGroup(uuid, playerRef.getUsername());
            playerRef.sendMessage(Message.raw("Du bist bereits fuer Regelversion " + questions.rulesVersion + " freigeschaltet."));
            return;
        }

        long now = System.currentTimeMillis();
        long cooldownUntil = cooldowns.getOrDefault(uuid, 0L);
        if (cooldownUntil > now) {
            long seconds = Math.max(1L, (cooldownUntil - now + 999L) / 1000L);
            playerRef.sendMessage(Message.raw("Bitte warte noch " + seconds + " Sekunden bis zum naechsten Versuch."));
            return;
        }

        List<Integer> order = new ArrayList<>();
        for (int index = 0; index < questions.questions.size(); index++) {
            order.add(index);
        }
        Collections.shuffle(order, ThreadLocalRandom.current());
        int count = Math.min(questions.questionsPerAttempt, order.size());
        QuizSession session = new QuizSession(new ArrayList<>(order.subList(0, count)));
        sessions.put(uuid, session);

        playerRef.sendMessage(Message.raw("Regeltest gestartet. Antworte jeweils mit /antwort <Nummer>."));
        sendQuestion(playerRef, session);
    }

    private void answer(PlayerRef playerRef, int selectedAnswer) {
        UUID uuid = playerRef.getUuid();
        QuizSession session = sessions.get(uuid);
        if (session == null) {
            playerRef.sendMessage(Message.raw("Starte zuerst mit /freischalten."));
            return;
        }

        Question question = questions.questions.get(session.order.get(session.position));
        if (selectedAnswer < 1 || selectedAnswer > question.answers.size()) {
            playerRef.sendMessage(Message.raw("Ungueltige Antwort. Waehle 1 bis " + question.answers.size() + "."));
            return;
        }

        if (selectedAnswer - 1 == question.correctIndex) {
            session.correct++;
            playerRef.sendMessage(Message.raw("Richtig. " + question.explanation));
        } else {
            playerRef.sendMessage(Message.raw("Nicht richtig. " + question.explanation));
        }

        session.position++;
        if (session.position < session.order.size()) {
            sendQuestion(playerRef, session);
            return;
        }

        sessions.remove(uuid);
        if (session.correct < questions.requiredCorrect) {
            cooldowns.put(uuid, System.currentTimeMillis() + questions.attemptCooldownSeconds * 1000L);
            playerRef.sendMessage(Message.raw(
                    "Nicht bestanden: " + session.correct + "/" + session.order.size()
                            + ". Lies die Regeln noch einmal: " + questions.docsUrl
            ));
            return;
        }

        if (!ensurePlayerGroup(uuid, playerRef.getUsername())) {
            playerRef.sendMessage(Message.raw("Freischaltung ist noch nicht bereit. Bitte melde dich beim Team."));
            return;
        }

        Approval approval = new Approval();
        approval.rulesVersion = questions.rulesVersion;
        approval.acceptedAt = Instant.now().toString();
        approval.score = session.correct;
        approval.total = session.order.size();
        approvals.put(uuid.toString(), approval);
        saveApprovals();
        cooldowns.remove(uuid);
        hideGuestNotice(playerRef);

        playerRef.sendMessage(Message.raw(
                "Bestanden: " + session.correct + "/" + session.order.size()
                        + ". Du bist jetzt als Spieler freigeschaltet. Willkommen!"
        ));
    }

    private boolean ensurePlayerGroup(UUID uuid, String username) {
        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            GroupManager groups = luckPerms.getGroupManager();
            Group playerGroup = ensureGroup(groups, PLAYER_GROUP);
            groups.saveGroup(playerGroup).join();

            User user = luckPerms.getUserManager().loadUser(uuid, username).join();
            user.data().add(InheritanceNode.builder(playerGroup).build());
            user.setPrimaryGroup(playerGroup.getName());
            luckPerms.getUserManager().saveUser(user).join();
            return true;
        } catch (RuntimeException exception) {
            getLogger().atSevere().withCause(exception).log(
                    "Automatische LuckPerms-Freischaltung fuer %s fehlgeschlagen",
                    username
            );
            return false;
        }
    }

    private static final class GuestNoticeHud extends CustomUIHud {
        private GuestNoticeHud(PlayerRef playerRef) {
            super(playerRef, GUEST_HUD_KEY, 20);
        }

        @Override
        protected void build(UICommandBuilder builder) {
            builder.appendInline(null,
                    "Group #GuestNoticeRoot { "
                            + "Anchor: (Full: 0); LayoutMode: Top; HitTestVisible: false; "
                            + "Group { Anchor: (Top: 105, Horizontal: 0, Width: 540, Height: 78); "
                            + "Background: #101722(0.88); Padding: (Horizontal: 14, Vertical: 7); "
                            + "LayoutMode: Top; "
                            + "Label { Anchor: (Height: 23); Text: \"DU BIST NOCH GAST\"; "
                            + "Style: (FontSize: 16, TextColor: #F2C66D, RenderBold: true, "
                            + "HorizontalAlignment: Center, VerticalAlignment: Center); } "
                            + "Label { Anchor: (Height: 20); Text: \"Regeln: /regeln | Webseite: /webseite oder /wiki\"; "
                            + "Style: (FontSize: 14, TextColor: #FFFFFF, "
                            + "HorizontalAlignment: Center, VerticalAlignment: Center); } "
                            + "Label { Anchor: (Height: 20); Text: \"Danach: /freischalten\"; "
                            + "Style: (FontSize: 14, TextColor: #FFFFFF, "
                            + "HorizontalAlignment: Center, VerticalAlignment: Center); } "
                            + "} }"
            );
        }
    }

    private void sendQuestion(PlayerRef playerRef, QuizSession session) {
        Question question = questions.questions.get(session.order.get(session.position));
        playerRef.sendMessage(Message.raw(
                "Frage " + (session.position + 1) + "/" + session.order.size() + ": " + question.question
        ));
        for (int index = 0; index < question.answers.size(); index++) {
            playerRef.sendMessage(Message.raw((index + 1) + ") " + question.answers.get(index)));
        }
    }

    private void loadData() throws IOException {
        Files.createDirectories(getDataDirectory());
        Path questionsPath = getDataDirectory().resolve("questions.json");
        if (!Files.exists(questionsPath)) {
            try (InputStream defaults = getClass().getResourceAsStream("/defaults/questions.json")) {
                if (defaults == null) {
                    throw new IOException("defaults/questions.json fehlt im Plugin");
                }
                Files.copy(defaults, questionsPath);
            }
        }

        questions = gson.fromJson(Files.readString(questionsPath, StandardCharsets.UTF_8), QuestionsConfig.class);
        validateQuestions();

        approvalsPath = getDataDirectory().resolve("approvals.json");
        if (Files.exists(approvalsPath)) {
            ApprovalStore store = gson.fromJson(
                    Files.readString(approvalsPath, StandardCharsets.UTF_8),
                    ApprovalStore.class
            );
            if (store != null && store.approvals != null) {
                approvals.putAll(store.approvals);
            }
        }

        Path propertyPricingPath = getDataDirectory().resolve("property-pricing.json");
        if (!Files.exists(propertyPricingPath)) {
            try (InputStream defaults = getClass().getResourceAsStream("/defaults/property-pricing.json")) {
                if (defaults == null) {
                    throw new IOException("defaults/property-pricing.json fehlt im Plugin");
                }
                Files.copy(defaults, propertyPricingPath);
            }
        }
        propertyPricing = gson.fromJson(
                Files.readString(propertyPricingPath, StandardCharsets.UTF_8),
                PropertyPricing.class
        );
        validatePropertyPricing();

        propertyProgressPath = getDataDirectory().resolve("property-progress.json");
        if (Files.exists(propertyProgressPath)) {
            PropertyProgressStore store = gson.fromJson(
                    Files.readString(propertyProgressPath, StandardCharsets.UTF_8),
                    PropertyProgressStore.class
            );
            if (store != null && store.highestOwned != null) {
                propertyProgress.putAll(store.highestOwned);
            }
        }
    }

    private void validatePropertyPricing() {
        if (propertyPricing == null || propertyPricing.worldKey == null || propertyPricing.worldKey.isBlank()) {
            throw new IllegalStateException("Bauwelt fehlt in property-pricing.json");
        }
        if (propertyPricing.plotPrices == null || propertyPricing.plotPrices.isEmpty()) {
            throw new IllegalStateException("Grundstueckspreise fehlen");
        }
        int previous = -1;
        for (Integer price : propertyPricing.plotPrices) {
            if (price == null || price < 0 || price < previous) {
                throw new IllegalStateException("Grundstueckspreise muessen aufsteigend und nicht negativ sein");
            }
            previous = price;
        }
    }

    private void validateQuestions() {
        if (questions == null || questions.rulesVersion == null || questions.rulesVersion.isBlank()) {
            throw new IllegalStateException("rulesVersion fehlt");
        }
        if (questions.docsUrl == null || questions.docsUrl.isBlank()) {
            throw new IllegalStateException("docsUrl fehlt");
        }
        if (questions.questions == null || questions.questions.isEmpty()) {
            throw new IllegalStateException("Keine Onboarding-Fragen konfiguriert");
        }
        if (questions.questionsPerAttempt < 1 || questions.questionsPerAttempt > questions.questions.size()) {
            throw new IllegalStateException("questionsPerAttempt ist ungueltig");
        }
        if (questions.requiredCorrect < 1 || questions.requiredCorrect > questions.questionsPerAttempt) {
            throw new IllegalStateException("requiredCorrect ist ungueltig");
        }
        for (Question question : questions.questions) {
            if (question.answers == null || question.answers.size() < 2
                    || question.correctIndex < 0 || question.correctIndex >= question.answers.size()) {
                throw new IllegalStateException("Ungueltige Frage: " + question.id);
            }
        }
    }

    private synchronized void saveApprovals() {
        if (approvalsPath == null) {
            return;
        }
        ApprovalStore store = new ApprovalStore();
        store.approvals.putAll(approvals);
        Path temporary = approvalsPath.resolveSibling("approvals.json.tmp");
        try {
            Files.writeString(
                    temporary,
                    gson.toJson(store),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
            try {
                Files.move(temporary, approvalsPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, approvalsPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            getLogger().atSevere().withCause(exception).log("Freischaltungen konnten nicht gespeichert werden");
        }
    }

    private synchronized void savePropertyProgress() {
        if (propertyProgressPath == null) {
            return;
        }
        PropertyProgressStore store = new PropertyProgressStore();
        store.highestOwned.putAll(propertyProgress);
        Path temporary = propertyProgressPath.resolveSibling("property-progress.json.tmp");
        try {
            Files.writeString(
                    temporary,
                    gson.toJson(store),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
            try {
                Files.move(temporary, propertyProgressPath, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, propertyProgressPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            getLogger().atSevere().withCause(exception).log("Grundstuecksfortschritt konnte nicht gespeichert werden");
        }
    }

    private int currentPlotCount(GlymeraPlotWorld plotWorld, UUID uuid) {
        return plotWorld.plotsOfPlayer(uuid.toString()).size();
    }

    private int nextPlotPrice(UUID uuid, int currentPlots) {
        int highestOwned = propertyProgress.getOrDefault(uuid.toString(), 0);
        int priceIndex = Math.max(currentPlots, highestOwned);
        priceIndex = Math.min(priceIndex, propertyPricing.plotPrices.size() - 1);
        return propertyPricing.plotPrices.get(priceIndex);
    }

    private void showPropertyStatus(PlayerRef playerRef) {
        GlymeraPlotWorld plotWorld = GlymeraPlotWorld.get();
        if (plotWorld == null) {
            playerRef.sendMessage(Message.raw("Die Bauwelt ist derzeit nicht verfuegbar."));
            return;
        }
        int currentPlots = currentPlotCount(plotWorld, playerRef.getUuid());
        int maximum = propertyPricing.plotPrices.size();
        if (currentPlots >= maximum) {
            playerRef.sendMessage(Message.raw("Du besitzt das Maximum von " + maximum + " Bauwelt-Grundstuecken."));
            return;
        }
        int price = nextPlotPrice(playerRef.getUuid(), currentPlots);
        String priceText = price == 0 ? "kostenlos" : price + " " + propertyPricing.currencyName;
        playerRef.sendMessage(Message.raw(
                "Bauwelt: " + currentPlots + "/" + maximum + " Grundstuecke. Naechstes Grundstueck: "
                        + priceText + ". Stelle dich auf ein freies Grundstueck und nutze /grundstueck kaufen."
        ));
    }

    private void buyProperty(Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef, World world) {
        UUID uuid = playerRef.getUuid();
        if (isGuest(uuid)) {
            notifyGuestGuard(playerRef);
            return;
        }
        if (world == null || !propertyPricing.worldKey.equals(world.getName())) {
            playerRef.sendMessage(Message.raw("Grundstuecke koennen nur direkt in der Bauwelt gekauft werden."));
            return;
        }
        if (propertyTransactions.putIfAbsent(uuid, Boolean.TRUE) != null) {
            playerRef.sendMessage(Message.raw("Ein Grundstueckskauf fuer dich wird bereits verarbeitet."));
            return;
        }

        Economy transactionEconomy = null;
        int transactionPrice = 0;
        boolean withdrawn = false;
        boolean completed = false;
        GlymeraPlotWorld transactionPlotWorld = null;
        boolean deedIssued = false;
        try {
            GlymeraPlotWorld plotWorld = GlymeraPlotWorld.get();
            if (plotWorld == null) {
                playerRef.sendMessage(Message.raw("Die Bauwelt ist derzeit nicht verfuegbar."));
                return;
            }
            transactionPlotWorld = plotWorld;
            int currentPlots = currentPlotCount(plotWorld, uuid);
            int maximum = propertyPricing.plotPrices.size();
            if (currentPlots >= maximum) {
                playerRef.sendMessage(Message.raw("Du besitzt bereits das Maximum von " + maximum + " Grundstuecken."));
                return;
            }

            int price = nextPlotPrice(uuid, currentPlots);
            Economy economy = null;
            if (price > 0) {
                economy = VaultUnlocked.economyObj();
                if (economy == null) {
                    playerRef.sendMessage(Message.raw("Die Wirtschaft ist derzeit nicht verfuegbar."));
                    return;
                }
                if (!economy.hasAccount(uuid)) {
                    economy.createAccount(uuid, playerRef.getUsername(), true);
                }
                BigDecimal amount = BigDecimal.valueOf(price);
                if (!economy.has(ECONOMY_CONTEXT, uuid, amount)) {
                    playerRef.sendMessage(Message.raw("Dafuer benoetigst du " + price + " "
                            + propertyPricing.currencyName + "."));
                    return;
                }
                EconomyResponse response = economy.withdraw(ECONOMY_CONTEXT, uuid, amount);
                if (response == null || !response.transactionSuccess()) {
                    playerRef.sendMessage(Message.raw("Die Zahlung wurde abgelehnt; es wurde kein Grundstueck vergeben."));
                    return;
                }
                withdrawn = true;
                transactionEconomy = economy;
                transactionPrice = price;
            }

            if (plotWorld.claimTokenRequired(uuid)) {
                plotWorld.giveToken(store, ref, PLOT_DEED_ID);
                deedIssued = true;
            }
            String result = plotWorld.claimUnderFeet(store, ref, world, playerRef);
            int updatedPlots = currentPlotCount(plotWorld, uuid);
            if (updatedPlots <= currentPlots) {
                plotWorld.consumeToken(store, ref, PLOT_DEED_ID);
                deedIssued = false;
                if (withdrawn && economy != null) {
                    economy.deposit(ECONOMY_CONTEXT, uuid, BigDecimal.valueOf(price));
                    withdrawn = false;
                }
                playerRef.sendMessage(Message.raw("Kein Kauf: " + result));
                return;
            }

            deedIssued = false;
            propertyProgress.merge(uuid.toString(), updatedPlots, Math::max);
            savePropertyProgress();
            completed = true;
            String paid = price == 0 ? "kostenlos" : "fuer " + price + " " + propertyPricing.currencyName;
            playerRef.sendMessage(Message.raw("Grundstueck " + paid + " erworben. " + updatedPlots + "/"
                    + maximum + " Grundstuecke belegt."));
        } catch (Exception exception) {
            getLogger().atSevere().withCause(exception).log("Grundstueckskauf fuer %s fehlgeschlagen", uuid);
            playerRef.sendMessage(Message.raw("Der Grundstueckskauf ist fehlgeschlagen. Bitte melde dich beim Team."));
        } finally {
            if (deedIssued && !completed && transactionPlotWorld != null) {
                try {
                    transactionPlotWorld.consumeToken(store, ref, PLOT_DEED_ID);
                } catch (Exception cleanupError) {
                    getLogger().atSevere().withCause(cleanupError).log(
                            "Temporare Plot Deed fuer %s konnte nicht entfernt werden",
                            uuid
                    );
                }
            }
            if (withdrawn && !completed && transactionEconomy != null) {
                try {
                    transactionEconomy.deposit(
                            ECONOMY_CONTEXT,
                            uuid,
                            BigDecimal.valueOf(transactionPrice)
                    );
                } catch (Exception refundError) {
                    getLogger().atSevere().withCause(refundError).log(
                            "Rueckerstattung von %d Gold fuer %s fehlgeschlagen",
                            transactionPrice,
                            uuid
                    );
                }
            }
            propertyTransactions.remove(uuid);
        }
    }

    private static final class UnlockCommand extends AbstractPlayerCommand {
        private final NonSinnPublicCore plugin;

        private UnlockCommand(NonSinnPublicCore plugin) {
            super("freischalten", "Regeltest starten");
            this.plugin = plugin;
        }

        @Override
        public boolean canGeneratePermission() {
            return false;
        }

        @Override
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref,
                               PlayerRef playerRef, World world) {
            plugin.startQuiz(playerRef);
        }
    }

    private static final class AnswerCommand extends AbstractPlayerCommand {
        private final NonSinnPublicCore plugin;
        private final RequiredArg<Integer> answerArg;

        private AnswerCommand(NonSinnPublicCore plugin) {
            super("antwort", "Antwort im Regeltest geben");
            this.plugin = plugin;
            this.answerArg = withRequiredArg("nummer", "Nummer der Antwort", ArgTypes.INTEGER);
        }

        @Override
        public boolean canGeneratePermission() {
            return false;
        }

        @Override
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref,
                               PlayerRef playerRef, World world) {
            plugin.answer(playerRef, answerArg.get(context));
        }
    }

    private static final class RulesCommand extends AbstractPlayerCommand {
        private final NonSinnPublicCore plugin;

        private RulesCommand(NonSinnPublicCore plugin) {
            super("regeln", "Regeln und aktuelle Regelversion anzeigen");
            this.plugin = plugin;
            addAliases("rules");
        }

        @Override
        public boolean canGeneratePermission() {
            return false;
        }

        @Override
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref,
                               PlayerRef playerRef, World world) {
            context.sendMessage(Message.raw("Online-Handbuch öffnen (klicken)").link(plugin.questions.docsUrl));
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) {
                return;
            }
            if (player.getPageManager().getCustomPage() != null) {
                context.sendMessage(Message.raw("Schliesse zuerst das bereits geoeffnete Fenster."));
                return;
            }
            player.getPageManager().openCustomPage(ref, store, new HandbookPage(playerRef, "rules"));
        }
    }

    private static final class WebsiteCommand extends AbstractPlayerCommand {
        private final NonSinnPublicCore plugin;

        private WebsiteCommand(NonSinnPublicCore plugin) {
            super("webseite", "Online-Handbuch im Browser oeffnen");
            this.plugin = plugin;
            addAliases("website", "wiki");
        }

        @Override
        public boolean canGeneratePermission() {
            return false;
        }

        @Override
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref,
                               PlayerRef playerRef, World world) {
            context.sendMessage(Message.raw("Online-Handbuch öffnen (klicken)").link(plugin.questions.docsUrl));
        }
    }

    private static final class HandbookCommand extends AbstractPlayerCommand {
        private HandbookCommand() {
            super("handbuch", "Ingame-Handbuch oeffnen");
            addAliases("guide");
        }

        @Override
        public boolean canGeneratePermission() {
            return false;
        }

        @Override
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref,
                               PlayerRef playerRef, World world) {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) {
                return;
            }
            if (player.getPageManager().getCustomPage() != null) {
                context.sendMessage(Message.raw("Schliesse zuerst das bereits geoeffnete Fenster."));
                return;
            }
            player.getPageManager().openCustomPage(ref, store, new HandbookPage(playerRef, "start"));
        }
    }

    private static final class HandbookPage extends InteractiveCustomUIPage<HandbookEventData> {
        private static final String[] NAV_IDS = {
                "#NavStart", "#NavRules", "#NavCommands", "#NavWorlds", "#NavClaims",
                "#NavEconomy", "#NavRPG", "#NavTech", "#NavFarming"
        };
        private static final String[] NAV_ACTIONS = {
                "start", "rules", "commands", "worlds", "claims",
                "economy", "rpg", "tech", "farming"
        };

        private String selected;
        private boolean templateAppended;

        private HandbookPage(PlayerRef playerRef, String selected) {
            super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, HandbookEventData.CODEC);
            this.selected = selected;
        }

        @Override
        public void build(Ref<EntityStore> ref, UICommandBuilder commands, UIEventBuilder events,
                          Store<EntityStore> store) {
            if (!templateAppended) {
                commands.append("Pages/WaldbrandHandbook.ui");
                events.addEventBinding(
                        CustomUIEventBindingType.Activating,
                        "#CloseButton",
                        EventData.of("Action", "close"),
                        false
                );
                for (int index = 0; index < NAV_IDS.length; index++) {
                    events.addEventBinding(
                            CustomUIEventBindingType.Activating,
                            NAV_IDS[index],
                            EventData.of("Action", NAV_ACTIONS[index]),
                            false
                    );
                }
                templateAppended = true;
            }
            applyContent(commands);
        }

        @Override
        public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, HandbookEventData data) {
            if (data == null || data.action == null) {
                return;
            }
            if ("close".equals(data.action)) {
                Player player = store.getComponent(ref, Player.getComponentType());
                if (player != null) {
                    player.getPageManager().setPage(ref, store, Page.None);
                }
                return;
            }
            for (String action : NAV_ACTIONS) {
                if (action.equals(data.action)) {
                    selected = action;
                    UICommandBuilder commands = new UICommandBuilder();
                    applyContent(commands);
                    sendUpdate(commands, false);
                    return;
                }
            }
        }

        private void applyContent(UICommandBuilder commands) {
            HandbookContent content = HandbookContent.forSection(selected);
            commands.set("#ContentTitle.Text", content.title);
            commands.set("#ContentLead.Text", content.lead);
            commands.set("#SectionOneTitle.Text", content.sectionOneTitle);
            commands.set("#SectionOneBody.Text", content.sectionOneBody);
            commands.set("#SectionTwoTitle.Text", content.sectionTwoTitle);
            commands.set("#SectionTwoBody.Text", content.sectionTwoBody);
            commands.set("#SectionThreeTitle.Text", content.sectionThreeTitle);
            commands.set("#SectionThreeBody.Text", content.sectionThreeBody);
            commands.set("#SectionFourTitle.Text", content.sectionFourTitle);
            commands.set("#SectionFourBody.Text", content.sectionFourBody);
            commands.set("#SectionFiveTitle.Text", content.sectionFiveTitle);
            commands.set("#SectionFiveBody.Text", content.sectionFiveBody);
            commands.set("#SectionSixTitle.Text", content.sectionSixTitle);
            commands.set("#SectionSixBody.Text", content.sectionSixBody);
        }
    }

    private static final class HandbookEventData {
        private static final BuilderCodec<HandbookEventData> CODEC = BuilderCodec
                .builder(HandbookEventData.class, HandbookEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING),
                        (data, value) -> data.action = value,
                        data -> data.action)
                .add()
                .build();

        private String action;
    }

    private record HandbookContent(
            String title,
            String lead,
            String sectionOneTitle,
            String sectionOneBody,
            String sectionTwoTitle,
            String sectionTwoBody,
            String sectionThreeTitle,
            String sectionThreeBody,
            String sectionFourTitle,
            String sectionFourBody,
            String sectionFiveTitle,
            String sectionFiveBody,
            String sectionSixTitle,
            String sectionSixBody
    ) {
        private static HandbookContent forSection(String section) {
            return switch (section) {
                case "rules" -> new HandbookContent(
                        "2. Regeln & Fairplay",
                        "Hier stehen die Grundregeln fuer ein harmonisches Zusammenleben. /regeln oeffnet dieses Kapitel direkt.",
                        "Respekt & Miteinander",
                        "Behandle andere so, wie du selbst behandelt werden moechtest. Beleidigungen, Belaestigung, Diskriminierung, Drohungen, Spam und Betrug sind streng verboten. Konflikte klaeren wir sachlich oder ueber das Team.",
                        "Schutz fremden Eigentums",
                        "Fremde Bauten, Kisten, Maschinen, Tiere und Dekorationen sind unantastbar. Griefing und Diebstahl fuehren zum Ausschluss. Auch ungesicherte Gebiete duerfen nicht ungefragt veraendert werden.",
                        "Bauen in der Nachbarschaft",
                        "Halte Abstand zu fremden Bauwerken. Blockiere keine Strassen, Wege, Portale, NPC-Plaetze oder Nachbargrundstuecke. Riesige unschoene 1x1-Tuerme oder Schandflecke werden entfernt.",
                        "Exploits, Bugs & Cheats",
                        "Das Ausnutzen von Fehlern (Item-Verdopplung, Geld-Glitches, Rechte-Bypaesse) ist untersagt. Wenn du einen Fehler findest, melde ihn vertraulich dem Team und behalte ihn fuer dich.",
                        "Grenzen der Automation",
                        "Automatische Farmen (Worker, Farmer) muessen im Rahmen bleiben und duerfen die Serverleistung nicht beeintraechtigen. Uebermaessige Lag-Maschinen werden ohne Vorwarnung zurueckgebaut.",
                        "Welten & Konsequenzen",
                        "Die Survivalwelt (Default) kann nach Ankuendigung erneuert werden. Baue Dauerhaftes stets in der Bauwelt. Bei Regelverstoessen folgen Verwarnung, Rollback oder dauerhafter Bann."
                );
                case "commands" -> new HandbookContent(
                        "3. Vollstaendige Befehlsliste",
                        "Alle wichtigen Spielerbefehle auf einen Blick. Druecke Enter, tippe den Befehl ein und bestaetige mit Enter.",
                        "Allgemeine Befehle",
                        "/handbuch (oder /guide): Oeffnet dieses Kompendium.\n/regeln: Zeigt die Regeln.\n/webseite (oder /website, /wiki): Sendet den Link zur offiziellen Doku im Chat.\n/help: Allgemeine Hilfe.",
                        "Geld & Wirtschaft",
                        "/money: Zeigt dein aktuelles Goldguthaben an.\n/pay <Spieler> <Betrag>: Ueberweist Gold (z.B. /pay Frosty 50).\nHinweis: /shop ist gesperrt – Handel laeuft direkt ueber die NPCs in Glutwacht!",
                        "Survival-Claims (in Default)",
                        "/claim map: Oeffnet die visuelle Chunk-Karte.\n/claim info: Infos zum Gebiet unter dir.\n/claim list: Liste deiner Claims.\n/claim members: Mitgliederverwaltung.\n/claim show: Grenzen 30s hervorheben.",
                        "Zusaetzliche Claim-Chunks",
                        "/claim buychunk [Anzahl]: Kauft zusaetzliche Claim-Chunks fuer je 32 Claim-Scherben (bis zum Server-Maximum von 9 Chunks).\n/claim help: Zeigt alle verfuegbaren Claim-Optionen.",
                        "Bauwelt & Grundstuecke",
                        "/grundstueck (oder /bauplatz): Zeigt Status und naechsten Preis.\n/grundstueck kaufen: Kauft den freien 64x64-Plot unter deinen Fuessen.\n/plot: Plot-Menue oeffnen.\n/plot help: Plot-Befehle anzeigen.",
                        "Fahrzeuge & Steuerung",
                        "/gvehicles exit: Verlaesst ein Fahrzeug oder Boot sicher.\n/gvehicles clean: Raeumt herrenlose Fahrzeuge auf.\nTipp: Tab-Taste vervollstaendigt Spielernamen und Befehle automatisch!"
                );
                case "worlds" -> new HandbookContent(
                        "4. Welten & Portale",
                        "Der Waldbrand bietet verschiedene Welten fuer jeden Zweck. Portale im Hub bringen dich an dein Ziel.",
                        "Glutwacht (Der Himmelshub)",
                        "Die friedliche Stadt ueber den Wolken. Hier starten alle neuen Gaeste. Du findest hier Marktstaende mit 7 Fachhaendlern, das Kopfgeldbrett fuer Quests und das zentrale Portalnetzwerk.",
                        "Default (Die Survival-Wildnis)",
                        "Die klassische Survivalwelt zum Rohstoffsammeln, Hoehlenerkunden und Kaempfen. Hier sicherst du Land mit /claim. Wichtig: Default kann in Zukunft nach Vorankuendigung erneuert werden.",
                        "Bauwelt (Dauerhaftes Zuhause)",
                        "Hier liegen geschuetzte 64x64-Grundstuecke im festen Strassenraster. Friedlich eingestellt: Kein PvP, kein Fallschaden, keine feindlichen Mobs und pausierte Zeit fuer perfektes Bauen.",
                        "Oakhaven (Aetherhaven RPG)",
                        "Eine atmosphaerische Rollenspiel-Siedlung mit eigenen Dorfbewohnern, Musikern, NPCs, Quests und Haendlern. Wird ueber die vorgesehene Quest-Reise betreten.",
                        "Schwebende Inseln (Endgame)",
                        "Gefaehrliche Himmelsinseln voller Ruinen und Dungeons (GlymeraStructures). Achtung: Toedlicher Fallschaden und hochstufige Monster, dafuer aber legendare Beute!",
                        "Under & Limbo (Spezialwelten)",
                        "Under ist die finstere Unterwelt (Hytales End-Dimension), Limbo die gluehende Feuerwelt (Hytales Nether). Beide Dimensionen besitzen eigene Portalzugaenge und seltene Erze."
                );
                case "claims" -> new HandbookContent(
                        "5. Claims & Grundstuecke",
                        "Wie schuetze ich mein Eigentum? Wir erklaeren Survival-Claims und Bauwelt-Grundstuecke Schritt fuer Schritt.",
                        "Survival-Claims Schritt-fuer-Schritt",
                        "1. Suche einen freien Platz in 'default'.\n2. Tippe /claim map ein – ein 16x16-Chunk-Raster oeffnet sich.\n3. Klicke auf die Chunks, die du beanspruchen willst.\n4. 3 Chunks sind sofort kostenlos!",
                        "Claim erweitern & Mitspieler",
                        "Mit /claim buychunk kaufst du bis zu 9 Chunks fuer Claim-Scherben. Mit /claim members fuegst du Freunde hinzu und erlaubst ihnen Bauen, Kistenzugriff oder Tuerbenutzung. /claim show zeigt Grenzen.",
                        "Bauwelt-Grundstueck kaufen",
                        "1. Gehe durch das Bauwelt-Portal in Glutwacht.\n2. Laufe zu einem freien, leeren 64x64-Plot.\n3. Stelle dich mitten auf die Flaeche.\n4. Tippe /grundstueck kaufen ein.\n5. Bei 'Build away!' gehoert es dir!",
                        "Preise der Bauwelt-Plots",
                        "1. Grundstück: Kostenlos (0 Gold)\n2. Grundstück: 1.000 Gold\n3. Grundstück: 3.000 Gold\n4. Grundstück: 7.500 Gold\n(Maximal 4 Grundstuecke pro Spieler moeglich).",
                        "Plot-Verwaltung (/plot)",
                        "/plot oeffnet dein Verwaltungsmenue.\n/plot trust <Spieler>: Erlaubt Freunden das Mitbauen.\n/plot untrust <Spieler>: Entzieht die Rechte.\n/plot merge: Verbindet angrenzende Plots zu einem Riesen-Areal!",
                        "Wichtigster Unterschied",
                        "Claims in Default schuetzen vor Spielern, koennen aber bei einem Welt-Reset erneuert werden. Bauwelt-Grundstuecke bleiben dauerhaft fuer immer erhalten und werden niemals geloescht!"
                );
                case "economy" -> new HandbookContent(
                        "6. Wirtschaft, Haendler & Geld",
                        "Wie verdiene ich Gold? Wo kaufe ich Gegenstaende ein? Hier erfaehrst du alles zum Geldsystem.",
                        "Wie verdiene ich Gold?",
                        "1. Kopfgeld-Auftraege am Brett in Glutwacht erfuellen.\n2. Ueberschuessige Erze, Nahrung, Traenke und Beute bei Haendlern verkaufen.\n3. Monster besiegen.\n4. Mit Spielern ueber /pay handeln.",
                        "Die 7 Haendler in Glutwacht",
                        "Mira: Nahrung & Proviant | Brom: Schmiede, Werkzeuge & Waffen | Yara: Alchemie & Traenke | Rova: Taverne & Verpflegung | Elowen: Deko & Moebel | Tamo: Baustoffe | Syra: Seltene Beute & Schatze.",
                        "Gebrauchte Werkzeuge verkaufen",
                        "Unser Schmied Brom kauft auch benutzte Holz-, Stein- und Kupferwerkzeuge an! Der Ankaufspreis skaliert fair nach der restlichen Haltbarkeit (z.B. 50% Haltbarkeit = halber Goldpreis).",
                        "Das Kopfgeldbrett (Bounties)",
                        "Das Questbrett in Glutwacht bietet stündliche, taegliche und woechentliche Aufgaben fuer Gold, Bounty-Token und Skill-XP. Beschaffungsvertraege kaufen Baustoffe in grossen Mengen an.",
                        "Wofuer brauche ich Gold?",
                        "Gold fliesst in wertvolle Ausruestung, seltene Deko, Claim-Scherben, Bauwelt-Grundstuecke (bis 7.500 Gold) und Spieler-Handel. Preise sind serverseitig fair ausbalanciert.",
                        "Geld-Befehle im Spiel",
                        "/money zeigt deinen aktuellen Kontostand.\n/pay <Spieler> <Betrag> ueberweist Gold an Mitspieler (z.B. /pay Simon 100).\nGehandelt wird direkt an den NPC-Staenden vor Ort."
                );
                case "rpg" -> new HandbookContent(
                        "7. RPG, Skills & Klassen",
                        "Dein Charakter waechst mit jedem Abenteuer! Lerne alles ueber Klassen, Talentbaeume und Beute.",
                        "Natural20 Klassen & Attribute",
                        "Nach der Freischaltung waehlst du einmalig deine Klasse (Krieger, Schurke, Magier, Kleriker). Jede Klasse startet mit passender Ausruestung und Attributen (Staerke, Geschick, Mana, etc.).",
                        "Ausruestung & Seltenheiten",
                        "Monster und Kisten droppen Waffen und Ruestungen mit einzigartigen Attributen. Seltenheitsstufen: Gewoehnlich (Grau), Ungewoehnlich (Gruen), Selten (Blau), Episch (Lila), Legendaer (Gold).",
                        "MMO Skill Tree (Talente)",
                        "Fuer Monsterkaempfe, Bergbau, Holzfaellen und Farmen erhaeltst du Skill-XP. Investiere verdiente Skillpunkte in Talentbaeume fuer mehr Miningspeed, Leben, Schaden und Tragkraft.",
                        "MmoMobScaling (Gegnerstufen)",
                        "In gefaehrlichen Aussenwelten (wie den Schwebenden Inseln) passen sich Monster deiner Stufe an. Hoehere Level bedeuten haertere Kaempfe, aber auch deutlich mehr Beute und Gold!",
                        "Aetherhaven Quests & Oakhaven",
                        "In der Siedlung Oakhaven warten Dorfbewohner mit spannenden Aufgaben. Schliesse Quests ab fuer Aetherhaven-Muenzen, exklusive Bauplaene und Ruf in der Siedlung.",
                        "Zaehmung & Begleiter",
                        "Zaehme Wildtiere mit passendem Futter (AlecsTamework). Rekrutiere Begleiter (GlymeraCompanion), die dir im Kampf beistehen, dich beschuetzen und Gegenstaende tragen."
                );
                case "tech" -> new HandbookContent(
                        "8. Technik, Bergbau & Lager",
                        "Moderne Hilfsmittel fuer effizientes Bauen, 3x3-Bergbau und automatisches Kistensortieren.",
                        "NonSinn's MiningTweaks (3x3)",
                        "Bergbauhammer baut 3x3 Gestein mit einem Schlag ab! Flaechenschaufel graebt 3x3 Erde/Sand. An der Bergbauwerkbank craften, an der Montagebank Haltbarkeit und Tempo anpassen!",
                        "Splitter-Recycling",
                        "Alte Modifikator-Teile koennen an der Montagebank direkt in Splitter zurueckgewonnen werden (Standard=1, Praezise=2, Meisterlich=3 Splitter). Kein Material geht verloren!",
                        "AutoStorage (Kisten-Sortierung)",
                        "Nie wieder Unordnung! Platziere Kisten in deiner Basis. Lege Items in die Eingangskiste – AutoStorage sortiert sie vollautomatisch in passende Kisten im Umkreis (14x6) ein.",
                        "GrabFromFar (Reichweite)",
                        "Beim Craften an Werkbaenken musst du Zutaten nicht im Inventar tragen: Werkbaenke greifen automatisch auf bis zu 200 Kisten im Umkreis von 16 Bloecken horizontal und 8 vertikal zu!",
                        "Macaw's Moebel & Deko",
                        "Hunderte neue Moebelstuecke: Stuehle, Tische, Schraenke, Fenster, Fensterlaeden, Tueren, Lampen, Bruecken, Treppen, Daecher, Gemaelde und Wandbehange fuer dein Traumhaus.",
                        "Bluestone & Lichtbloecke",
                        "GlymeraBluestone ermoeglicht Schaltungen, Druckplatten und Fallen. GlymeraLightBlock und GlymeraLantern sorgen fuer stimmungsvolle Beleuchtung ohne nervige Fackeln."
                );
                case "farming" -> new HandbookContent(
                        "9. Natur, Helfer & Fahrzeuge",
                        "Landwirtschaft, Imkerei, Tierzucht und praktische Arbeitshelfer fuer deine Farm.",
                        "Bienen & Imkerei (GlymeraBees)",
                        "Platziere Bienenstoecke in der Naehe von Blumen. Sammle regelmaessig Honigwaben und Honig fuer staerkende Nahrung, Trankbrauen und wertvollen Haendlerverkauf.",
                        "Bewaesserung & Komposter",
                        "Irrigation: Verlege Wasserleitungen und Sprinkler fuer automatische Feldbewaesserung.\nComposter: Verwerte Pflanzenreste und Laub zu wertvollem Duenger fuer schnelleres Wachstum.",
                        "Farm- und Arbeitshelfer",
                        "GlymeraFarmer erntet und saet Felder automatisch neu an. GlymeraWorker hilft bei Holz- und Erzarbeiten. Helfer arbeiten treu innerhalb deiner Grundstuecksgrenzen.",
                        "Baumzucht & Pilzkulturen",
                        "GlymeraSaplings bietet seltene Baumsetzlinge fuer edle Hoelzer. GlymeraMushrooms ermoeglicht Pilzanbau in dunklen Kellern oder Gewaechshaeusern.",
                        "Schutz vor Wildtieren",
                        "SlothGuard und Schutzzaeune sichern deine Ernte und Zuchttiere vor wilden Kreaturen und Raeubern.",
                        "Fahrzeuge & Reittiere",
                        "Baue Boote, Floesse und Luftschiffe (GlymeraVehicles) zum Bereisen entfernter Kontinente. Zaehme Pferde und Reittiere (TravelingMounts) fuer rasante Landreisen!"
                );
                default -> new HandbookContent(
                        "1. Start & Onboarding",
                        "Willkommen auf Der Waldbrand! Dieses Handbuch fuehrt dich Schritt fuer Schritt durch den Server.",
                        "1. Ankunft in Glutwacht",
                        "Du landest sicher in der Himmelsstadt Glutwacht. Als Gast kannst du dich frei umsehen. Die Stadt ist vor Griefing geschuetzt; deine ersten Schritte sind ganz einfach.",
                        "2. Warum bin ich Gast?",
                        "Gaeste koennen noch nicht abbauen, bauen, kaempfen oder Kisten oeffnen. Das schuetzt den Server vor Bots. Die Freischaltung dauert nur 1 Minute!",
                        "3. Freischalten Schritt-fuer-Schritt",
                        "1. Tippe /regeln ein und lies die Regeln.\n2. Tippe /freischalten ein (6 kurze Fragen).\n3. Antworte mit /antwort <Nummer> (z.B. /antwort 1).\n4. Bei 5 richtigen Antworten bist du sofort Spieler!",
                        "4. Klasse & Startausruestung",
                        "Nach bestandener Freischaltung waehlst du deine RPG-Klasse (Krieger, Schurke, Magier etc.). Deine Startgegenstaende landen direkt im Inventar und bleiben dauerhaft gespeichert.",
                        "5. Wichtige Tastenkombinationen",
                        "Druecke Enter zum Chatten und Befehle eingeben. /handbuch oeffnet dieses Hilfefenster jederzeit wieder. /money zeigt dein Gold.",
                        "6. Wohin als Naechstes?",
                        "Gehe zum grossen Portalplatz in Glutwacht. Von dort fuehren Portale in die Survival-Wildnis (Default), die friedliche Bauwelt oder gefaehrliche Abenteuerwelten!"
                );
            };
        }
    }

    private static final class PropertyCommand extends AbstractPlayerCommand {
        private final NonSinnPublicCore plugin;

        private PropertyCommand(NonSinnPublicCore plugin) {
            super("grundstueck", "Bauwelt-Grundstuecke anzeigen und kaufen");
            this.plugin = plugin;
            addAliases("bauplatz");
            addSubCommand(new BuyPropertyCommand(plugin));
        }

        @Override
        public boolean canGeneratePermission() {
            return false;
        }

        @Override
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref,
                               PlayerRef playerRef, World world) {
            plugin.showPropertyStatus(playerRef);
        }
    }

    private static final class BuyPropertyCommand extends AbstractPlayerCommand {
        private final NonSinnPublicCore plugin;

        private BuyPropertyCommand(NonSinnPublicCore plugin) {
            super("kaufen", "Freies Bauwelt-Grundstueck kaufen");
            this.plugin = plugin;
        }

        @Override
        public boolean canGeneratePermission() {
            return false;
        }

        @Override
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref,
                               PlayerRef playerRef, World world) {
            plugin.buyProperty(store, ref, playerRef, world);
        }
    }

    private static final class BuildSpawnPlazaCommand extends AbstractAsyncCommand {
        private final NonSinnPublicCore plugin;

        private BuildSpawnPlazaCommand(NonSinnPublicCore plugin) {
            super("nspcbuildspawn", "Erzeugt den zentralen Spawn-Platz in der Bauwelt");
            this.plugin = plugin;
        }

        @Override
        public boolean canGeneratePermission() {
            return false;
        }

        @Override
        protected CompletableFuture<Void> executeAsync(CommandContext context) {
            plugin.buildBauweltSpawnPlaza();
            context.sendMessage(Message.raw("100x100 RPG-Spawn-Sanctuary wird generiert..."));
            return CompletableFuture.completedFuture(null);
        }
    }

    public void buildBauweltSpawnPlaza() {
        World bauwelt = Universe.get().getWorld("bauwelt");
        if (bauwelt != null) {
            bauwelt.getWorldConfig().setPvpEnabled(false);
            bauwelt.getWorldConfig().setFallDamageEnabled(false);
            bauwelt.getWorldConfig().setSpawningNPC(false);
            bauwelt.getWorldConfig().setIsSpawnMarkersEnabled(false);
            bauwelt.getWorldConfig().setIsAllNPCFrozen(true);
            bauwelt.getWorldConfig().setGameTimePaused(true);
            bauwelt.getWorldConfig().markChanged();
            SpawnPlazaBuilder.build(bauwelt, this);
        } else {
            Universe.get().loadWorld("bauwelt").thenAccept(w -> {
                if (w != null) {
                    w.getWorldConfig().setPvpEnabled(false);
                    w.getWorldConfig().setFallDamageEnabled(false);
                    w.getWorldConfig().setSpawningNPC(false);
                    w.getWorldConfig().setIsSpawnMarkersEnabled(false);
                    w.getWorldConfig().setIsAllNPCFrozen(true);
                    w.getWorldConfig().setGameTimePaused(true);
                    w.getWorldConfig().markChanged();
                    SpawnPlazaBuilder.build(w, this);
                } else {
                    getLogger().atWarning().log("Konnte Welt 'bauwelt' nicht laden!");
                }
            }).exceptionally(ex -> {
                getLogger().atWarning().withCause(ex).log("Fehler beim Laden von Welt 'bauwelt'");
                return null;
            });
        }
    }

    private static final class QuizSession {
        private final List<Integer> order;
        private int position;
        private int correct;

        private QuizSession(List<Integer> order) {
            this.order = order;
        }
    }

    private static final class QuestionsConfig {
        private String rulesVersion;
        private String docsUrl;
        private int requiredCorrect;
        private int questionsPerAttempt;
        private int attemptCooldownSeconds;
        private List<Question> questions = new ArrayList<>();
    }

    private static final class Question {
        private String id;
        private String question;
        private List<String> answers = new ArrayList<>();
        private int correctIndex;
        private String explanation;
    }

    private static final class Approval {
        private String rulesVersion;
        private String acceptedAt;
        private int score;
        private int total;
    }

    private static final class ApprovalStore {
        private int schemaVersion = 1;
        private Map<String, Approval> approvals = new LinkedHashMap<>();
    }

    private static final class PropertyPricing {
        private int schemaVersion;
        private String worldKey;
        private String currencyName;
        private List<Integer> plotPrices = new ArrayList<>();
    }

    private static final class PropertyProgressStore {
        private int schemaVersion = 1;
        private Map<String, Integer> highestOwned = new LinkedHashMap<>();
    }
}
