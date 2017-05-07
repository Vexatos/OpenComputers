package li.cil.oc.integration.opencomputers

import li.cil.oc.Constants
import li.cil.oc.api
import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.driver.item.HostAware
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.common
import li.cil.oc.common.Slot
import li.cil.oc.common.Tier
import li.cil.oc.common.item.Delegator
import li.cil.oc.server.component
import net.minecraft.item.ItemStack

object DriverGraphicsCard extends Item with HostAware {
  override def worksWith(stack: ItemStack) = isOneOf(stack,
    api.Items.get(Constants.ItemName.GraphicsCardTier1),
    api.Items.get(Constants.ItemName.GraphicsCardTier2),
    api.Items.get(Constants.ItemName.GraphicsCardTier3),
    api.Items.get(Constants.ItemName.GraphicsCardTier4))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost) =
    if (host.world != null && host.world.isRemote) null
    else realTier(stack) match {
      case Tier.One => new component.GraphicsCard(Tier.One)
      case Tier.Two => new component.GraphicsCard(Tier.Two)
      case Tier.Three => new component.GraphicsCard(Tier.Three)
      case Tier.Four => new component.GraphicsCard(Tier.Four)
      case _ => null
    }

  override def slot(stack: ItemStack) = Slot.Card

  def realTier(stack: ItemStack): Int =
    Delegator.subItem(stack) match {
      case Some(gpu: common.item.GraphicsCard) => gpu.gpuTier
      case _ => Tier.One
    }

  override def tier(stack: ItemStack): Int =
    realTier(stack) match {
      case Tier.Four => Tier.One
      case t => t
    }

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[_] =
      if (worksWith(stack))
        classOf[component.GraphicsCard]
      else null
  }

}
