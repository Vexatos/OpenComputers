package li.cil.oc.server.component

import li.cil.oc.api
import li.cil.oc.api.network.{LuaCallback, Context, Arguments, Visibility}
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntityNote

class NoteBlock(entity: TileEntityNote) extends ManagedComponent {
  val node = api.Network.newNode(this, Visibility.Network).
    withComponent("note_block").
    create()

  // ----------------------------------------------------------------------- //

  @LuaCallback("getPitch")
  def getPitch(context: Context, args: Arguments): Array[AnyRef] = result(entity.note + 1)

  @LuaCallback("setPitch")
  def setPitch(context: Context, args: Arguments): Array[AnyRef] = {
    setPitch(args.checkInteger(0))
    result(true)
  }

  @LuaCallback("trigger")
  def trigger(context: Context, args: Arguments): Array[AnyRef] = {
    if (args.count > 0) {
      setPitch(args.checkInteger(0))
    }

    val world = entity.getWorldObj
    val (x, y, z) = (entity.xCoord, entity.yCoord, entity.zCoord)
    entity.triggerNote(world, x, y, z)
    result(world.getBlockMaterial(x, y + 1, z) == Material.air)
  }

  def setPitch(value: Int) {
    if (value < 1 || value > 25) throw new IllegalArgumentException("invalid pitch")
    entity.note = (value - 1).toByte
    entity.onInventoryChanged()
  }
}
