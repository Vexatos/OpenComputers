package li.cil.oc.integration.gregtech.gregtech5

import li.cil.oc.api
import li.cil.oc.api.Driver
import li.cil.oc.integration.{ModProxy, Mods}
import net.minecraftforge.common.MinecraftForge

object ModGregtech5 extends ModProxy {
  override def getMod = Mods.GregTech5

  override def initialize() {
    api.IMC.registerToolDurabilityProvider("li.cil.oc.integration.gregtech.gregtech5.EventHandlerGregTech.getDurability")

    MinecraftForge.EVENT_BUS.register(EventHandlerGregTech)

    Driver.add(new DriverEnergyContainer)

    RecipeHandler.init()
  }
}
