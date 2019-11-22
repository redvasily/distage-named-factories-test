package example

import distage._
import izumi.distage.model.Locator.LocatorRef

class Incrementor(by: Int) {
  def increment(x: Int): Int = x + by
}

class MyApp(incrementorFactory: () => Incrementor) {
  def run(): Unit = {
    println(s"The result is: ${incrementorFactory().increment(10)}")
  }
}

object ManualFactoryModule extends ModuleDef {
  make[Incrementor].from(() => new Incrementor(5))
  make[() => Incrementor].from { all: LocatorRef =>
    () => all.get.get[Incrementor]
  }
  make[MyApp]
}


// Normally running with this module fails:
// Exception in thread "main" izumi.distage.model.exceptions.ProvisioningException: Provisioner stopped after 1 instances, 1/4 operations failed:
// - {type.scala.Int} (Example.scala:27), MissingInstanceException: Instance is not available in the object graph: {type.scala.Int}. required by refs: Set({type.scala.Function0[+Incrementor]})
object AutoFactoryModule extends ModuleDef {
  make[Int].from(0)
  make[Incrementor].from(() => new Incrementor(5))
  make[() => Incrementor]
  make[MyApp]
}


object App1 extends App {
  val injector = Injector()
  val plan = injector.plan(ManualFactoryModule, GCMode.NoGC)
  println(plan)
  Thread.sleep(1000)
  val locator = injector.produceUnsafe(plan)
  val app = locator.get[MyApp]
  app.run()
}

object App2 extends App {
  val injector = Injector()
  val plan = injector.plan(AutoFactoryModule, GCMode.NoGC)
  println(plan)
  Thread.sleep(1000)
  val locator = injector.produceUnsafe(plan)
  val app = locator.get[MyApp]
  app.run()
}
