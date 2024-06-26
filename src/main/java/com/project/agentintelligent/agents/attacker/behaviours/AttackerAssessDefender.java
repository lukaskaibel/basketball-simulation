package com.project.agentintelligent.agents.attacker.behaviours;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import com.project.agentintelligent.App;
import com.project.agentintelligent.ConversationId;
import com.project.agentintelligent.agents.attacker.Attacker;
import com.project.agentintelligent.agents.defender.state.DefenderState;

import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttackerAssessDefender extends SimpleBehaviour {
    private static final Logger logger = LoggerFactory.getLogger(AttackerAssessDefender.class);
    
    private Attacker attacker;
    private boolean done = false;

    public AttackerAssessDefender(Attacker attacker) {
        super(attacker);
        this.attacker = attacker;
    }

    @Override
    public void action() {
        // Send a request to the Defender for action and position
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.addReceiver(new AID("Defender", AID.ISLOCALNAME));
        request.setContent("ATTACKER: Requesting your action and position");
        request.setConversationId(ConversationId.DEFENDER_STATE_REQUEST);
        logger.debug("ATTACKER: Sending state request to Defender");
        myAgent.send(request);

        // Wait for the response
        MessageTemplate getDefenderStateTemplate = MessageTemplate.and(
            MessageTemplate.MatchSender(new AID("Defender", AID.ISLOCALNAME)),
            MessageTemplate.MatchConversationId(ConversationId.DEFENDER_STATE_REQUEST)
        );
        logger.debug("ATTACKER: Waiting for defender state response...");
        ACLMessage defenderStateResponse = myAgent.blockingReceive(getDefenderStateTemplate);
        logger.debug("ATTACKER: Received defender state response: " + defenderStateResponse.getContent());

        if (defenderStateResponse != null) {
            try {
                // Deserialize the object from the byte array
                byte[] serializedObject = defenderStateResponse.getByteSequenceContent();
                ByteArrayInputStream bais = new ByteArrayInputStream(serializedObject);
                ObjectInputStream ois = new ObjectInputStream(bais);
                DefenderState defenderState = (DefenderState) ois.readObject();
                ois.close();

                logger.debug("ATTACKER: received defender state: " + defenderState);
                attacker.setDefenderState(defenderState);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        done = true;
    }

    @Override
    public boolean done() {
        return done;
    }
}
