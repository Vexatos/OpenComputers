package li.cil.oc.common.tileentity

import li.cil.oc.Settings
import li.cil.oc.api.driver.Slot
import li.cil.oc.server.driver.Registry
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

class Case(var tier: Int, isRemote: Boolean) extends Computer(isRemote) {
  def this() = this(0, false)

  // ----------------------------------------------------------------------- //

  override def readFromNBT(nbt: NBTTagCompound) {
    tier = nbt.getByte(Settings.namespace + "tier") max 0 min 2
    super.readFromNBT(nbt)
  }

  override def writeToNBT(nbt: NBTTagCompound) {
    nbt.setByte(Settings.namespace + "tier", tier.toByte)
    super.writeToNBT(nbt)
  }

  // ----------------------------------------------------------------------- //

  def getInvName = Settings.namespace + "container.Case"

  def getSizeInventory = tier match {
    case 0 => 4
    case 1 => 6
    case 2 => 8
    case _ => 0
  }

  override def isUseableByPlayer(player: EntityPlayer) =
    world.getBlockTileEntity(x, y, z) match {
      case t: TileEntity if t == this && computer.canInteract(player.getCommandSenderName) =>
        player.getDistanceSq(x + 0.5, y + 0.5, z + 0.5) <= 64
      case _ => false
    }

  def isItemValidForSlot(slot: Int, stack: ItemStack) = tier match {
    case 0 => (slot, Registry.itemDriverFor(stack)) match {
      case (_, None) => false // Invalid item.
      case (0 | 1, Some(driver)) => driver.slot(stack) == Slot.Card
      case (2, Some(driver)) => driver.slot(stack) == Slot.Memory
      case (3, Some(driver)) => driver.slot(stack) == Slot.HardDiskDrive
      case _ => false // Invalid slot.
    }
    case 1 => (slot, Registry.itemDriverFor(stack)) match {
      case (_, None) => false // Invalid item.
      case (0 | 1, Some(driver)) => driver.slot(stack) == Slot.Card
      case (2 | 3, Some(driver)) => driver.slot(stack) == Slot.Memory
      case (4 | 5, Some(driver)) => driver.slot(stack) == Slot.HardDiskDrive
      case _ => false // Invalid slot.
    }
    case 2 => (slot, Registry.itemDriverFor(stack)) match {
      case (_, None) => false // Invalid item.
      case (0 | 1 | 2, Some(driver)) => driver.slot(stack) == Slot.Card
      case (3 | 4, Some(driver)) => driver.slot(stack) == Slot.Memory
      case (5 | 6, Some(driver)) => driver.slot(stack) == Slot.HardDiskDrive
      case (7, Some(driver)) => driver.slot(stack) == Slot.Disk
      case _ => false // Invalid slot.
    }
    case _ => false
  }
}