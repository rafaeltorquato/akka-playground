package com.torquato.primesgen;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Random;

@Slf4j
public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command> {

    public record Command(String message,
                          ActorRef<ManagerBehavior.Command> sender) implements Serializable {
    }

    public static Behavior<WorkerBehavior.Command> create() {
        return Behaviors.setup(WorkerBehavior::new);
    }

    private WorkerBehavior(ActorContext<WorkerBehavior.Command> context) {
        super(context);
    }

    @Override
    public Receive<WorkerBehavior.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(WorkerBehavior.Command.class, (command) -> {
                    if ("start".equals(command.message)) {
                        final BigInteger number = new BigInteger(2000, new Random());
                        final BigInteger probablePrime = number.nextProbablePrime();
                        command.sender.tell(new ManagerBehavior.ResultCommand(probablePrime));
                    }
                    return this;
                })
                .build();
    }
}
