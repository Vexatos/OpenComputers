package li.cil.oc.integration.gregtech.gregtech6

import li.cil.oc.api
import li.cil.oc.integration.{ModProxy, Mods}
import net.minecraftforge.common.MinecraftForge

object ModGregtech6 extends ModProxy {
  override def getMod = Mods.GregTech6

  override def initialize() {
    api.IMC.registerToolDurabilityProvider("li.cil.oc.integration.gregtech.gregtech6.EventHandlerGregTech.getDurability")

    MinecraftForge.EVENT_BUS.register(EventHandlerGregTech)

    // TODO RecipeHandler.init()
  }
}
