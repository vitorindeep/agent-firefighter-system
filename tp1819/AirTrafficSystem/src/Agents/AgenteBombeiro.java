package Agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.Random;

public class AgenteBombeiro extends Agent {
    private String id;
    private int x, y;
    private boolean active, replenishment;
    // listar em relação à lista de informação de aviões
    private DFAgentDescription dfd;
    private ServiceDescription sd;

    protected void setup() {
        System.out.println("$ Starting: Bombeiro");
        super.setup();
        // posição inicial
        Random rand = new Random();
        x = rand.nextInt(100);
        y = rand.nextInt(100);
        // estado inicial
        active = false;
        replenishment = false;
        // definições de DF
        dfd = new DFAgentDescription();
        sd = new ServiceDescription();
        sd.setType("Estacao");
        dfd.addServices(sd);

        // ler argumentos para criar novo AgenteBombeiro
        Object[] args = getArguments();
        int identificador = (Integer) args[0]; // id único
        int type = (Integer) args[1]; // tipo de bombeiro (1-aeronave, 2-drone, 3-camioes)
        id = "type" + Integer.toString(type) + "id" + Integer.toString(identificador);

        // assumir behaviours
        this.addBehaviour(new EnviarCoordsIniciais());
        this.addBehaviour(new EnviarCoords(this, 1000));
    }

    protected void takeDown() {
        System.out.println("$ Ending: " + id);
        super.takeDown();

    }

    private class EnviarCoordsIniciais extends OneShotBehaviour {

        @Override
        public void action() {
            // enviar coordenadas iniciais a todas as Estações (só haverá uma no caso base)
            try {
                DFAgentDescription[] dfCentrais = DFService.search(this.myAgent, dfd);
                if (dfCentrais.length > 0) {
                    for (int i = 0; i < dfCentrais.length; ++i) {
                        DFAgentDescription dfd = dfCentrais[i];
                        AID central = dfd.getName();
                        if(!central.equals(this.myAgent.getAID())){
                            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                            msg.addReceiver(central);
                            msg.setContent(x+";"+y+";"+active+";"+replenishment+";"+id);
                            send(msg);
                        }
                    }
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
    }

    // enviar coordenadas de 1 em 1 seg para o AgenteCentrak
    private class EnviarCoords extends TickerBehaviour {

        public EnviarCoords(Agent a, long period) { super(a, period); }

        protected void onTick() {
            // verificar se está em combate a incêndio (active)
            // ou em reabastecimento (replenishment)
            // (caso contrário, não muda coordenadas)
            if(active || replenishment){
                try {
                    // enviar coordenadas a todas as Estações (só haverá uma no caso base)
                    DFAgentDescription[] dfCentrais = DFService.search(this.myAgent, dfd);
                    if (dfCentrais.length > 0) {
                        for (int i = 0; i < dfCentrais.length; ++i) {
                            DFAgentDescription dfd = dfCentrais[i];
                            AID central = dfd.getName();
                            if(!central.equals(this.myAgent.getAID())){
                                ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                                msg.addReceiver(central);
                                msg.setContent(x+";"+y+";"+active+";"+replenishment+";"+id);
                                send(msg);
                            }
                        }
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        }
    }


    // MODIFICAR - PARA RECEBER MENSAGENS DO AGENTE CENTRAL E LUTAR PELO INCÊNDIO
    private class Receiver extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                if (msg.getPerformative() == ACLMessage.CFP) {
                    System.out.println("Call For Proposal Requested!");
                    ACLMessage response = msg.createReply();
                    response.setPerformative(ACLMessage.INFORM);
                    response.setContent("" + x + "," + y);
                    send(response);
                } else if (msg.getPerformative() == ACLMessage.CONFIRM) {
                    System.out.println("We got a customer!");
                    String[] info = msg.getContent().split(",");

                    AID receiver = new AID();
                    receiver.setLocalName(info[0]);

                    ACLMessage mensagem = new ACLMessage(ACLMessage.CONFIRM);
                    mensagem.addReceiver(receiver);
                    myAgent.send(mensagem);
                    x = Integer.parseInt(info[1]);
                    y = Integer.parseInt(info[2]);

                }

            } else {
                block();
            }

        }

    }
}
