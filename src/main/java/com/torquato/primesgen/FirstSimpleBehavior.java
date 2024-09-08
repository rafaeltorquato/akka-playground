package com.torquato.primesgen;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FirstSimpleBehavior extends AbstractBehavior<String> {

    public static Behavior<String> create() {
        return Behaviors.setup(FirstSimpleBehavior::new);
    }

    private FirstSimpleBehavior(ActorContext<String> context) {
        super(context);
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder()
                .onAnyMessage(message -> {
                    log.info("Received message {}", message);
                    return this;
                })
                .build();
    }
}
