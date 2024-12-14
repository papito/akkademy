import akkademy.iot.DeviceManager
import akkademy.iot.DeviceManager.DeviceRegistered
import akkademy.iot.DeviceManager.RequestTrackDevice
import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.funsuite.AnyFunSuiteLike

class IotDeviceManagerTest extends ScalaTestWithActorTestKit with AnyFunSuiteLike {

  test("DeviceManager actor must reply to registration requests") {
    val probe = createTestProbe[DeviceRegistered]()
    val managerActor = spawn(DeviceManager())

    managerActor ! RequestTrackDevice("group1", "device", probe.ref)
    val registered1 = probe.receiveMessage()

    // another group
    managerActor ! RequestTrackDevice("group2", "device", probe.ref)
    val registered2 = probe.receiveMessage()

    assert(registered1.device != registered2.device)
  }
}
