package com.torquato.blockchain;

import akka.actor.testkit.typed.javadsl.BehaviorTestKit;
import akka.actor.testkit.typed.javadsl.TestInbox;
import com.torquato.blockchain.model.Block;
import com.torquato.blockchain.model.HashResult;
import com.torquato.blockchain.utils.BlocksData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class WorkerBehaviorTest {


    BehaviorTestKit<WorkerBehavior.Command> testBehavior;
    TestInbox<HashResult> inbox;

    @BeforeEach
    void setUp() {
        this.testBehavior = BehaviorTestKit.create(WorkerBehavior.create());
        this.inbox = TestInbox.create();
    }

    @Test
    public void shouldReceiveHashResult() {
        final HashResult expectedHashResult = new HashResult();
        expectedHashResult.foundAHash(
                "00000d8f88bd56d44a8c1d96f43ca8cd4ab1993de47c519bd4c31ab3c2964f45",
                41371
        );
        final Block block = BlocksData.getNextBlock(0, "0");
        final var command = new WorkerBehavior.BlockMiningCommand(
                block,
                41071,
                5,
                this.inbox.getRef()
        );

        this.testBehavior.run(command);

        this.inbox.expectMessage(expectedHashResult);
    }

    @Test
    public void shouldNotReceiveHashResult() {
        final Block block = BlocksData.getNextBlock(0, "0");
        final var command = new WorkerBehavior.BlockMiningCommand(
                block,
                0,
                5,
                this.inbox.getRef()
        );

        this.testBehavior.run(command);

        assertFalse(this.inbox.hasMessages());
    }
}