package li.cil.oc.server.driver.block

import li.cil.oc.api.driver
import li.cil.oc.server.component
import net.minecraft.block.Block
import net.minecraft.item.{ItemBlock, ItemStack}
import net.minecraft.tileentity.TileEntityCommandBlock
import net.minecraft.world.World

object CommandBlock extends driver.Block {
  def worksWith(world: World, x: Int, y: Int, z: Int) =
    world.getBlockId(x, y, z) == Block.commandBlock.blockID

  def worksWith(stack: ItemStack) = stack != null && (stack.getItem match {
    case itemBlock: ItemBlock => itemBlock.getBlockID == Block.commandBlock.blockID
    case _ => false
  })

  def createEnvironment(world: World, x: Int, y: Int, z: Int) =
    world.getBlockTileEntity(x, y, z) match {
      case block: TileEntityCommandBlock => new component.CommandBlock(block)
      case _ => null
    }
}
