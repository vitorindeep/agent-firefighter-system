package Agents;

import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import jade.core.behaviours.TickerBehaviour;
import org.knowm.xchart.BubbleChart;
import org.knowm.xchart.BubbleChartBuilder;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.demo.charts.ExampleChart;
import org.knowm.xchart.style.Styler.ChartTheme;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class AgenteInterface extends Agent {

    protected void setup() {
        super.setup();

        // argumentos recebidos são:
        // 1 - localizações de pontos de água
        // 2 - localizações de pontos de abastecimento
        
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
        addBehaviour(new ReceberInfoBombeiros());
        addBehaviour(new DesenhaGrafico(this, 500));
    }

    // recebe as coordenadas de cada um dos agentes
    private class ReceberInfoBombeiros extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();
            // recebe as coordenadas e informações de água e combustível
            if (msg != null && msg.getPerformative() == ACLMessage.INFORM) {
                String[] coordsAviao = msg.getContent().split(";");
                // pegar no nome do aviao e substituir os A. AA1 -> 1
                int pos = Integer.parseInt(coordsAviao[0].replace("A", ""));
                double aviaoCoordX = Double.parseDouble(coordsAviao[1]);
                double aviaoCoordY = Double.parseDouble(coordsAviao[2]);
                double agua = Double.parseDouble(coordsAviao[3]);
                double fuel = Double.parseDouble(coordsAviao[4]);
                // update structures HERE (MARCOS)
                // vê id de agente, adiciona/altera no hashmap de agentes
                // hashmap.get(idBombeiro).update(x, y, water, fuel)
            } else
                block();
        }
    }

    // faz update ao grafico de 0.5s em 0.5s
    private class DesenhaGrafico extends TickerBehaviour {
        public DesenhaGrafico(Agent a, long period) {
            super(a, period);
        }

        public void onTick() {
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