import java.net.InetAddress

import scala.collection.JavaConverters._
import scala.util.Try
import scala.util.control.NonFatal

import org.bitlet.weupnp._

object UPnP {
  def discover: (Map[InetAddress, GatewayDevice], Seq[GatewayDevice]) = {
    val discover = new GatewayDiscover
    (discover.discover.asScala.toMap,
    discover.getAllGateways.asScala.toMap.values.toSeq)
  }

  def getPortMappings(device: GatewayDevice): Seq[PortMappingEntry] = {

    def loop(i: Int, mappings: Seq[PortMappingEntry]): Seq[PortMappingEntry] = {
      val entry = new PortMappingEntry
      if (device.getGenericPortMappingEntry(i, entry)) {
        loop(i + 1, entry +: mappings)
      } else mappings.reverse
    }

    loop(0, Seq.empty)
  }

  def showDevice(ip: InetAddress, device: GatewayDevice): String =
    s"""
       |Interface:    ${ip.getHostAddress}
       |Name:         ${device.getFriendlyName}
       |Model:        ${device.getModelName}
       |Manufacturer: ${device.getManufacturer}
       |Description:  ${device.getModelDescription}
       |Type:         ${device.getDeviceType}
       |Search type:  ${device.getSt}
       |Service type: ${device.getServiceType}
       |Location:     ${device.getLocation}
       |External IP:  ${device.getExternalIPAddress}
       |Connected:    ${Try(device.isConnected).filter(identity).map(_ => "yes").getOrElse("no")}
    |""".stripMargin

  val showPortMappingHeader: String = {
    val externalPort = "%1$-8s".format("Extern")
    val internalPort = "%1$-8s".format("Intern")
    val internalClient = "%1$-15s".format("Host")
    val protocol = "%1$-10s".format("Protocol")
    val description = "Description"
    s"$protocol $externalPort $internalClient $internalPort $description"
  }

  def showPortMapping(m: PortMappingEntry): String = {
    val externalPort = "%1$-8s".format(s"${m.getExternalPort}")
    val internalPort = "%1$-8s".format(s"${m.getInternalPort}")
    val internalClient = "%1$-15s".format(m.getInternalClient)
    val protocol = "%1$-10s".format(m.getProtocol)
    val description = m.getPortMappingDescription
    s"$protocol $externalPort $internalClient $internalPort $description"
  }

  def addPort(device: GatewayDevice, port: Int): Either[String, Boolean] =
    try {
      Right(device.addPortMapping(port, port, device.getLocalAddress.getHostAddress, "TCP", "RChain"))
    } catch {
      case NonFatal(ex: Exception) => Left(ex.toString)
    }

  def removePort(device: GatewayDevice, port: Int): Either[String, Unit] =
    try {
      device.deletePortMapping(port, "TCP")
      Right(())
    } catch {
      case NonFatal(ex: Exception) => Left(ex.toString)
    }
}
