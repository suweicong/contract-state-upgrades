package com.upgrade

import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.transactions.LedgerTransaction

// Upgraded contracts must implement the UpgradedContract interface.
// We're also upgrading the state, so we pass the references to the old and new state as the input and output state.
open class NewContractAndState : UpgradedContract<OldState, NewContractAndState.NewState> {
    companion object {
        val id = "com.upgrade.NewContractAndState"
    }

    override val legacyContract = OldContract.id

    // We're upgrading the state as well, so we modify the OldState.
    override fun upgrade(state: OldState) = NewContractAndState.NewState(state.a, state.b, 1)

    // Definition of the new state
    data class NewState(val a: AbstractParty, val b: AbstractParty, val value: Int) : ContractState {
        override val participants get() = listOf(a, b)
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        requireThat {
            when (command.value) {
                is NewContractAndState.Commands.Action -> {
                    "Value should be greater than 0" using (tx.outputsOfType<NewState>().single().value > 0)
                }
            }
        }
    }

    interface Commands : CommandData{
        class Action : Commands
    }
}