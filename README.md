# agent-firefighter-system

## Requisitos

- Mapa 500x500

Métricas:
- Capacidade max: águaMax/combustivel;
- Capacidade presente: água/combustível;
- Distância Agente-Bombeiro;
- Velocidade;
- Incendio tempo-atividade;
- Incendio Gravidade;
- Spread (adicional);
- Vento (adicional)

### Agentes Participativos
- Comunicar localização e recursos;
- Receber pedidos da central;
- Informa que está em (x,y) a apagar ativamente;
- Controla os seus recursos e mantém-se perto o suficiente para não ficar sem combustível e com reabastecimento rápido

#### Drones (10x)
- 2 água, 5 combustível;
- Velocidade: 4

#### Aeronave (2x)
- 15 água, 20 combustível;
- Velocidade: 2

#### Camiões (5x)
- 10 água, 10 combustível;
- Velocidade: 1

### Agente Central
- Quartel de Bombeiros;
- Informa localização de incêndio (x,y) aos participativos que quer colocar lá;
- Se a central não informar nada a um partipativo, pode decidir mandá-lo abastecer;
- Central controla estado dos fogos e pode aumentar a intensidade ou o spread do mesmo

### Agente Incendiário
- Gera fogos;
- Sempre que despoleta um fogo, informa o quartel

### Agente Interface
- Imprimir mapa, acontecimentos e unidades;
- Olha para as páginas amarelas

## Tarefas Relatório
O que há de agentes atualmente na área?
O que há de simulação (bibliografia tem)?

Introdução, casos de estudo (vitor).
Diagramas representam a arquitetura e protocolos:
- classe (sergio);
- estado (marcos);
- sequencia (sergio, vitor);
- atividade (marcos, vitor)