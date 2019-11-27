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
    private int xDestination, yDestination;
    private int direcaoX = 0, direcaoY = 0;
    private int velocidadeMax, fuel, fuelMax, water, waterMax;
    private boolean movingToFire, fightingFire, replenishment;
    // listar em relação à lista de informação de aviões
    private DFAgentDescription dfdEstacoes, dfdBombeiro, dfdInterface;
    private ServiceDescription sdEstacoes, sdBombeiro, sdInterface;

    protected void setup() {
        System.out.println("$ Starting: Bombeiro");
        super.setup();
        // posição inicial
        Random rand = new Random();
        x = rand.nextInt(100);
        y = rand.nextInt(100);
        // estado inicial
        movingToFire = false;
        fightingFire = false;
        replenishment = false;

        // ler argumentos para criar novo AgenteBombeiro
        Object[] args = getArguments();
        int identificador = (Integer) args[0]; // id único
        int type = (Integer) args[1]; // tipo de bombeiro (1-aeronave, 2-drone, 3-camioes)
        id = "type" + Integer.toString(type) + "id" + Integer.toString(identificador);

        // adapt velocidadeMax according to vehicle type
        if(true) { // THIS HAS TO CHANGE
            velocidadeMax = 5;
            fuel = fuelMax = 15;
            water = waterMax = 20;
        }

        // definições de DF
        // Para comunicação com estações
        dfdEstacoes = new DFAgentDescription();
        sdEstacoes = new ServiceDescription();
        sdEstacoes.setType("Estacao");
        dfdEstacoes.addServices(sdEstacoes);
        // Para comunicação com interfaces
        dfdInterface = new DFAgentDescription();
        sdInterface = new ServiceDescription();
        sdInterface.setType("Interface");
        dfdInterface.addServices(sdInterface);
        // Para comunicação de Estações para mim (bombeiro)
        dfdBombeiro = new DFAgentDescription();
        sdBombeiro = new ServiceDescription();
        sdBombeiro.setType("Bombeiro");
        sdBombeiro.setName(id);
        dfdBombeiro.addServices(sdBombeiro);

        try {
            DFService.register(this, dfdBombeiro);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        // assumir behaviours
        this.addBehaviour(new EnviarCoordsIniciais());
        this.addBehaviour(new EnviarCoords(this, 1000));
        this.addBehaviour(new ReceberPedidoCombate());
        this.addBehaviour(new Movimento(this, 1000));
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
                DFAgentDescription[] dfCentrais = DFService.search(this.myAgent, dfdEstacoes);
                if (dfCentrais.length > 0) {
                    for (int i = 0; i < dfCentrais.length; ++i) {
                        DFAgentDescription dfd = dfCentrais[i];
                        AID central = dfd.getName();
                        if(!central.equals(this.myAgent.getAID())){
                            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                            msg.addReceiver(central);
                            msg.setContent(x+";"+y+";"+movingToFire+";"+fightingFire+";"+replenishment+";"+id+
                                    ";"+water+";"+fuel);
                            send(msg);
                        }
                    }
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
    }

    // enviar coordenadas e estado de 1 em 1 seg para o AgenteCentral, quando em movimento
    private class EnviarCoords extends TickerBehaviour {

        public EnviarCoords(Agent a, long period) { super(a, period); }

        protected void onTick() {
            // verificar se está em direção a incêndio (active)
            // ou em reabastecimento (replenishment)
            // (caso contrário, não muda coordenadas)
            if(movingToFire || replenishment){
                try {
                    // enviar coordenadas a todas as Estações (só haverá uma no caso base)
                    DFAgentDescription[] dfCentrais = DFService.search(this.myAgent, dfdEstacoes);
                    if (dfCentrais.length > 0) {
                        for (int i = 0; i < dfCentrais.length; ++i) {
                            DFAgentDescription dfd = dfCentrais[i];
                            AID central = dfd.getName();
                            if(!central.equals(this.myAgent.getAID())){
                                ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                                msg.addReceiver(central);
                                msg.setContent(x+";"+y+";"+movingToFire+";"+fightingFire+";"+replenishment+";"+id+
                                        ";"+water+";"+fuel);
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

    // receber pedidos para lutar por incêncio
    private class ReceberPedidoCombate extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                if (msg.getPerformative() == ACLMessage.CFP) {
                    // signal for active fire fighting (moving atm)
                    movingToFire = true;
                    // extract fire coordinates
                    String[] coordinates = msg.getContent().split(";");
                    xDestination = Integer.parseInt(coordinates[0]);
                    yDestination = Integer.parseInt(coordinates[1]);
                    System.out.println("$ Bombeiro " + id + ": Pedido para combate. Fogo em (" + xDestination + ", " + yDestination + ")");
                    // definir direção no eixo para luta a incêndio
                    adaptDirectionOfMovement(xDestination, yDestination);
                    // reply
                    ACLMessage response = msg.createReply();
                    response.setPerformative(ACLMessage.CONFIRM);
                    send(response);
                }
            } else {
                block();
            }

        }

        private void adaptDirectionOfMovement(int xFire, int yFire) {
            // x
            if (xFire > x) {
                direcaoX = 1;
            }
            else if (xFire < x) {
                direcaoX = -1;
            }
            else {
                direcaoX = 0;
            }
            // y
            if (yFire > y) {
                direcaoY = 1;
            }
            else if (yFire < y) {
                direcaoY = -1;
            }
            else {
                direcaoY = 0;
            }
        }
    }

    // simular movimento do bombeiro de acordo com a sua velocidade
    private class Movimento extends TickerBehaviour {

        public Movimento(Agent a, long period) { super(a, period); }
        protected void onTick() {
            // na luta contra incendio ou em busca de recursos há moviemento
            if(movingToFire || replenishment) {
                int newX = x+velocidadeMax*direcaoX;
                int newY = y+velocidadeMax*direcaoY;
                checkIfDestinationAchieved(newX, newY);

                // informação para interface
                try {
                    // procurar interfaces registadas
                    DFAgentDescription[] r = DFService.search(this.myAgent, dfdInterface);
                    if (r.length > 0) {
                        // enviar mensagem com nome, coordX e coordY para todas as interfaces registadas
                        for (int i = 0; i < r.length; ++i) {
                            DFAgentDescription interf = r[i];
                            AID interfName = interf.getName();
                            ACLMessage m = new ACLMessage(ACLMessage.INFORM);
                            m.addReceiver(interfName);
                            m.setContent(id+";"+x+";"+y);
                            send(m);
                        }
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
                System.out.println("Para Interface: id: " + id + ", " +
                        "x: " + x + ", " +
                        "y: " + y + ", " +
                        "xDest: " + xDestination + ", " +
                        "yDest: " + yDestination + ", " +
                        "direcaoX: " + direcaoX + ", " +
                        "direcaoY: " + direcaoY + ", " +
                        "moving: " + movingToFire + ", " +
                        "fighting: " + fightingFire + ", " +
                        "replenishment: " + replenishment);
            }
        }

        // this method changes direcaoX and direcaoY if position X or Y was reached
        // when both are reached, sets velocidadeAtual to 0, making the bombeiro stop moving
        private void checkIfDestinationAchieved(int newX, int newY) {

            // deslocação no sentido positivo em X
            if (direcaoX > 0) {
                // ultrapassamos o ponto do fogo, por isso assumimos que já lá chegamos
                if(newX > xDestination) {
                    x = xDestination;
                    direcaoX = 0; // parar de mover neste sentido
                }
                else x = newX;
            }
            // deslocação no sentido negativo em X
            if (direcaoX < 0) {
                // ultrapassamos o ponto do fogo, por isso assumimos que já lá chegamos
                if(newX < xDestination) {
                    x = xDestination;
                    direcaoX = 0; // parar de mover neste sentido
                }
                else x = newX;
            }

            // deslocação no sentido positivo em Y
            if (direcaoY > 0) {
                // ultrapassamos o ponto do fogo, por isso assumimos que já lá chegamos
                if(newY > yDestination) {
                    y = yDestination;
                    direcaoY = 0; // parar de mover neste sentido
                }
                else y = newY;
            }
            // deslocação no sentido negativo em Y
            if (direcaoY < 0) {
                // ultrapassamos o ponto do fogo, por isso assumimos que já lá chegamos
                if(newY < yDestination) {
                    y = yDestination;
                    direcaoY = 0; // parar de mover neste sentido
                }
                else y = newY;
            }

            // destino atingido, quer seja incendio ou replenishment
            if ((direcaoX == 0) && (direcaoY == 0)) {
                // chegou ao fogo
                if (movingToFire) {
                    movingToFire = false;
                    fightingFire = true;
                }
                // chegou ao abastecimento
                else {
                    replenishment = false;
                    fuel = fuelMax;
                    water = waterMax;
                }
            }
        }
    }
}
