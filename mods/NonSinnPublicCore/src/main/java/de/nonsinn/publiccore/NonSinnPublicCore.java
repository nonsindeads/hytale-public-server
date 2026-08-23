package de.nonsinn.publiccore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.CraftRecipeEvent;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.DropItemEvent;
import com.hypixel.hytale.server.core.event.events.ecs.InteractivelyPickupItemEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.event.events.player.DrainPlayerFromWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class NonSinnPublicCore extends JavaPlugin {
    private static final String HUB_WORLD = "himmelsinsel";
    private static final String PLAYER_GROUP = "spieler";
    private static final String PLOT_DEED_ID = "GlymeraPlotWorld_Deed";
    private static final String ECONOMY_CONTEXT = "NonSinnPublicCore";
    private static final UUID OWNER_UUID = UUID.fromString("2e07651a-2a27-4165-a440-5b3f7abb3392");
    private static final long NOTICE_INTERVAL_MS = 3_000L;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<UUID, QuizSession> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastGuardNotice = new ConcurrentHashMap<>();
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
        getCommandRegistry().registerCommand(new PropertyCommand(this));

        getEventRegistry().registerGlobal(PlayerConnectEvent.class, this::onPlayerConnect);
        getEventRegistry().registerGlobal(DrainPlayerFromWorldEvent.class, this::onPlayerDrain);
        getEventRegistry().registerGlobal(PlayerInteractEvent.class, this::onPlayerInteract);

        getEntityStoreRegistry().registerSystem(new GuestBreakBlockSystem(this));
        getEntityStoreRegistry().registerSystem(new GuestDamageBlockSystem(this));
        getEntityStoreRegistry().registerSystem(new GuestPlaceBlockSystem(this));
        getEntityStoreRegistry().registerSystem(new GuestUseBlockSystem(this));
        getEntityStoreRegistry().registerSystem(new GuestDropItemSystem(this));
        getEntityStoreRegistry().registerSystem(new GuestPickupItemSystem(this));
        getEntityStoreRegistry().registerSystem(new GuestCraftSystem(this));

        getLogger().atInfo().log(
                "NonSinnPublicCore geladen: Regelversion %s, %d Fragen, %d Freigaben",
                questions.rulesVersion,
                questions.questions.size(),
                approvals.size()
        );
    }

    @Override
    protected void start() {
        // LuckPerms publishes its API during its start phase. The declared
        // dependency guarantees that its start runs before ours.
        bootstrapPermissions();
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
        ownerUser.data().add(InheritanceNode.builder(owner).build());
        ownerUser.setPrimaryGroup(owner.getName());
        luckPerms.getUserManager().saveUser(ownerUser).join();

        getLogger().atInfo().log(
                "LuckPerms-Basis sichergestellt: gast, spieler, moderator, admin, owner; NonSinn=owner"
        );
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

    private void onPlayerConnect(PlayerConnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        if (!isGuest(playerRef.getUuid())) {
            return;
        }
        World hub = Universe.get().getWorld(HUB_WORLD);
        if (hub != null) {
            event.setWorld(hub);
        } else {
            getLogger().atWarning().log("Gast-Hub '%s' ist nicht geladen", HUB_WORLD);
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
            PermissionsModule.get().setUserGroup(uuid, PLAYER_GROUP);
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

        if (!PermissionsModule.get().getAllRegisteredGroups().contains(PLAYER_GROUP)) {
            playerRef.sendMessage(Message.raw("Freischaltung ist noch nicht bereit. Bitte melde dich beim Team."));
            getLogger().atSevere().log("LuckPerms-Gruppe '%s' fehlt", PLAYER_GROUP);
            return;
        }

        Approval approval = new Approval();
        approval.rulesVersion = questions.rulesVersion;
        approval.acceptedAt = Instant.now().toString();
        approval.score = session.correct;
        approval.total = session.order.size();
        approvals.put(uuid.toString(), approval);
        saveApprovals();
        PermissionsModule.get().setUserGroup(uuid, PLAYER_GROUP);
        cooldowns.remove(uuid);

        playerRef.sendMessage(Message.raw(
                "Bestanden: " + session.correct + "/" + session.order.size()
                        + ". Du bist jetzt als Spieler freigeschaltet. Willkommen!"
        ));
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
            context.sendMessage(Message.raw(
                    "Regelversion " + plugin.questions.rulesVersion + ": " + plugin.questions.docsUrl
            ));
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
