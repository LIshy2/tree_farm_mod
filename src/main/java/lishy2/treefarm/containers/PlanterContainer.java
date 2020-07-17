package lishy2.treefarm.containers;

import lishy2.treefarm.entities.PlanterBlockEntity;
import lishy2.treefarm.util.RegistryHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;


public class PlanterContainer extends ItemHolderContainer<PlanterContainer> {
    public PlanterContainer(final int windowId, final PlayerInventory playerInventory, final PlanterBlockEntity data) {
        super(windowId, playerInventory, data, RegistryHandler.PLANTER_CONTAINER, RegistryHandler.PLANTER_BLOCK);
    }

    public PlanterContainer(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
        super(windowId, playerInventory, data, RegistryHandler.PLANTER_CONTAINER, RegistryHandler.PLANTER_BLOCK);

    }
}
