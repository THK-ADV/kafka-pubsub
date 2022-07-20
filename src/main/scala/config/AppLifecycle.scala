package config

import scala.concurrent.Future

trait AppLifecycle {
  def onStop(f: () => Future[_]): Unit
}

