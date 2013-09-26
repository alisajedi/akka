/**
 * Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */

package akka.routing

import akka.testkit.AkkaSpec
import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.LocalActorRef
import scala.concurrent.duration._
import akka.actor.Identify
import akka.actor.ActorIdentity

@org.junit.runner.RunWith(classOf[org.scalatest.junit.JUnitRunner])
class RouteeCreationSpec extends AkkaSpec {

  "Creating Routees" must {

    "result in visible routees" in {
      val N = 100
      system.actorOf(Props(new Actor {
        system.actorSelection(self.path).tell(Identify(self.path), testActor)
        def receive = Actor.emptyBehavior
      }).withRouter(RoundRobinPool(N)))
      for (i ← 1 to N) {
        expectMsgType[ActorIdentity] match {
          case ActorIdentity(_, Some(_)) ⇒ // fine
          case x                         ⇒ fail(s"routee $i was not found $x")
        }
      }
    }

    "allow sending to context.parent" in {
      val N = 100
      system.actorOf(Props(new Actor {
        context.parent ! "one"
        def receive = {
          case "one" ⇒ testActor forward "two"
        }
      }).withRouter(RoundRobinPool(N)))
      val gotit = receiveWhile(messages = N) {
        case "two" ⇒ lastSender.toString
      }
      expectNoMsg(100.millis)
      if (gotit.size != N) {
        fail(s"got only ${gotit.size} from [${gotit mkString ", "}]")
      }
    }

  }

}