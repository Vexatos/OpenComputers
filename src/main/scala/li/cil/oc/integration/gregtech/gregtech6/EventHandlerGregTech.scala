package li.cil.oc.integration.gregtech.gregtech6

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import gregapi.item.IItemDamagable
import gregapi.item.multiitem.MultiItemTool
import li.cil.oc.api.event.RobotUsedToolEvent
import net.minecraft.item.ItemStack

object EventHandlerGregTech {
  @SubscribeEvent
  def onRobotApplyDamageRate(e: RobotUsedToolEvent.ApplyDamageRate) {
    (e.toolBeforeUse.getItem, e.toolAfterUse.getItem) match {
      case (itemBefore: IItemDamagable, itemAfter: IItemDamagable) =>
        val damage = MultiItemTool.getToolDamage(e.toolAfterUse) - MultiItemTool.getToolDamage(e.toolBeforeUse)
        if (damage > 0) {
          val actualDamage = damage * e.getDamageRate
          val repairedDamage =
            if (e.agent.player.getRNG.nextDouble() > 0.5)
              damage - math.floor(actualDamage).toInt
            else
              damage - math.ceil(actualDamage).toInt
          MultiItemTool.setToolDamage(e.toolAfterUse, MultiItemTool.getToolDamage(e.toolAfterUse) - repairedDamage)
        }
      case _ =>
    }
  }

  def getDurability(stack: ItemStack): Double = {
    stack.getItem match {
      case item: IItemDamagable => 1.0 - MultiItemTool.getToolDamage(stack).toDouble / MultiItemTool.getToolMaxDamage(stack).toDouble
      case _ => Double.NaN
    }
  }
}
