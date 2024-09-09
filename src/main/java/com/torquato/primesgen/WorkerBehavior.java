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
        return uncachedReceive();
    }

    private Receive<Command> uncachedReceive() {
        return newReceiveBuilder()
                .onMessage(Command.class, (command) -> {
                    BigInteger probablePrime = null;
                    if ("start".equals(command.message)) {
                        final BigInteger number = new BigInteger(2000, new Random());
                        probablePrime = number.nextProbablePrime();
                        command.sender.tell(new ManagerBehavior.ResultCommand(probablePrime));
                    }
                    return cachedReceive(probablePrime);
                })
                .build();
    }

    private Receive<Command> cachedReceive(BigInteger generatedPrime) {
        return newReceiveBuilder()
                .onMessage(Command.class, (command) -> {
                    if ("start".equals(command.message)) {
                        log.info("Cache HIT!");
                        command.sender.tell(new ManagerBehavior.ResultCommand(generatedPrime));
                    }
                    return Behaviors.same();
                })
                .build();
    }
}
