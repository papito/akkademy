import akkademy.iot.Device
import akkademy.iot.Device.Command
import akkademy.iot.DeviceGroupQuery
import akkademy.iot.DeviceGroupQuery.WrappedRespondTemperature
import akkademy.iot.DeviceManager.DeviceNotAvailable
import akkademy.iot.DeviceManager.DeviceTimedOut
import akkademy.iot.DeviceManager.RespondAllTemperatures
import akkademy.iot.DeviceManager.Temperature
import akkademy.iot.DeviceManager.TemperatureNotAvailable
import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.funsuite.AnyFunSuiteLike

import scala.concurrent.duration._

class IotDeviceGroupQueryTest extends ScalaTestWithActorTestKit with AnyFunSuiteLike {

  test("DeviceGroupQuery must return temperature value for working devices") {
    val requester = createTestProbe[RespondAllTemperatures]()

    val device1 = createTestProbe[Command]()
    val device2 = createTestProbe[Command]()

    val deviceIdToActor = Map("device1" -> device1.ref, "device2" -> device2.ref)

    val queryActor =
      spawn(DeviceGroupQuery(deviceIdToActor, requestId = 1, requester = requester.ref, timeout = 3.seconds))

    device1.expectMessageType[Device.ReadTemperature]
    device2.expectMessageType[Device.ReadTemperature]

    queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device1", Some(1.0)))
    queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device2", Some(2.0)))

    requester.expectMessage(
      RespondAllTemperatures(
        requestId = 1,
        temperatures = Map("device1" -> Temperature(1.0), "device2" -> Temperature(2.0))))
  }

  test("DeviceGroupQuery must return TemperatureNotAvailable for devices with no readings") {
    val requester = createTestProbe[RespondAllTemperatures]()

    val device1 = createTestProbe[Command]()
    val device2 = createTestProbe[Command]()

    val deviceIdToActor = Map("device1" -> device1.ref, "device2" -> device2.ref)

    val queryActor =
      spawn(DeviceGroupQuery(deviceIdToActor, requestId = 1, requester = requester.ref, timeout = 3.seconds))

    device1.expectMessageType[Device.ReadTemperature]
    device2.expectMessageType[Device.ReadTemperature]

    queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device1", None))
    queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device2", Some(2.0)))

    requester.expectMessage(
      RespondAllTemperatures(
        requestId = 1,
        temperatures = Map("device1" -> TemperatureNotAvailable, "device2" -> Temperature(2.0))))
  }

  test("DeviceGroupQuery must return DeviceNotAvailable if device stops before answering") {
    val requester = createTestProbe[RespondAllTemperatures]()

    val device1 = createTestProbe[Command]()
    val device2 = createTestProbe[Command]()

    val deviceIdToActor = Map("device1" -> device1.ref, "device2" -> device2.ref)

    val queryActor =
      spawn(DeviceGroupQuery(deviceIdToActor, requestId = 1, requester = requester.ref, timeout = 3.seconds))

    device1.expectMessageType[Device.ReadTemperature]
    device2.expectMessageType[Device.ReadTemperature]

    queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device1", Some(2.0)))

    device2.stop()

    requester.expectMessage(
      RespondAllTemperatures(
        requestId = 1,
        temperatures = Map("device1" -> Temperature(2.0), "device2" -> DeviceNotAvailable)))
  }

  test("DeviceGroupQuery must return temperature reading even if device stops after answering") {
    val requester = createTestProbe[RespondAllTemperatures]()

    val device1 = createTestProbe[Command]()
    val device2 = createTestProbe[Command]()

    val deviceIdToActor = Map("device1" -> device1.ref, "device2" -> device2.ref)

    val queryActor =
      spawn(DeviceGroupQuery(deviceIdToActor, requestId = 1, requester = requester.ref, timeout = 3.seconds))

    device1.expectMessageType[Device.ReadTemperature]
    device2.expectMessageType[Device.ReadTemperature]

    queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device1", Some(1.0)))
    queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device2", Some(2.0)))

    device2.stop()

    requester.expectMessage(
      RespondAllTemperatures(
        requestId = 1,
        temperatures = Map("device1" -> Temperature(1.0), "device2" -> Temperature(2.0))))
  }

  test("DeviceGroupQuery must return DeviceTimedOut if device does not answer in time") {
    val requester = createTestProbe[RespondAllTemperatures]()

    val device1 = createTestProbe[Command]()
    val device2 = createTestProbe[Command]()

    val deviceIdToActor = Map("device1" -> device1.ref, "device2" -> device2.ref)

    val queryActor =
      spawn(DeviceGroupQuery(deviceIdToActor, requestId = 1, requester = requester.ref, timeout = 200.millis))

    device1.expectMessageType[Device.ReadTemperature]
    device2.expectMessageType[Device.ReadTemperature]

    queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device1", Some(1.0)))

    // no reply from device2

    requester.expectMessage(
      RespondAllTemperatures(
        requestId = 1,
        temperatures = Map("device1" -> Temperature(1.0), "device2" -> DeviceTimedOut)))
  }
}
