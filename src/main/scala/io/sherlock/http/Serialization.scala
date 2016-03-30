package io.sherlock.http

import io.sherlock.core.{ HeartBeat, Service }

trait Serialization extends spray.json.DefaultJsonProtocol {
  implicit val heartBeatFormat = jsonFormat3(HeartBeat)
  implicit val getHealthFormat = jsonFormat1(Service.Result)
}
