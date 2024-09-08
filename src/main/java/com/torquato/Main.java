package com.torquato;

import akka.actor.typed.ActorSystem;
import com.torquato.primesgen.FirstSimpleBehavior;

public class Main {
    public static void main(String[] args) {
        final ActorSystem<String> actorSystem = ActorSystem.create(
                FirstSimpleBehavior.create(),
                "FirstActorSystem"
        );
        actorSystem.tell("Hello Actor!");
        actorSystem.tell("Second message!");
    }
}