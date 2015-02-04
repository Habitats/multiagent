package agents;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

/**
 * Created by anon on 04.02.2015.
 */
public class TaskAdministrator extends Agent {


  @Override
  protected void setup() {
    // Printout a welcome message
    System.out.println("Hello! Buyer-agent " + getAID().getName() + " is ready.");
    Object[] args = getArguments();
    if (args == null || args.length == 0) {
      String problem = "+ * 5 2 - 7 2";
      System.out.println("Trying to solve:" + problem);
      addBehaviour(new TickerBehaviour(this, 10000) {
        protected void onTick() {
          DFAgentDescription template = new DFAgentDescription();
          ServiceDescription sd = new ServiceDescription();
          sd.setType("math-solver");
          template.addServices(sd);

          try {
            List<DFAgentDescription> result = Arrays.asList(DFService.search(myAgent, template));
            List<AID> sellerAgents = result.stream().map(res -> res.getName()).collect(Collectors.toList());

            addBehaviour(new OneShotBehaviour() {
              @Override
              public void action() {
                ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                cfp.setContent(problem);
                sellerAgents.forEach(agent -> cfp.addReceiver(agent));

                cfp.setContent(problem);
                cfp.setConversationId("math-problem");

                TaskAdministrator.this.send(cfp);
              }
            });
          } catch (FIPAException fe) {
            fe.printStackTrace();
          }


        }
      });


    }
  }

  @Override
  protected void takeDown() {
    super.takeDown();
  }
}
