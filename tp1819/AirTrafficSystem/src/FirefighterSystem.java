import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class FirefighterSystem {

    Runtime rt;
    ContainerController container;

    public static void main(String[] args) {
        FirefighterSystem a = new FirefighterSystem();
        a.initMainContainerInPlatform("localhost", "9888", "FirefighterSystem");

        // criar estacao
        a.startAgentInPlatform("AgenteCentral", "Agents.AgenteCentral");

        // criar incendiario
        a.startAgentInPlatform("AgenteIncendiario", "Agents.AgenteIncendiario");
    }

    public void startAgentInPlatform(String name, String classpath) {
        try {
            AgentController ac = container.createNewAgent(name, classpath, new Object[0]);
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
