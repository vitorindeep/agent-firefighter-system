package Agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.Random;

public class AgenteIncendiario extends Agent {

    protected void setup() {
        System.out.println("$ Starting: Incendiário");
        super.setup();

        // new fire every 5 seconds
        this.addBehaviour(new Incendiar(this, 1000));
    }

    protected void takeDown() {
        System.out.println("$ Ending: Incendiário");
        super.takeDown();
    }

    private class Incendiar extends TickerBehaviour {
        private DFAgentDescription dfd;
        private ServiceDescription sd;
        Random rand;

        public Incendiar(Agent a, long period) {
            super(a, period);
            // formatar para procurar interface
            dfd = new DFAgentDescription();
            sd = new ServiceDescription();
            sd.setType("Estacao");
            dfd.addServices(sd);
            // inicializar random para escolher novas posições de incêndio
            rand = new Random();
        }

        protected void onTick() {
            // procurar interfaces registadas como Estacao
            try {
                DFAgentDescription[] estacoes = DFService.search(this.myAgent, dfd);
                // escolher local de novo incêndio
                int coordX = rand.nextInt(39);
                int coordY = rand.nextInt(29);
                // enviar localização de novo incêndio para todas as Estações
                for(DFAgentDescription dfEstacao : estacoes) {
                    AID estacaoID = dfEstacao.getName();
                    ACLMessage m = new ACLMessage(ACLMessage.INFORM);
                    m.addReceiver(estacaoID);
                    m.setContent(coordX + ";" + coordY);
                    send(m);
                }
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        }

    }

}
