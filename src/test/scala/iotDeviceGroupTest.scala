import akkademy.iot.Device
import akkademy.iot.Device.Passivate
import akkademy.iot.Device.RecordTemperature
import akkademy.iot.Device.TemperatureRecorded
import akkademy.iot.DeviceGroup
import akkademy.iot.DeviceManager.DeviceRegistered
import akkademy.iot.DeviceManager.ReplyDeviceList
import akkademy.iot.DeviceManager.RequestAllTemperatures
import akkademy.iot.DeviceManager.RequestDeviceList
import akkademy.iot.DeviceManager.RequestTrackDevice
import akkademy.iot.DeviceManager.RespondAllTemperatures
import akkademy.iot.DeviceManager.Temperature
import akkademy.iot.DeviceManager.TemperatureNotAvailable
import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.funsuite.AnyFunSuiteLike

import scala.concurrent.duration._

class iotDeviceGroupTest extends ScalaTestWithActorTestKit with AnyFunSuiteLike {

  test("DeviceGroup actor must be able to register a device actor") {
    val probe = createTestProbe[DeviceRegistered]()
    val groupActor = spawn(DeviceGroup("group"))

    groupActor ! RequestTrackDevice("group", "device1", probe.ref)
    val registered1 = probe.receiveMessage()
    val deviceActor1 = registered1.device

    // another deviceId
    groupActor ! RequestTrackDevice("group", "device2", probe.ref)
    val registered2 = probe.receiveMessage()
    val deviceActor2 = registered2.device
    assert(deviceActor1 != deviceActor2)

    // Check that the device actors are working
    val recordProbe = createTestProbe[TemperatureRecorded]()
    deviceActor1 ! RecordTemperature(requestId = 0, 1.0, recordProbe.ref)
    recordProbe.expectMessage(TemperatureRecorded(requestId = 0))
    deviceActor2 ! Device.RecordTemperature(requestId = 1, 2.0, recordProbe.ref)
    recordProbe.expectMessage(Device.TemperatureRecorded(requestId = 1))
  }

  test("DeviceGroup actor must ignore requests for wrong groupId") {
    val probe = createTestProbe[DeviceRegistered]()
    val groupActor = spawn(DeviceGroup("group"))

    groupActor ! RequestTrackDevice("wrongGroup", "device1", probe.ref)
    probe.expectNoMessage(500.milliseconds)
  }

  test("DeviceGroup actor must return same actor for same deviceId") {
    val probe = createTestProbe[DeviceRegistered]()
    val groupActor = spawn(DeviceGroup("group"))

    groupActor ! RequestTrackDevice("group", "device1", probe.ref)
    val registered1 = probe.receiveMessage()

    // registering same again should be idempotent
    groupActor ! RequestTrackDevice("group", "device1", probe.ref)
    val registered2 = probe.receiveMessage()

    assert(registered1.device == registered2.device)
  }

  test("DeviceGroup actor must be able to list active devices") {
    val registeredProbe = createTestProbe[DeviceRegistered]()
    val groupActor = spawn(DeviceGroup("group"))

    groupActor ! RequestTrackDevice("group", "device1", registeredProbe.ref)
    registeredProbe.receiveMessage()

    groupActor ! RequestTrackDevice("group", "device2", registeredProbe.ref)
    registeredProbe.receiveMessage()

    val deviceListProbe = createTestProbe[ReplyDeviceList]()
    groupActor ! RequestDeviceList(requestId = 0, groupId = "group", deviceListProbe.ref)
    deviceListProbe.expectMessage(ReplyDeviceList(requestId = 0, Set("device1", "device2")))
  }

  test("DeviceGroup actor must be able to list active devices after one shuts down") {
    val registeredProbe = createTestProbe[DeviceRegistered]()
    val groupActor = spawn(DeviceGroup("group"))

    groupActor ! RequestTrackDevice("group", "device1", registeredProbe.ref)
    val registered1 = registeredProbe.receiveMessage()
    val toShutDown = registered1.device

    groupActor ! RequestTrackDevice("group", "device2", registeredProbe.ref)
    registeredProbe.receiveMessage()

    val deviceListProbe = createTestProbe[ReplyDeviceList]()
    groupActor ! RequestDeviceList(requestId = 0, groupId = "group", deviceListProbe.ref)
    deviceListProbe.expectMessage(ReplyDeviceList(requestId = 0, Set("device1", "device2")))

    toShutDown ! Passivate
    registeredProbe.expectTerminated(toShutDown, registeredProbe.remainingOrDefault)

    // using awaitAssert to retry because it might take longer for the groupActor
    // to see the Terminated, that order is undefined
    registeredProbe.awaitAssert {
      groupActor ! RequestDeviceList(requestId = 1, groupId = "group", deviceListProbe.ref)
      deviceListProbe.expectMessage(ReplyDeviceList(requestId = 1, Set("device2")))
    }
  }

  test("DeviceGroup actor must be able to collect temperatures from all active devices") {
    val registeredProbe = createTestProbe[DeviceRegistered]()
    val groupActor = spawn(DeviceGroup("group"))

    groupActor ! RequestTrackDevice("group", "device1", registeredProbe.ref)
    val deviceActor1 = registeredProbe.receiveMessage().device

    groupActor ! RequestTrackDevice("group", "device2", registeredProbe.ref)
    val deviceActor2 = registeredProbe.receiveMessage().device

    groupActor ! RequestTrackDevice("group", "device3", registeredProbe.ref)
    registeredProbe.receiveMessage()

    // Check that the device actors are working
    val recordProbe = createTestProbe[TemperatureRecorded]()
    deviceActor1 ! RecordTemperature(requestId = 0, 1.0, recordProbe.ref)
    recordProbe.expectMessage(TemperatureRecorded(requestId = 0))
    deviceActor2 ! RecordTemperature(requestId = 1, 2.0, recordProbe.ref)
    recordProbe.expectMessage(TemperatureRecorded(requestId = 1))
    // No temperature for device3

    val allTempProbe = createTestProbe[RespondAllTemperatures]()
    groupActor ! RequestAllTemperatures(requestId = 0, groupId = "group", allTempProbe.ref)
    allTempProbe.expectMessage(
      RespondAllTemperatures(
        requestId = 0,
        temperatures =
          Map("device1" -> Temperature(1.0), "device2" -> Temperature(2.0), "device3" -> TemperatureNotAvailable)))
  }
}
