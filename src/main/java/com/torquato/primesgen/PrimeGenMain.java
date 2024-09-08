package com.torquato.primesgen;

import akka.actor.typed.ActorSystem;

public class PrimeGenMain {
    public static void main(String[] args) {
        final ActorSystem<ManagerBehavior.Command> manager = ActorSystem.create(
                ManagerBehavior.create(),
                "Manager"
        );
        manager.tell(new ManagerBehavior.InstructionCommand("start"));
    }
}