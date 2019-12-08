import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import java.util.ArrayList;
import java.util.Random;

/*
- from AgenteIncendiario to AgenteCentral - informar coordenadas novo fogo -> INFORM
- from AgenteBombeiro to AgenteInterface - informar coordenadas atuais -> INFORM
- from AgenteBombeiro to AgenteCentral - informar coordenadas e estado de bombeiro -> PROPOSE
- from AgenteCentral to AgenteBombeiro - informar incêndio a combater e coords -> CFP
- from AgenteBombeiro to AgenteCentral - confirmar aceitação de pedido -> CONFIRM
- from AgenteBombeiro to AgenteCentral - confirmar dispêndio de água em incêndio -> INFORM_IF
- from AgenteCentral to AgenteBombeiro - informar fim de incendio -> CONFIRM
- from AgenteCentral to AgenteInterface - informar incêndio a combater e coords -> CFP

 */

public class FirefighterSystem {

    Runtime rt;
    ContainerController container;


    public static void main(String[] args) {
        FirefighterSystem a = new FirefighterSystem();
        a.initMainContainerInPlatform("localhost", "9888", "FirefighterSystem");

        // initializations
        Object[] dummyAargs = new Object[0];
        Random rand = new Random();

        // criar arraylist com posições de postos de abastecimento
        ArrayList<String> gasStations = new ArrayList<String>();
        gasStations.add(rand.nextInt(19) + ";" + rand.nextInt(14)); // ESQ BAIXO
        gasStations.add(rand.nextInt(19) + ";" + (rand.nextInt(14) + 15)); // ESQ CIMA
        gasStations.add((rand.nextInt(19) + 20) + ";" + rand.nextInt(14)); // DIREITA BAIXO
        gasStations.add((rand.nextInt(19) + 20) + ";" + (rand.nextInt(14) + 15)); // DIREITA CIMA

        // criar arraylist com posições de postos de abastecimento
        ArrayList<String> waterZones = new ArrayList<String>();
        for (int i = 0; i < 6; i++) {
            waterZones.add(rand.nextInt(39) + ";" + rand.nextInt(29));
        }


        // criar estacao
        a.startAgentInPlatform("AgenteCentral", "Agents.AgenteCentral", dummyAargs);

        int totalAgentes = 0; // to make the id unique in every cycle
        int newMaxAgents = totalAgentes + 2; // mais 2 que os existentes
        // agentes bombeiros AERONAVE
        for (; totalAgentes < newMaxAgents; totalAgentes++) {
            Object[] aargs = new Object[4];
            aargs[0] = (int) totalAgentes; // identificador de agente
            aargs[1] = (int) 1; // tipo de agente
            aargs[2] = gasStations;
            aargs[3] = waterZones;
            a.startAgentInPlatform("Bombeiro" + totalAgentes, "Agents.AgenteBombeiro", aargs);
        }

        // agentes bombeiros DRONE
        newMaxAgents = totalAgentes + 10; // mais 10 que os existentes
        for (; totalAgentes < newMaxAgents; totalAgentes++) {
            Object[] aargs = new Object[4];
            aargs[0] = (int) totalAgentes; // identificador de agente
            aargs[1] = (int) 2; // tipo de agente
            aargs[2] = gasStations;
            aargs[3] = waterZones;
            a.startAgentInPlatform("Bombeiro" + totalAgentes, "Agents.AgenteBombeiro", aargs);
        }

        // agentes bombeiros CAMIAO
        newMaxAgents = totalAgentes + 5; // mais 5 que os existentes
        for (; totalAgentes < newMaxAgents; totalAgentes++) {
            Object[] aargs = new Object[4];
            aargs[0] = (int) totalAgentes; // identificador de agente
            aargs[1] = (int) 3; // tipo de agente
            aargs[2] = gasStations;
            aargs[3] = waterZones;
            a.startAgentInPlatform("Bombeiro" + totalAgentes, "Agents.AgenteBombeiro", aargs);
        }


        // criar incendiario
        a.startAgentInPlatform("AgenteIncendiario", "Agents.AgenteIncendiario", dummyAargs);

        // criar agente interface
        Object[] aargs = new Object[2];
        aargs[0] = gasStations;
        aargs[1] = waterZones;
        a.startAgentInPlatform("AI", "Agents.AgenteInterface", aargs);

    }

    public void startAgentInPlatform(String name, String classpath, Object[] aargs) {
        try {
            AgentController ac = container.createNewAgent(name, classpath, aargs);
            ac.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initMainContainerInPlatform(String host, String port, String containerName) {

        // Get the JADE runtime interface (singleton)
        this.rt = Runtime.instance();

        // Create a Profile, where the launch arguments are stored
        Profile prof = new ProfileImpl();
        prof.setParameter(Profile.CONTAINER_NAME, containerName);
        prof.setParameter(Profile.MAIN_HOST, host);
        prof.setParameter(Profile.MAIN_PORT, port);
        prof.setParameter(Profile.MAIN, "true");
        prof.setParameter(Profile.GUI, "true");

        // create a main agent container
        this.container = rt.createMainContainer(prof);
        rt.setCloseVM(true);
    }

}
