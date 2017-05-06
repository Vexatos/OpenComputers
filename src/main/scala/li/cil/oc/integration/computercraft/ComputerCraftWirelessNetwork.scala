package li.cil.oc.integration.computercraft

import java.util

import com.google.common.base.Charsets
import dan200.computercraft.api.ComputerCraftAPI
import dan200.computercraft.api.network.IPacketReceiver
import dan200.computercraft.api.network.IPacketSender
import dan200.computercraft.api.network.Packet
import li.cil.oc.api
import li.cil.oc.api.network
import li.cil.oc.api.network.WirelessEndpoint
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

import scala.collection.convert.WrapAsJava._

object ComputerCraftWirelessNetwork {

  def sendWirelessPacket(source: WirelessEndpoint, strength: Double, packet: network.Packet): Unit = {
    val payload: util.Map[Int, AnyRef] = mapAsJavaMap(packet.data.collect {
      case b: Array[Byte] => new String(b, Charsets.UTF_8)
      case v => v
    }.zipWithIndex.map(t => t._2 + 1 -> t._1).toMap)
    ComputerCraftAPI.getWirelessNetwork.transmit(
      new Packet(packet.port, packet.port, payload, strength, false, Sender(source, packet.source))
    )
  }

  def addReceiver(receiver: WirelessEndpoint): Unit = {
    ComputerCraftAPI.getWirelessNetwork.addReceiver(Receiver(receiver))
  }

  def removeReceiver(receiver: WirelessEndpoint): Unit = {
    ComputerCraftAPI.getWirelessNetwork.removeReceiver(Receiver(receiver))
  }

  abstract class Device(val point: WirelessEndpoint) {
    def getWorld: World = point.world()

    def getPosition: Vec3d = new Vec3d(point.x + 0.5, point.y + 0.5, point.z + 0.5)
  }

  case class Sender(override val point: WirelessEndpoint, name: String) extends Device(point) with IPacketSender {
    override def getSenderID: String = name
  }

  case class Receiver(override val point: WirelessEndpoint) extends Device(point) with IPacketReceiver {
    override def getRange: Double = 0

    override def isInterdimensional: Boolean = false

    override def receiveSameDimension(packet: Packet, v: Double): Unit = try packet.getSender match {
      case d: Device if this.point.equals(d.point) => // Our own packet
      case _ => point.receivePacket(
        api.Network.newPacket("cc" + packet.getSender.getSenderID, null, packet.getChannel, Array(packet.getPayload)),
        FakeEndpoint(packet.getSender)
      )
    } catch {
      case _: Exception => // Probably an unsupported data type like a table
    }

    override def receiveDifferentDimension(packet: Packet): Unit = try packet.getSender match {
      case d: Device if this.point.equals(d.point) => // Our own packet
      case _ => point.receivePacket(
        api.Network.newPacket("cc" + packet.getSender.getSenderID, null, packet.getChannel, Array(packet.getPayload)),
        FakeEndpoint(packet.getSender))
    } catch {
      case _: Exception => // Probably an unsupported data type like a table
    }
  }

  case class FakeEndpoint(sender: IPacketSender) extends WirelessEndpoint {

    override def x(): Int = sender.getPosition.xCoord.toInt

    override def y(): Int = sender.getPosition.yCoord.toInt

    override def z(): Int = sender.getPosition.zCoord.toInt

    override def world(): World = sender.getWorld

    override def receivePacket(packet: network.Packet, source: WirelessEndpoint) {} // Senders don't receive.
  }

}
