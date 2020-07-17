package lishy2.treefarm.util;


import lishy2.treefarm.Treefarm;
import lishy2.treefarm.gui.CultivatorBlockScreen;
import lishy2.treefarm.gui.PlanterBlockScreen;
import lishy2.treefarm.gui.WoodCutterBlockScreen;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


@Mod.EventBusSubscriber(modid = Treefarm.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class ClientEventBusSubscriber {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        ScreenManager.registerFactory(RegistryHandler.PLANTER_CONTAINER.get(), PlanterBlockScreen::new);
        ScreenManager.registerFactory(RegistryHandler.CULTIVATOR_CONTAINER.get(), CultivatorBlockScreen::new);
        ScreenManager.registerFactory(RegistryHandler.WOOD_CUTTER_CONTAINER.get(), WoodCutterBlockScreen::new);
    }
}
