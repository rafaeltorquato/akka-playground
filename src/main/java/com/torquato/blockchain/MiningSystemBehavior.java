package com.torquato.blockchain;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MiningSystemBehavior extends AbstractBehavior<ManagerBehavior.Command> {

    private final PoolRouter<ManagerBehavior.Command> managerPoolRouter;

    //Class Stuff
    private final ActorRef<ManagerBehavior.Command> managers;
    private MiningSystemBehavior(ActorContext<ManagerBehavior.Command> context) {
        super(context);
        this.managerPoolRouter = Routers.pool(
                3,
                Behaviors.supervise(ManagerBehavior.create()).onFailure(SupervisorStrategy.restart())
        );
        this.managers = getContext().spawn(this.managerPoolRouter, "ManagerPool");
    }

    public static Behavior<ManagerBehavior.Command> create() {
        return Behaviors.setup(MiningSystemBehavior::new);
    }

    @Override
    public Receive<ManagerBehavior.Command> createReceive() {
        return newReceiveBuilder()
                .onAnyMessage((command) -> {
                    this.managers.tell(command);

                    return Behaviors.same();
                })
                .build();
    }
}
