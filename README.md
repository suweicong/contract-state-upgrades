<p align="center">
  <img src="https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png" alt="Corda" width="500">
</p>

# Contract and State Upgrades CorDapp

This CorDapp shows the end-to-end process of upgrading contracts in Corda.

The upgrade takes place in four stages:

1. Create a replacement contract and state implementing the `UpgradedContract` interface
2. Bundle the replacement contract and state into a CorDapp and install it on each node
3. For each state you wish to upgrade the contract of, authorise the contract and state upgrade for that state on each node
4. On a single node, authorise the contract upgrade for each state you wish to upgrade the contract of

# Pre-requisites:
  
See https://docs.corda.net/getting-set-up.html.

# Usage

## Running the nodes:

See https://docs.corda.net/tutorial-cordapp.html#running-the-example-cordapp.

## Upgrading the contract and state:

Run the following command from the project's root folder:

* Unix/Mac OSX: `./gradlew runUpgradeContractStateClient`
* Windows: `gradlew runUpgradeContractStateClient`

This will run the contract and state upgrade client defined [HERE](https://github.com/amolpednekar/contract-state-upgrades/blob/master/cordapp/src/main/kotlin/com/upgrade/Client.kt). This
client will:

1. Connect to PartyA and PartyB's nodes via RPC
2. Issue a state with the old contract
3. Upgrade the state to use the new contract and state
4. Wait ten seconds for the upgrade to propagate
5. Log the state to show that its contract and state has been upgraded

You should see a message of the form:

    ```I 09:47:54 1 UpgradeContractStateClient.main - TransactionState(data=NewState(a=O=PartyA, L=London, C=GB, b=O=PartyB, L=New York, C=US, value=1), contract=com.upgrade.NewCon
       tractAndState, notary=O=Notary, L=London, C=GB, encumbrance=null, constraint=net.corda.core.contracts.WhitelistedByZoneAttachmentConstraint@649b5891)```