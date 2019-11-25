package Agents;

import java.util.*;

import Components.Fire;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class AgenteCentral extends Agent {

	protected void setup() {
		System.out.println("$ Starting: Estação");
		super.setup();

		// register Estacao to DF
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Estacao");
		sd.setName(getLocalName());
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}

		// listen to incendiario
		this.addBehaviour(new Receiver());
	}

	protected void takeDown() {
		System.out.println("$ Ending: Estação");
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		super.takeDown();
	}

	private class Receiver extends CyclicBehaviour {
		private int fireCount = 0; // count the number of fires solved

		//
		private int xOrigin, yOrigin;
		private int minDistance = 1000;
		private int taxisProcessed = 0;
		private AID closestTaxi;
		private String customerName;

		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
				// mensagem proveniente de incendiario
				if (msg.getPerformative() == ACLMessage.INFORM) {
					System.out.println("$ Estação: Novo fogo detetado!");
					this.fireCount++;
					// extract fire coordinates
					String[] coordinates = msg.getContent().split(",");
					int xFire = Integer.parseInt(coordinates[0]);
					int yFire = Integer.parseInt(coordinates[1]);
					new Fire(fireCount, xFire, yFire);

					/*
					// Time to contact all taxis
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("taxi");
					template.addServices(sd);

					DFAgentDescription[] result;

					try {
						result = DFService.search(myAgent, template);
						AID[] taxis;
						taxis = new AID[result.length];
						numTaxis = result.length;

						ParallelBehaviour pb = new ParallelBehaviour(myAgent, ParallelBehaviour.WHEN_ALL) {

							public int onEnd() {
								System.out.println("All taxis inquired.");
								return super.onEnd();
							}
						};
						myAgent.addBehaviour(pb);

						for (int i = 0; i < result.length; ++i) {
							taxis[i] = result[i].getName();
							System.out.println(taxis[i].getName());
							pb.addSubBehaviour(new taxiSend(taxis[i]));
						}

					} catch (FIPAException e) {
						e.printStackTrace();
					}
					*/
				}

				/*
				else if (msg.getPerformative() == ACLMessage.INFORM) {
					String[] coordinates = msg.getContent().split(",");
					taxisProcessed++;
					int xTaxi = Integer.parseInt(coordinates[0]);
					int yTaxi = Integer.parseInt(coordinates[1]);
					int distance = (int) Math
							.sqrt(((Math.pow((xTaxi - xOrigin), 2)) + (Math.pow((yTaxi - yOrigin), 2))));
					System.out.println("D" + msg.getSender().getLocalName() + ":" + distance);
					if (distance < minDistance) {
						minDistance = distance;
						closestTaxi = msg.getSender();
					}
					if (taxisProcessed == numTaxis) {
						System.out.println("Taxi Chosen:" + closestTaxi.getName());
						ACLMessage mensagem = new ACLMessage(ACLMessage.CONFIRM);
						mensagem.addReceiver(closestTaxi);
						mensagem.setContent(customerName + "," + xDestination + "," + yDestination);
						myAgent.send(mensagem);
						taxisProcessed = 0;
						minDistance = 1000;
						closestTaxi = null;
					}
					*/

			}
			else {
				block();
			}
		}

	}

}
