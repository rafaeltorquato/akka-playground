package com.torquato;

import akka.actor.typed.ActorSystem;
import com.torquato.primesgen.FirstSimpleBehavior;

public class Main {
    public static void main(String[] args) {
        final ActorSystem<String> actorSystem = ActorSystem.create(
                FirstSimpleBehavior.create(),
                "FirstActorSystem"
        );
        actorSystem.tell("say hello");
        actorSystem.tell("who are you");
        actorSystem.tell("Second message!");
    }
}