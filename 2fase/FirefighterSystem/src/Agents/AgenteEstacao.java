package Agents;

import java.util.*;

import Components.Bombeiro;
import Components.Fire;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jess.JessException;
import jess.Rete;
import jess.Value;

public class AgenteEstacao extends Agent {
	// listar em relação à lista de informação de aviões
	private DFAgentDescription dfdEstacoes, dfdBombeiros, dfdInterface;
	private ServiceDescription sdEstacoes, sdBombeiros, sdInterface;
	// control
	private HashMap<Integer, Fire> fires = new HashMap<Integer, Fire>(); // all the fires
	private HashMap<String, Bombeiro> bombeiros = new HashMap<String, Bombeiro>(); // all the bombeiros agents

	protected void setup() {
		System.out.println("$ Starting: Estação");
		super.setup();

		// para receber comunicações
		dfdEstacoes = new DFAgentDescription();
		dfdEstacoes.setName(getAID());
		sdEstacoes = new ServiceDescription();
		sdEstacoes.setType("Estacao");
		sdEstacoes.setName(getLocalName());
		dfdEstacoes.addServices(sdEstacoes);

		// Para comunicação com interfaces
		dfdInterface = new DFAgentDescription();
		sdInterface = new ServiceDescription();
		sdInterface.setType("Interface");
		dfdInterface.addServices(sdInterface);

		try {
			DFService.register(this, dfdEstacoes);
		} catch (FIPAException e) {
			e.printStackTrace();
		}

		// listen to incendiario
		this.addBehaviour(new Receiver());
		// check if any non atended fire exhists every 5 seconds
		this.addBehaviour(new Oracle(this, 5000));
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

	// watch if any fires are not treated
	private class Oracle extends TickerBehaviour {

		public Oracle(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			for (Map.Entry<Integer, Fire> entry : fires.entrySet()) {
				Fire entryFire = entry.getValue();
				Integer entryKey = entry.getKey();
				// if fire is not atended
				// because there was no firefighter available at the time
				if (!entryFire.isAtended()) {
					// aumentar intensidade do fogo porque demorou a ser apagado
					entryFire.increaseFireIntensity();
					// verificar o bombeiro mais próximo
					Bombeiro bombeiroDisponivel = getNearestAvailableFirefighter(entryFire.getCoordX(), entryFire.getCoordY());
					// se existe disponível, encaminhá-lo para incendio
					if (bombeiroDisponivel != null) {
						System.out.println("$ Estação: FOGO AINDA NÃO ATENDIDO. Bombeiro " + bombeiroDisponivel.getId() + " disponível.");
						// colocar fogo como atendido
						fires.get(entryKey).atended();
						// definições para procura de bombeiro
						dfdBombeiros = new DFAgentDescription();
						sdBombeiros = new ServiceDescription();
						sdBombeiros.setType("Bombeiro");
						sdBombeiros.setName(bombeiroDisponivel.getId()); // procura por o id do Bombeiro
						dfdBombeiros.addServices(sdBombeiros);
						DFAgentDescription[] dfBombeiros;
						try {
							// obter bombeiro em específico (só deve encontrar 1)
							dfBombeiros = DFService.search(this.myAgent, dfdBombeiros);
							if (dfBombeiros.length > 0) {
								for (int i = 0; i < dfBombeiros.length; ++i) {
									DFAgentDescription dfd = dfBombeiros[i];
									AID bombeiro = dfd.getName();
									ACLMessage newMsg = new ACLMessage(ACLMessage.CFP);
									newMsg.addReceiver(bombeiro);
									newMsg.setContent(entryKey + ";" + entryFire.getCoordX() + ";" + entryFire.getCoordY());
									send(newMsg);
								}

							}
						} catch (FIPAException e) {
							e.printStackTrace();
						}
					}
					else {
						System.out.println("$ Estação: Nenhum bombeiro disponível para o combate ao fogo.");
					}
				}
			}
		}

	}

	// receive and treat infos
	private class Receiver extends CyclicBehaviour {
		private int fireCount = 0; // count the number of fires solved

		// JESS
		private Rete engine;

		private int xOrigin, yOrigin;
		private int minDistance = 1000;
		private int taxisProcessed = 0;
		private AID closestTaxi;
		private String customerName;

		public Receiver () {
			engine = new Rete();
			try {
				engine.executeCommand("(deftemplate fire " +
												"(slot id) " +
												"(slot x) " +
												"(slot y) " +
												"(slot intensity) " +
												"(slot active) " +
											")");
			} catch (JessException e) {
				e.printStackTrace();
			}

		}

		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
				// mensagem proveniente de Incendiario
				if (msg.getPerformative() == ACLMessage.INFORM) {
					this.fireCount++;
					// extract fire coordinates
					String[] coordinates = msg.getContent().split(";");
					System.out.println("$ Estação: Novo fogo detetado! -> x: " + coordinates[0] + ", y: " + coordinates[1]);
					int xFire = Integer.parseInt(coordinates[0]);
					int yFire = Integer.parseInt(coordinates[1]);
					fires.put(fireCount, new Fire(fireCount, xFire, yFire));

					// comunicar ao Agente Interface
					try {
						DFAgentDescription[] interfaces = DFService.search(this.myAgent, dfdInterface);
						if (interfaces.length > 0) {
							for (int i = 0; i < interfaces.length; ++i) {
								DFAgentDescription dfd = interfaces[i];
								AID interfaceAgent = dfd.getName();
								ACLMessage newMsg = new ACLMessage(ACLMessage.CFP);
								newMsg.addReceiver(interfaceAgent);
								newMsg.setContent(fireCount + ";" + xFire + ";" + yFire);
								send(newMsg);
							}
						}
					} catch (FIPAException e) {
						e.printStackTrace();
					}

					// verificar o bombeiro mais próximo
					Bombeiro bombeiroDisponivel = getNearestAvailableFirefighter(xFire, yFire);
					// se existe disponível, encaminhá-lo para incendio
					if (bombeiroDisponivel != null) {
						System.out.println("$ Estação: Bombeiro " + bombeiroDisponivel.getId() + " disponível.");
						// colocar fogo como atendido
						fires.get(fireCount).atended();
						// definições para procura de bombeiro
						dfdBombeiros = new DFAgentDescription();
						sdBombeiros = new ServiceDescription();
						sdBombeiros.setType("Bombeiro");
						sdBombeiros.setName(bombeiroDisponivel.getId()); // procura por o id do Bombeiro
						dfdBombeiros.addServices(sdBombeiros);
						DFAgentDescription[] dfBombeiros;
						try {
							// obter bombeiro em específico (só deve encontrar 1)
							dfBombeiros = DFService.search(this.myAgent, dfdBombeiros);
							if (dfBombeiros.length > 0) {
								for (int i = 0; i < dfBombeiros.length; ++i) {
									DFAgentDescription dfd = dfBombeiros[i];
									AID bombeiro = dfd.getName();
									ACLMessage newMsg = new ACLMessage(ACLMessage.CFP);
									newMsg.addReceiver(bombeiro);
									newMsg.setContent(fireCount + ";" + xFire + ";" + yFire);
									send(newMsg);
								}

							}
						} catch (FIPAException e) {
							e.printStackTrace();
						}
					}
					else {
						System.out.println("$ Estação: Nenhum bombeiro disponível para o combate ao fogo.");
					}

                    // test jess
                    try {
                        engine.executeCommand("(assert (fire (id " + fireCount + ") " +
                                "(x " + xFire + ") " +
                                "(y " + yFire + ") " +
                                "(intensity 1) " +
                                "(active TRUE)" +
                                "))");
                        //engine.executeCommand("(facts)");
                        //engine.executeCommand("(call ?fires put " + fireCount + " ?newFire)");
                        //engine.executeCommand("(call ?fires get " + fireCount + ")");

                        //engine.executeCommand("(add newFire )");
                        //engine.executeCommand("(facts)");

						/*
						engine.executeCommand("(bind ?fogoNum " + fireCount+ ")");
						engine.executeCommand("(printout t idFogo: ?fogoNum crlf)");

						engine.executeCommand("(bind ?fogo " + newFire + ")");
						engine.executeCommand("(printout t ?fogo crlf)");

						engine.executeCommand("(call ?fires put ?fogoNum ?fogo)");
						Value teste = engine.executeCommand("(call ?fires get ?fogoNum)");
						Fire testefogo = (Fire) teste;

						engine.executeCommand("(printout t (call ?fires get ?fogoNum) crlf)");
						engine.executeCommand("(printout t ?fires crlf)");
						*/
                    } catch (JessException e) {
                        e.printStackTrace();
                    }
				}
				// mensagem proveniente de Bombeiro a informar Coordenadas e Estado quando em modo ATIVO
				// só é enviada esta mensagem depois de uma CFP por parte do Central
				else if (msg.getPerformative() == ACLMessage.PROPOSE) {
					// extract bombeiro info
					String[] infos = msg.getContent().split(";");
					int xBombeiro = Integer.parseInt(infos[0]);
					int yBombeiro = Integer.parseInt(infos[1]);
					boolean movingBombeiro = Boolean.parseBoolean(infos[2]);
					boolean fightingBombeiro = Boolean.parseBoolean(infos[3]);
					boolean replanishmentBombeiro = Boolean.parseBoolean(infos[4]);
					String idBombeiro = infos[5];
					int waterBombeiro = Integer.parseInt(infos[6]);
					int fuelBombeiro = Integer.parseInt(infos[7]);
					int speed = Integer.parseInt(infos[8]);
					/*
					System.out.println("$ Estação: Informações de Bombeiro! -> id: " + idBombeiro + ", x: " + xBombeiro + ", y: " + yBombeiro +
							", moving: " + movingBombeiro + ", fighting: " + fightingBombeiro +
							", replanishment: " + replanishmentBombeiro +
							", water: " + waterBombeiro + ", fuel: " + fuelBombeiro);
                    */
					// se existe, atualizar
					if (bombeiros.containsKey(idBombeiro)) {
						bombeiros.replace(idBombeiro, new Bombeiro(idBombeiro, xBombeiro, yBombeiro, movingBombeiro, fightingBombeiro, replanishmentBombeiro, speed));
					}
					// se não existe, adicionar ao catálogo de bombeiros
					else {
						bombeiros.put(idBombeiro, new Bombeiro(idBombeiro, xBombeiro, yBombeiro, movingBombeiro, fightingBombeiro, replanishmentBombeiro, speed));
					}
				}
				// mensagem de confirmação de início de ida para incêndio por parte de bombeiro
				else if (msg.getPerformative() == ACLMessage.CONFIRM) {
					System.out.println("$ Estação: Confirmação bombeiro");
				}
				// mensagem de informação de gasto de unidade de água para X incêndio
				else if (msg.getPerformative() == ACLMessage.INFORM_IF) {
					// extract fire being fighted info
					int idFire = Integer.parseInt(msg.getContent());
					System.out.println("$ Estação: 1 unidade de água utilizada no incêndio " + idFire);
					fires.get(idFire).decreaseFireIntensity();
					// caso extinto, enviar notícia de extinção para o bombeiro responsável pelo incêndio
					// e atualizar a nossa lista de bombeiros
					if(fires.get(idFire).isFireExtinguished()) {
						// inform interface
						DFAgentDescription[] interfaces = new DFAgentDescription[0];
						try {
							interfaces = DFService.search(this.myAgent, dfdInterface);
							if (interfaces.length > 0) {
								for (int i = 0; i < interfaces.length; ++i) {
									DFAgentDescription dfd = interfaces[i];
									AID interfaceAgent = dfd.getName();
									ACLMessage newMsg = new ACLMessage(ACLMessage.CONFIRM);
									newMsg.addReceiver(interfaceAgent);
									newMsg.setContent(String.valueOf(idFire));
									send(newMsg);
								}
							}
						} catch (FIPAException e) {
							e.printStackTrace();
						}
						// inform bombeiro
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.CONFIRM);
						send(reply);
						System.out.println("Extinto: " + fires.get(idFire).isFireExtinguished());
					}
				}
			}
			else {
				block();
			}
		}
	}

	private Bombeiro getNearestAvailableFirefighter(int xFire, int yFire) {
		double minTime = 99999;
		Bombeiro b = null;

		for (Map.Entry<String, Bombeiro> entry : bombeiros.entrySet()) {
			Bombeiro bombeiro = entry.getValue();
			// verificar se está disponível para combater fogo
			if (bombeiro.isAvailable()) {
				int xBombeiro = bombeiro.getX();
				int yBombeiro = bombeiro.getY();
				// calcular distancia entre bombeiro e fogo a combater
				double timeTo = (Math.sqrt(((Math.pow((xFire - xBombeiro), 2)) + (Math.pow((yFire - yBombeiro), 2)))) / bombeiro.getSpeed());
				// verificar se é a mais pequena até ao momento
				if (timeTo < minTime) {
					minTime = timeTo;
					b = bombeiro;
				}
			}
		}

		return b;
	}

}
