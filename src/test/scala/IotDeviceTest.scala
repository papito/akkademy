import akkademy.iot.Device
import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.funsuite.AnyFunSuiteLike

class IotDeviceTest extends ScalaTestWithActorTestKit with AnyFunSuiteLike {
  import Device._

  test("Device actor must reply with empty reading if no temperature is known") {
    val probe = createTestProbe[RespondTemperature]()
    val deviceActor = spawn(Device("group", "device"))

    deviceActor ! Device.ReadTemperature(requestId = 42, probe.ref)
    val response = probe.receiveMessage()
    assert(response.requestId == 42)
    assert(response.value.isEmpty)
  }

  test("Device actor must reply with latest temperature reading") {
    val recordProbe = createTestProbe[TemperatureRecorded]()
    val readProbe = createTestProbe[RespondTemperature]()
    val deviceActor = spawn(Device("group", "device"))

    deviceActor ! Device.RecordTemperature(requestId = 1, 24.0, recordProbe.ref)
    recordProbe.expectMessage(Device.TemperatureRecorded(requestId = 1))

    deviceActor ! Device.ReadTemperature(requestId = 2, readProbe.ref)
    val response1 = readProbe.receiveMessage()
    assert(response1.requestId == 2)
    assert(response1.value.contains(24.0))

    deviceActor ! Device.RecordTemperature(requestId = 3, 55.0, recordProbe.ref)
    recordProbe.expectMessage(Device.TemperatureRecorded(requestId = 3))

    deviceActor ! Device.ReadTemperature(requestId = 4, readProbe.ref)
    val response2 = readProbe.receiveMessage()
    assert(response2.requestId == 4)
    assert(response2.value.contains(55.0))
  }
}
