package com.torquato.racing;

import akka.actor.typed.ActorSystem;

public class RacingMain {

    public static void main(String[] args) {
        final ActorSystem<RaceController.Command> manager = ActorSystem.create(
                RaceController.create(),
                "RaceController"
        );
        manager.tell(new RaceController.StartCommand());
    }
}
