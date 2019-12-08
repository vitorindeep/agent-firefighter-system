package Agents;

import Components.Bombeiro;
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

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class AgenteBombeiro extends Agent {
    private static final int SPEED_FACTOR = 1, FUEL_FACTOR = 5;
    private String id;
    private int idFire, type;
    private int x, y;
    private int xDestination, yDestination;
    private int direcaoX = 0, direcaoY = 0;
    private int velocidadeMax, fuel, fuelMax, water, waterMax;
    private boolean movingToFire, fightingFire, replenishment, fuelReplenishmentActive, waterReplenishmentActive;
    // zonas de abastecimento e água
    private ArrayList<String> gasStations;
    private ArrayList<String> waterZones;
    // listar em relação à lista de informação de aviões
    private DFAgentDescription dfdEstacoes, dfdBombeiro, dfdInterface;
    private ServiceDescription sdEstacoes, sdBombeiro, sdInterface;

    protected void setup() {
        System.out.println("$ Starting: Bombeiro");
        super.setup();
        // posição inicial
        Random rand = new Random();
        x = rand.nextInt(39);
        y = rand.nextInt(29);
        // estado inicial
        movingToFire = false;
        fightingFire = false;
        replenishment = fuelReplenishmentActive = waterReplenishmentActive = false;

        // ler argumentos para criar novo AgenteBombeiro
        Object[] args = getArguments();
        int identificador = (Integer) args[0]; // id único
        type = (Integer) args[1]; // tipo de bombeiro (1-aeronave, 2-drone, 3-camioes)
        gasStations = (ArrayList<String>) args[2];
        waterZones = (ArrayList<String>) args[3];

        // adapt velocidadeMax according to vehicle type
        switch(type) {
            case 1: // aeronave
                id = "aeronave" + Integer.toString(identificador);
                velocidadeMax = 2 * SPEED_FACTOR;
                fuel = fuelMax = 20 * FUEL_FACTOR;
                water = waterMax = 15;
                break;
            case 2: // drone
                id = "drone" + Integer.toString(identificador);
                velocidadeMax = 4 * SPEED_FACTOR;
                fuel = fuelMax = 5 * FUEL_FACTOR;
                water = waterMax = 2;
                break;
            case 3: // camioes
                id = "camiao" + Integer.toString(identificador);
                velocidadeMax = 1 * SPEED_FACTOR;
                fuel = fuelMax = 10 * FUEL_FACTOR;
                water = waterMax = 10;
                break;
            default:
                System.out.println("$ Bombeiro " + id + ": Tipo de veículo errado. TAKEDOWN.");
                takeDown();
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
        this.addBehaviour(new EnviarInfoCentralIniciais());
        this.addBehaviour(new EnviarInfoCentral(this, 1000));
        this.addBehaviour(new ReceberInfoCombate());
        this.addBehaviour(new Movimento(this, 1000));
        this.addBehaviour(new ApagarIncendio(this, 1000));
    }

    protected void takeDown() {
        System.out.println("$ Ending: " + id);
        super.takeDown();
        try {
            DFService.deregister(this, dfdBombeiro);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private class EnviarInfoCentralIniciais extends OneShotBehaviour {

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
                                    ";"+water+";"+fuel+";"+velocidadeMax);
                            send(msg);
                        }
                    }
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
    }

    // enviar coordenadas e estado de 1 em 1 seg para o AgenteEstacao
    private class EnviarInfoCentral extends TickerBehaviour {

        public EnviarInfoCentral(Agent a, long period) { super(a, period); }

        protected void onTick() {
            // é sempre importante enviar o estado de modo a atualizar o agente central
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
                                    ";"+water+";"+fuel+";"+velocidadeMax);
                            send(msg);
                        }
                    }
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }

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
                        m.setContent(id+";"+x+";"+y+";"+water+";"+fuel+";"+type);
                        send(m);
                    }
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
    }

    // receber pedidos para lutar por incêndio e quando acaba
    private class ReceberInfoCombate extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                // pedido para lutar por incêndio
                if (msg.getPerformative() == ACLMessage.CFP) {
                    // signal for active fire fighting (moving atm)
                    movingToFire = true;
                    // extract fire coordinates
                    String[] coordinates = msg.getContent().split(";");
                    idFire = Integer.parseInt(coordinates[0]);
                    xDestination = Integer.parseInt(coordinates[1]);
                    yDestination = Integer.parseInt(coordinates[2]);
                    System.out.println("$ Bombeiro " + id + ": Pedido para combate. Fogo id : " + idFire + " em (" + xDestination + ", " + yDestination + ")");
                    // definir direção no eixo para luta a incêndio
                    adaptDirectionOfMovement(xDestination, yDestination);
                    // reply
                    ACLMessage response = msg.createReply();
                    response.setPerformative(ACLMessage.CONFIRM);
                    send(response);
                }
                // receber informação de incêndio apagado
                else if (msg.getPerformative() == ACLMessage.CONFIRM) {
                    fightingFire = false;
                    System.out.println("$ Bombeiro " + id + ": Combate a incêndio " + idFire + " terminado.");
                    // verificar necessidade de reabastecimento de combustível
                    if (needsFuel()) {
                        System.out.println("$ Bombeiro " + id + ": Necessita de combustível.");
                        replenishment = true;
                        setFuelStationDestination();
                    }
                    // ou se apenas necessita de água
                    else if (needsWater()) {
                        System.out.println("$ Bombeiro " + id + ": Necessita de água apenas.");
                        replenishment = true;
                        setWaterZoneDestination();
                    }
                }
            } else {
                block();
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
                // gasta combustível
                fuel--;
            }
            // deslocação no sentido negativo em X
            if (direcaoX < 0) {
                // ultrapassamos o ponto do fogo, por isso assumimos que já lá chegamos
                if(newX < xDestination) {
                    x = xDestination;
                    direcaoX = 0; // parar de mover neste sentido
                }
                else x = newX;
                // gasta combustível
                fuel--;
            }

            // deslocação no sentido positivo em Y
            if (direcaoY > 0) {
                // ultrapassamos o ponto do fogo, por isso assumimos que já lá chegamos
                if(newY > yDestination) {
                    y = yDestination;
                    direcaoY = 0; // parar de mover neste sentido
                }
                else y = newY;
                // gasta combustível
                fuel--;
            }
            // deslocação no sentido negativo em Y
            if (direcaoY < 0) {
                // ultrapassamos o ponto do fogo, por isso assumimos que já lá chegamos
                if(newY < yDestination) {
                    y = yDestination;
                    direcaoY = 0; // parar de mover neste sentido
                }
                else y = newY;
                // gasta combustível
                fuel--;
            }

            // destino atingido, quer seja incendio ou replenishment
            if ((direcaoX == 0) && (direcaoY == 0)) {
                // chegou ao fogo
                if (movingToFire) {
                    movingToFire = false;
                    fightingFire = true;
                }
                // chegou ao abastecimento
                else if (replenishment) {
                    // esta em busca de combustivel
                    if (fuelReplenishmentActive) {
                        System.out.println("$ Bombeiro " + id + ": Combustível ABASTECIDO.");
                        fuelReplenishmentActive = false;
                        fuel = fuelMax;
                        // verificar se também é preciso água
                        if (needsWater()) {
                            System.out.println("$ Bombeiro " + id + ": Necessita de água, para além de combustível.");
                            waterReplenishmentActive = true;
                            replenishment = true;
                            setWaterZoneDestination();
                        }
                        // se nao for preciso, ja nao esta mais em replenishment
                        else {
                            replenishment = false;
                        }
                    }
                    // se busca por água
                    else if (waterReplenishmentActive) {
                        System.out.println("$ Bombeiro " + id + ": Água ABASTECIDA.");
                        waterReplenishmentActive = false;
                        water = waterMax;
                        replenishment = false;
                    }
                }
            }
        }
    }

    // gastar recursos no incendio uma vez chegado ao mesmo
    // e informar estacao
    private class ApagarIncendio extends TickerBehaviour {

        public ApagarIncendio(Agent a, long period) { super(a, period); }

        protected void onTick() {
            // verificar se está a combater incêndio
            if(fightingFire){
                try {
                    // gastar água
                    water--;
                    // enviar informação com gasto de unidade de água para X incêndio
                    DFAgentDescription[] dfCentrais = DFService.search(this.myAgent, dfdEstacoes);
                    if (dfCentrais.length > 0) {
                        for (int i = 0; i < dfCentrais.length; ++i) {
                            DFAgentDescription dfd = dfCentrais[i];
                            AID central = dfd.getName();
                            if(!central.equals(this.myAgent.getAID())){
                                ACLMessage msg = new ACLMessage(ACLMessage.INFORM_IF);
                                msg.addReceiver(central);
                                msg.setContent(idFire + "");
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

    private boolean needsWater() {

        // water replanishment differs from vehicle type
        switch(type) {
            case 1: // aeronave apenas necessita quando está sem água porque abastecer é dispendioso e demora tempo
                return (water == 0);
            case 2: // drone apenas necessita quando está sem água porque tem muito pouca capacidade
                return (water == 0);
            case 3: // camioes reabastecem quando têm 20% da água para nunca andarem vazios
                return (water == 0.2 * waterMax);
            default:
                System.out.println("$ Bombeiro " + id + ": ABASECIMENTO IMPOSSÍVEL.");
                takeDown();
                return false;
        }
    }

    private boolean needsFuel() {

        // fuel replanishment differs from vehicle type
        // max distance from gasStation is 20 steps at speed 1
        switch(type) {
            // aeronave tem speed=2, portanto leva 10steps=10unidades de combustível a chegar ao reabastecimento, no max
            // o seu maxFuel = 100, pelo que precisa de garantir sempre 10% (logo colocamos 20% para margem após incêndio)
            case 1:
                return (fuel <= 0.2 * fuelMax);
            // drone tem speed=4, portanto leva 5steps=5unidades de combustível a chegar ao reabastecimento, no max
            // o seu maxFuel = 25, pelo que precisa de garantir sempre 20% (logo colocamos 40% para margem após incêndio)
            case 2:
                return (fuel <= 0.4 * fuelMax);
            // camiao tem speed=1, portanto leva 20steps=20unidades de combustível a chegar ao reabastecimento, no max
            // o seu maxFuel = 50, pelo que precisa de garantir sempre 40% (logo colocamos 60% para margem após incêndio)
            case 3:
                return (fuel <= 0.6 * fuelMax);
            default:
                System.out.println("$ Bombeiro " + id + ": ABASECIMENTO IMPOSSÍVEL.");
                takeDown();
                return false;
        }
    }

    // checks the closest gasStation and set destination x and y
    // in Movimento() we check when we arrive and change what we need accordingly
    private void setFuelStationDestination() {
        fuelReplenishmentActive = true;

        double minDistance = 99999;
        int xGas, yGas, xMin = 0, yMin = 0;

        for (String entry : gasStations) {
            String[] coords = entry.split(";");
            xGas = Integer.valueOf(coords[0]);
            yGas = Integer.valueOf(coords[1]);

            // calcular distancia entre bombeiro e posto de combustível
            double distPercorrer = Math.sqrt(((Math.pow((xGas - x), 2)) + (Math.pow((yGas - y), 2))));

            // verificar se é a mais pequena até ao momento
            if (distPercorrer < minDistance) {
                minDistance = distPercorrer;
                // definir novo destino
                xMin = xGas;
                yMin = yGas;
            }
        }
        xDestination = xMin;
        yDestination = yMin;

        // definir direção no eixo para abastecimento
        adaptDirectionOfMovement(xDestination, yDestination);
    }

    // checks the closest water zone and set destination x and y
    // in Movimento() we check when we arrive and change what we need accordingly
    private void setWaterZoneDestination() {
        waterReplenishmentActive = true;

        double minDistance = 99999;
        int xWater, yWater, xMin = 0, yMin = 0;

        for (String entry : waterZones) {
            String[] coords = entry.split(";");
            xWater = Integer.valueOf(coords[0]);
            yWater = Integer.valueOf(coords[1]);

            // calcular distancia entre bombeiro e posto de combustível
            double distPercorrer = Math.sqrt(((Math.pow((xWater - x), 2)) + (Math.pow((yWater - y), 2))));

            // verificar se é a mais pequena até ao momento
            if (distPercorrer < minDistance) {
                minDistance = distPercorrer;
                // definir novo destino
                xMin = xWater;
                yMin = yWater;
            }
        }

        xDestination = xMin;
        yDestination = yMin;

        // definir direção no eixo para abastecimento
        adaptDirectionOfMovement(xDestination, yDestination);
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
