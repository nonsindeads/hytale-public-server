package de.nonsinn.publiccore;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.ICancellableEcsEvent;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.CraftRecipeEvent;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.DropItemEvent;
import com.hypixel.hytale.server.core.event.events.ecs.InteractivelyPickupItemEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Set;

abstract class GuestGuardSystem<T extends EcsEvent & ICancellableEcsEvent>
        extends EntityEventSystem<EntityStore, T> {

    private final NonSinnPublicCore plugin;

    protected GuestGuardSystem(NonSinnPublicCore plugin, Class<T> eventType) {
        super(eventType);
        this.plugin = plugin;
    }

    @Override
    public void handle(
            int index,
            ArchetypeChunk<EntityStore> chunk,
            Store<EntityStore> store,
            CommandBuffer<EntityStore> commandBuffer,
            T event
    ) {
        PlayerRef playerRef = chunk.getComponent(index, PlayerRef.getComponentType());
        if (playerRef != null && plugin.isGuest(playerRef.getUuid())) {
            event.setCancelled(true);
            plugin.notifyGuestGuard(playerRef);
        }
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }

    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return RootDependency.firstSet();
    }
}

final class GuestBreakBlockSystem extends GuestGuardSystem<BreakBlockEvent> {
    GuestBreakBlockSystem(NonSinnPublicCore plugin) {
        super(plugin, BreakBlockEvent.class);
    }
}

final class GuestDamageBlockSystem extends GuestGuardSystem<DamageBlockEvent> {
    GuestDamageBlockSystem(NonSinnPublicCore plugin) {
        super(plugin, DamageBlockEvent.class);
    }
}

final class GuestPlaceBlockSystem extends GuestGuardSystem<PlaceBlockEvent> {
    GuestPlaceBlockSystem(NonSinnPublicCore plugin) {
        super(plugin, PlaceBlockEvent.class);
    }
}

final class GuestUseBlockSystem extends GuestGuardSystem<UseBlockEvent.Pre> {
    GuestUseBlockSystem(NonSinnPublicCore plugin) {
        super(plugin, UseBlockEvent.Pre.class);
    }
}

final class GuestDropItemSystem extends GuestGuardSystem<DropItemEvent.PlayerRequest> {
    GuestDropItemSystem(NonSinnPublicCore plugin) {
        super(plugin, DropItemEvent.PlayerRequest.class);
    }
}

final class GuestPickupItemSystem extends GuestGuardSystem<InteractivelyPickupItemEvent> {
    GuestPickupItemSystem(NonSinnPublicCore plugin) {
        super(plugin, InteractivelyPickupItemEvent.class);
    }
}

final class GuestCraftSystem extends GuestGuardSystem<CraftRecipeEvent.Pre> {
    GuestCraftSystem(NonSinnPublicCore plugin) {
        super(plugin, CraftRecipeEvent.Pre.class);
    }
}
