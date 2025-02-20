package Agents;

import java.util.*;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.text.html.HTMLDocument;

import Map.DroneAgent;
import Map.Fire;
import Map.Helpers.Clock;
import Map.TileGrid;
import jade.core.behaviours.TickerBehaviour;
import org.knowm.xchart.BubbleChart;
import org.knowm.xchart.BubbleChartBuilder;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.demo.charts.ExampleChart;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.Styler.ChartTheme;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import org.lwjgl.opengl.Display;

import static Map.Helpers.Artist.BeginSession;
import static Map.Helpers.Artist.QuickLoad;
import Map.TileType;

public class AgenteInterface extends Agent {

    protected int map [][];
    protected TileGrid grid;
    protected DroneAgent da;
    private Random rand;

    protected HashMap<String, DroneAgent> agents;
    protected HashMap<String, Fire> fire;
    private ArrayList<String> gasStations;
    private ArrayList<String> waterZones;

    protected void setup() {
        super.setup();

        agents = new HashMap<>();
        fire = new HashMap<>();
        this.map = new int[][]{
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},

        };
        // argumentos recebidos são:
        Object[] args = getArguments();
        // 1 - localizações de pontos de água
        waterZones = (ArrayList<String>) args[1];
        for (String coords : waterZones) {
            //System.out.println("Posto de água em: " + coords);
            String[] wCoord = coords.split(";");
            map[Integer.parseInt(wCoord[1])][Integer.parseInt(wCoord[0])] = 2;
        }

//        for(int i = 14; i < 24 ; i++){
//            for(int j = 9; j<19; j++) {
//                map[j][i] = 2;
//            }
//        }

        // 2 - localizações de pontos de abastecimento
        gasStations = (ArrayList<String>) args[0];
        for (String coords : gasStations) {
            //System.out.println("Posto de abastecimento em: " + coords);
            String[] gCoord = coords.split(";");
            map[Integer.parseInt(gCoord[1])][Integer.parseInt(gCoord[0])] = 3;
        }

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getLocalName());
        sd.setType("Interface");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        addBehaviour(new ReceberInfoIncendio());
        addBehaviour(new DesenhaGrafico(this, 500));
    }

    // receber pedidos para lutar por incêndio e quando acaba
    private class ReceberInfoIncendio extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                // pedido para lutar por incêndio
                if (msg.getPerformative() == ACLMessage.CFP) {
                    // extract fire coordinates
                    String[] coordinates = msg.getContent().split(";");
                    int idFire = Integer.parseInt(coordinates[0]);
                    int xDestination = Integer.parseInt(coordinates[1]);
                    int yDestination = Integer.parseInt(coordinates[2]);
                    //desenhar incendio no mapa
                    if(!fire.containsKey(Integer.toString(idFire))){
                        fire.put(Integer.toString(idFire), new Fire(QuickLoad("fire64"),grid.GetTile(xDestination,yDestination),32,32));

                    }
                }
                // receber informação de incêndio apagado
                else if (msg.getPerformative() == ACLMessage.CONFIRM) {
                    String info = msg.getContent();
                    String idFire = info;

                    if(fire.containsKey(idFire)){
                        fire.get(idFire).Update(QuickLoad("dirt64"));
                    }
//                    fire.get(idFire).Update(QuickLoad("dirt64"));
                }
                // recebe as coordenadas e informações de água e combustível
                else if (msg.getPerformative() == ACLMessage.INFORM) {
                    String[] coordsAviao = msg.getContent().split(";");
                    String idAgente = coordsAviao[0];
                    float aviaoCoordX = Float.parseFloat(coordsAviao[1]);
                    float aviaoCoordY = Float.parseFloat(coordsAviao[2]);
                    int water = Integer.parseInt(coordsAviao[3]);
                    int fuel = Integer.parseInt(coordsAviao[4]);
                    int agentType = Integer.parseInt(coordsAviao[5]);
                    // update structures
                    // vê id de agente, adiciona/altera no hashmap de agentes
                    if(!agents.containsKey(idAgente)){

                switch (agentType){
                    case 1:
                        //aeronave
                        agents.put(coordsAviao[0], new DroneAgent(QuickLoad("helicopter64"),grid.GetTile((int)aviaoCoordX,(int)aviaoCoordY),32,32,2,water,fuel));

                        break;
                    case 2:
                        //drone
                        agents.put(coordsAviao[0], new DroneAgent(QuickLoad("drone64"),grid.GetTile((int)aviaoCoordX,(int)aviaoCoordY),32,32,2,water,fuel));

                        break;
                    case 3:
                        //camião
                        agents.put(coordsAviao[0], new DroneAgent(QuickLoad("truck64"),grid.GetTile((int)aviaoCoordX,(int)aviaoCoordY),32,32,2,water,fuel));

                }
                        }
                    else
                    {
                        agents.get(idAgente).Update(aviaoCoordX,aviaoCoordY, water, fuel);
                    }

                }
            } else {
                block();
            }

        }
    }

    // faz update ao grafico de 0.5s em 0.5s
    private class DesenhaGrafico extends TickerBehaviour {


        public DesenhaGrafico(Agent a, long period) {
            super(a, period);

            BeginSession();

            grid = new TileGrid(map);
//            for (int i = 0; i < 5; i++){
//                grid.SetTile(rand.nextInt(39),rand.nextInt(29), TileType.Water);
//            }


            //grid.SetTile(3,4, grid.GetTile(2,4).getType());
            //da = new DroneAgent(QuickLoad("drone64"),grid.GetTile(10,10),32,32,2,1000,1000);
            /*while(!Display.isCloseRequested()){
                Clock.update();
                da.Update();
                grid.Draw();
                da.Draw();

                Display.update();
                Display.sync(60);
            }

            Display.destroy();*/
        }

        public void onTick() {
            if(Display.isCloseRequested()){
                Display.destroy();
            }
            else {
                //Clock.update();
                //da.Update();

                grid.Draw();

                for(Fire f : fire.values()){
                    f.Draw();
                }
                for (DroneAgent da : agents.values()) {
                    da.Draw();
                }


                //da.Draw();

                Display.update();
                Display.sync(60);
            }


            /*
            grid.Draw()
            // percorrer hashmap de agentes e Draw
            hashmap.iterate()
                da.Draw()
            */

        }
    }

    protected void takeDown() {
        super.takeDown();
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}