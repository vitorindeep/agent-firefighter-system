import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import java.util.Random;

/*
- from AgenteIncendiario to AgenteCentral - informar coordenadas novo fogo -> INFORM
- from AgenteBombeiro to AgenteInterface - informar coordenadas atuais -> INFORM
- from AgenteBombeiro to AgenteCentral - informar coordenadas e estado de bombeiro -> PROPOSE
- from AgenteCentral to AgenteBombeiro - informar incêndio a combater e coords -> CFP
- from AgenteBombeiro to AgenteCentral - confirmar aceitação de pedido -> CONFIRM
- from AgenteBombeiro to AgenteCentral - confirmar dispêndio de água em incêndio -> INFORM_IF
- from AgenteCentral to AgenteBombeiro - informar fim de incendio -> CONFIRM
 */

public class FirefighterSystem {

    Runtime rt;
    ContainerController container;


    public static void main(String[] args) {
        FirefighterSystem a = new FirefighterSystem();
        a.initMainContainerInPlatform("localhost", "9888", "FirefighterSystem");
        Object[] dummyAargs = new Object[0];

        Random rand = new Random();

        // criar estacao
        a.startAgentInPlatform("AgenteCentral", "Agents.AgenteCentral", dummyAargs);

        // agentes bombeiros
        for (int i = 0; i < 1; i++) {
            Object[] aargs = new Object[2];
            aargs[0] = (int) i; // identificador de agente
            aargs[1] = (int) 1; // tipo de agente
            a.startAgentInPlatform("Bombeiro" + i, "Agents.AgenteBombeiro", aargs);
        }

        // criar incendiario
        a.startAgentInPlatform("AgenteIncendiario", "Agents.AgenteIncendiario", dummyAargs);
        // criar agente interface
        Object[] iargs = new Object[2];
        for (int i = 0; i < 5; i++){
            int coordX = rand.nextInt(39);
            int coordY = rand.nextInt(29);
        }
        //iargs[0]=
        a.startAgentInPlatform("AI", "Agents.AgenteInterface",new Object[0]);

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
