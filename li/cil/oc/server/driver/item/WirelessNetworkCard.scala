package li.cil.oc.server.driver.item

import li.cil.oc.Items
import li.cil.oc.api.driver.Slot
import li.cil.oc.server.component
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.{TileEntity => MCTileEntity}

object WirelessNetworkCard extends Item {
  def worksWith(stack: ItemStack) = isOneOf(stack, Items.wlan)

  override def createEnvironment(stack: ItemStack, container: MCTileEntity) =
    if (container != null) new component.WirelessNetworkCard(container) else null

  def slot(stack: ItemStack) = Slot.Card
}
