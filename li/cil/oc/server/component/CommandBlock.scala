package li.cil.oc.server.component

import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.api.network.{LuaCallback, Context, Arguments, Visibility}
import net.minecraft.tileentity.TileEntityCommandBlock

class CommandBlock(entity: TileEntityCommandBlock) extends ManagedComponent {
  val node = api.Network.newNode(this, Visibility.Network).
    withComponent("command_block").
    create()

  // ----------------------------------------------------------------------- //

  @LuaCallback("getValue")
  def getValue(context: Context, args: Arguments): Array[AnyRef] = result(entity.getCommand)

  @LuaCallback("setValue")
  def setValue(context: Context, args: Arguments): Array[AnyRef] = {
    val value = args.checkString(0)
    entity.setCommand(value)
    entity.worldObj.markBlockForUpdate(entity.xCoord, entity.yCoord, entity.zCoord)
    result(true)
  }

  @LuaCallback("run")
  def run(context: Context, args: Arguments): Array[AnyRef] = {
    val name = if (Settings.get.commandUser != null && !Settings.get.commandUser.isEmpty)
      Settings.get.commandUser
    else
      context.address
    entity.setCommandSenderName(name)
    context.pause(0.1) // Make sure the command block has time to do its thing.
    result(entity.executeCommandOnPowered(entity.worldObj))
  }
}
