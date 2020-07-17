package lishy2.treefarm.containers;

import lishy2.treefarm.entities.CultivatorBlockEntity;
import lishy2.treefarm.util.RegistryHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class CultivatorContainer extends ItemHolderContainer<CultivatorContainer> {

    public CultivatorContainer(final int windowId, final PlayerInventory playerInventory, final CultivatorBlockEntity tileEntity) {
        super(windowId, playerInventory, tileEntity, RegistryHandler.CULTIVATOR_CONTAINER, RegistryHandler.CULTIVATOR_BLOCK);
    }

    public CultivatorContainer(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
        super(windowId, playerInventory, data, RegistryHandler.CULTIVATOR_CONTAINER, RegistryHandler.CULTIVATOR_BLOCK);
    }
}
