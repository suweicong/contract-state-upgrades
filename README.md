<p align="center">
  <img src="https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png" alt="Corda" width="500">
</p>

# Contract and State Upgrades CorDapp

This CorDapp shows the end-to-end process of upgrading contracts & state simultaneously in Corda, in explicit mode, with a local mock-network. Specially, when the contract is upgraded, the state remains the same. 

The upgrade takes place in four stages:

1. Create a replacement contract and state implementing the `UpgradedContract` interface
2. Bundle the replacement contract and state into a CorDapp and install it on each node
3. For each state you wish to upgrade the contract of, authorise the contract and state upgrade for that state on each node
4. On a single node, authorise the upgrade for each state you wish to upgrade the contract and state of

# Pre-requisites:
  
See https://docs.corda.net/getting-set-up.html.

# Usage

## How to deployNodes and runnodes:

See https://docs.corda.net/tutorial-cordapp.html#running-the-example-cordapp.

## Upgrading the contract:

the procedure can be referred to https://docs.corda.net/upgrading-cordapps.html#performing-explicit-contract-and-state-upgrades

Below are the steps for Mac, #todo: add Windows command

1.1 comment the part highlighted https://github.com/suweicong/contract-state-upgrades/blob/v1/cordapp/src/main/kotlin/com/upgrade/Client.kt#L38-L80
  
1.2 delete the folder "cordapp-new-contract-state" under the project folder (this is optional because step 1.3 should take care of this; you might want to backup this because you will need to add this back in later step)

1.3 comment everything related to "cordapp-new-contract-state", i.e. 
	
	https://github.com/suweicong/contract-state-upgrades/blob/v1/build.gradle#L81

	https://github.com/suweicong/contract-state-upgrades/blob/v1/build.gradle#L111

	https://github.com/suweicong/contract-state-upgrades/blob/v1/build.gradle#L124

	https://github.com/suweicong/contract-state-upgrades/blob/v1/build.gradle#L138

	https://github.com/suweicong/contract-state-upgrades/blob/v1/settings.gradle#L3

	https://github.com/suweicong/contract-state-upgrades/blob/v1/cordapp/build.gradle#L48
 
2. go to the contract-state-upgrades project folder, and run `./gradlew clean deployNodes`, followed by `./build/nodes/runnodes` to spin up nodes
(note: if you face permission issue, please run `chmod a+x ./gradlew`)

3. to issue some transactions, at the project folder, run `./gradlew runUpgradeContractStateClient` (can be run multiple times)

4. copy the entire original project folder, maybe rename as contract-state-upgrade-2 in this example, only comment the part highlighted https://github.com/suweicong/contract-state-upgrades/blob/v1/cordapp/src/main/kotlin/com/upgrade/Client.kt#L32-L36

5. go to the contract-state-upgrades-2 new project folder, and run `./gradlew clean deployNodes`
(note: if you face permission issue, please run `chmod a+x ./gradlew`) 

6. in PartyA's and PartyB's shell, run `run setFlowsDrainingModeEnabled enabled: true` to drain the flows for both PartyA and PartyB. Then run `bye` to shutdown all nodes including notary 

7.1 go to partyA's cordapp folder in contract-state-upgrades-2 new project folder, i.e. /contract-state-upgrades-2/build/nodes/PartyA/cordapps, and copy all jar files to all nodes' cordapp folder under contract-state-upgrades project folder
7.2 copy the network-parameters file, where you can find under a node's folder, e.g. /contract-state-upgrades-2/build/nodes/PartyA, and replace all the network-parameters file in all nodes'folder including notary under contract-state-upgrades project folder
7.3 copy the content in whitelist.txt located in /contract-state-upgrades-2/build/nodes, and add into the whitelist.txt in /contract-state-upgrades/build/nodes, so you might have something like:
com.upgrade.OldContract:1EDB4A7F8A5DBD2FDBF5E675062B84E20B990C3BFE8525DB3C7936D9B06D788D, 08110F6B26BAC2C9012C446979B372A213048F94DDA2F0CE6FA6C6427DC7D001
com.upgrade.NewContractAndState:753A233F436F1818F4A4B510354F573963B79BE29E8D459B979641AAD7AEEB96

8. using bootrapper to whitelist the new cordapp in network parameter, https://docs.corda.net/network-bootstrapper.html
 place the boostrapper into the folder /contract-state-upgrades/build/nodes, and in terminal, go to the folder /contract-state-upgrades/build/nodes where the bootstarpper is located, and run `java -jar corda-network-bootstrapper-3.2-corda-executable\ \(2\).jar .`
  note: the bootstrapper version seems to be important, if a proper version bootstrapper is not used, the bootstrapping might not be working. let me know if you want to get the one i am using for this sample

9. if boostrapping is completed, uncomment the code in step 1.3, and add back the folder deleted in step 1.2 if it is deleted. save the changes

10. call runnodes again and spin the nodes and notary again

11. in PartyA's and PartyB's shell, run `run setFlowsDrainingModeEnabled enabled: false` to undo drain the flows for both PartyA and PartyB.

12. in terminal, go to the contract-state-upgrades folder, and run `./gradlew runUpgradeContractStateClient`

13. check the database, and you should see the states are updated in vault_states table.

----------------------------------------------------------------------------------------------------

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
