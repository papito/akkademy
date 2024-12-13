import akkademy.iot.Device
import akkademy.iot.DeviceGroup
import akkademy.iot.DeviceManager.DeviceRegistered
import akkademy.iot.DeviceManager.ReplyDeviceList
import akkademy.iot.DeviceManager.RequestDeviceList
import akkademy.iot.DeviceManager.RequestTrackDevice
import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers

class iotTest extends ScalaTestWithActorTestKit with AnyFunSuiteLike with Matchers {

  import akkademy.iot.Device._

  test("Device actor should reply with empty reading if no temperature is known") {
    val probe = createTestProbe[RespondTemperature]()
    val deviceActor = spawn(Device("group", "device"))

    deviceActor ! Device.ReadTemperature(requestId = 42, probe.ref)
    val response = probe.receiveMessage()
    response.requestId should ===(42)
    response.value should ===(None)
  }

  test("Device actor should reply with latest temperature reading") {
    val recordProbe = createTestProbe[TemperatureRecorded]()
    val readProbe = createTestProbe[RespondTemperature]()
    val deviceActor = spawn(Device("group", "device"))

    deviceActor ! Device.RecordTemperature(requestId = 1, 24.0, recordProbe.ref)
    recordProbe.expectMessage(Device.TemperatureRecorded(requestId = 1))

    deviceActor ! Device.ReadTemperature(requestId = 2, readProbe.ref)
    val response1 = readProbe.receiveMessage()
    response1.requestId should ===(2)
    response1.value should ===(Some(24.0))

    deviceActor ! Device.RecordTemperature(requestId = 3, 55.0, recordProbe.ref)
    recordProbe.expectMessage(Device.TemperatureRecorded(requestId = 3))

    deviceActor ! Device.ReadTemperature(requestId = 4, readProbe.ref)
    val response2 = readProbe.receiveMessage()
    response2.requestId should ===(4)
    response2.value should ===(Some(55.0))
  }

  test("Device actor should be able to list active devices") {
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

  test("Device actor should be able to list active devices after one shuts down") {
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

    registeredProbe.awaitAssert {
      groupActor ! RequestDeviceList(requestId = 1, groupId = "group", deviceListProbe.ref)
      deviceListProbe.expectMessage(ReplyDeviceList(requestId = 1, Set("device2")))
    }
  }
}
