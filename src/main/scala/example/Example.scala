package example

import distage._

class Incrementer(inc: Int) {
  def increment(x: Int): Int = x + inc
}

class MyApp(incrementerFactory: () => Incrementer) {
  def run(): Unit = {
    println(s"The result is: ${incrementerFactory().increment(10)}")
  }
}


// Normally running with this module fails:
// Exception in thread "main" izumi.distage.model.exceptions.ProvisioningException: Provisioner stopped after 1 instances, 1/4 operations failed:
// - {type.scala.Int} (Example.scala:27), MissingInstanceException: Instance is not available in the object graph: {type.scala.Int}. required by refs: Set({type.scala.Function0[+Incrementor]})
object AutoFactoryModule extends ModuleDef {
  make[Int].from(0)
  make[Incrementer].from(() => new Incrementer(5))
  make[() => Incrementer]
  make[MyApp]
}

object App1 extends App {
  val injector = Injector()
  val plan = injector.plan(AutoFactoryModule, GCMode.NoGC)
  println(plan)
  Thread.sleep(1000)
  val locator = injector.produceUnsafe(plan)
  val app = locator.get[MyApp]
  app.run()
}
