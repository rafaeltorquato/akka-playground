package com.torquato.primesgen;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.time.Duration;
import java.util.SortedSet;
import java.util.concurrent.CompletionStage;

@Slf4j
public class PrimeGenMain {
    public static void main(String[] args) {
        final ActorSystem<ManagerBehavior.Command> manager = ActorSystem.create(
                ManagerBehavior.create(),
                "Manager"
        );
        final CompletionStage<SortedSet<BigInteger>> result = AskPattern.ask(
                manager,
                (me) -> new ManagerBehavior.InstructionCommand("start", me),
                Duration.ofSeconds(30),
                manager.scheduler()
        );
        result.whenComplete((reply, throwable) -> {
            if (reply != null) {
                log.info("Reply is: {}", reply);
            } else {
                log.warn("The System didn't respond in time");
            }
            manager.terminate();
        });
    }
}