object Main {
  def main(args: Array[String]): Unit = {
    val (all, gateways) = UPnP.discover

    println()

    if(gateways.isEmpty)
      println("No gateway devices found")
    else {
      println("Gateway devices:")
      gateways.foreach(d => println(d.getFriendlyName))
    }

    all.foreach {
      case (ip, d) =>
        println(UPnP.showDevice(ip, d))
        println(UPnP.showPortMappingHeader)
        UPnP.getPortMappings(d).foreach(m => println(UPnP.showPortMapping(m)))
        println()
    }
  }
}
