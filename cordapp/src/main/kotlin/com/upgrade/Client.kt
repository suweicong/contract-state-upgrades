package com.upgrade

import net.corda.client.rpc.CordaRPCClient
import net.corda.core.flows.ContractUpgradeFlow
import net.corda.core.utilities.NetworkHostAndPort.Companion.parse
import net.corda.core.utilities.loggerFor
import org.slf4j.Logger

fun main(args: Array<String>) {
    UpgradeContractStateClient().main(args)
}

/**
 *  A utility demonstrating the contract upgrade process.
 *  In this case, we are upgrading the states' contracts, but not the states
 *  themselves.
 **/
private class UpgradeContractStateClient {
    companion object {
        val logger: Logger = loggerFor<UpgradeContractStateClient>()
    }

    fun main(args: Array<String>) {
        require(args.size == 2) { "Usage: TemplateClient <PartyA RPC address> <PartyB RPC address>" }

        // Create a connection to PartyA and PartyB.
        val (partyAProxy, partyBProxy) = args.map { arg ->
            val nodeAddress = parse(arg)
            val client = CordaRPCClient(nodeAddress)
            client.start("user1", "test").proxy
        }

        // Issue a State that uses OldContract onto the ledger.
        val partyBIdentity = partyBProxy.nodeInfo().legalIdentities.first()
        partyAProxy.startFlowDynamic(Initiator::class.java, partyBIdentity)

        println("Started OldState issue flow, waiting 5 seconds")
        Thread.sleep(5000)

        // Authorise the upgrade of all the State instances using OldContract.
        listOf(partyAProxy, partyBProxy).forEach { proxy ->
            // Extract all the unconsumed State instances from the vault.
            val stateAndRefs = proxy.vaultQuery(OldState::class.java).states

            // Run the upgrade flow for each one.
            stateAndRefs
                    .filter { stateAndRef ->
                        stateAndRef.state.contract.equals(OldContract.id)
                    }.forEach { stateAndRef ->
                        proxy.startFlowDynamic(
                                ContractUpgradeFlow.Authorise::class.java,
                                stateAndRef,
                                NewContractAndState::class.java)
                    }
        }

        println("Authorizing state and contract upgrade, waiting 5 seconds")
        Thread.sleep(5000)

        // Initiate the upgrade of all the State instances using OldContract.
        partyAProxy.vaultQuery(OldState::class.java).states
                .filter { stateAndRef ->
                    stateAndRef.state.contract.equals(OldContract.id)
                }
                .forEach { stateAndRef ->
                    partyAProxy.startFlowDynamic(
                            ContractUpgradeFlow.Initiate::class.java,
                            stateAndRef,
                            NewContractAndState::class.java)
                }

        // Give the node the time to run the contract upgrade flows.
        println("Upgrading state and contract, waiting 10 seconds")
        Thread.sleep(10000)

        println("Upgraded State in Party A's vault")
        println( partyAProxy.vaultQuery(NewContractAndState.NewState::class.java).states )
        println("Upgraded State in Party B's vault")
        println( partyBProxy.vaultQuery(NewContractAndState.NewState::class.java).states)

        // Log all the State instances in the vault to show they are using NewContractAndState.
        partyAProxy.vaultQuery(NewContractAndState.NewState::class.java).states.forEach { logger.info("{}", it.state) }
    }
}
