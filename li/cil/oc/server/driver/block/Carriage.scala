package li.cil.oc.server.driver.block

import li.cil.oc.api.driver
import li.cil.oc.server.component
import li.cil.oc.util.mods.RedstoneInMotion
import net.minecraft.world.World
import net.minecraft.block.Block
import net.minecraft.item.ItemStack

object Carriage extends driver.Block {
  def worksWith(world: World, x: Int, y: Int, z: Int) =
    Option(world.getBlockTileEntity(x, y, z)) match {
      case Some(entity) if RedstoneInMotion.isCarriageController(entity) => true
      case _ => false
    }

  def worksWith(stack: ItemStack) = RedstoneInMotion.isCarriageController(stack)

  def createEnvironment(world: World, x: Int, y: Int, z: Int) =
    world.getBlockTileEntity(x, y, z) match {
      case entity if RedstoneInMotion.isCarriageController(entity) => new component.Carriage(entity)
      case _ => null
    }
}
