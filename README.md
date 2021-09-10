# TCC

Implementação de meta-heurísticas para a resolução do fluxo de potência ótimo reativo.

Os algoritmos foram escritos na linguagem de programação Java e foi utilizada a ferramenta Maven para buildar e gerenciar as dependências.

A implementação do FPO utilizado nesse programa pertence a uma biblioteca de Rafael Martins.

# Requisitos

- Java 8+
- Maven 3+
- libeps (Biblioteca de Rafael Martins)

# Algoritmos

- Particle Swarm Optimization
- Genetic Algorithm
- Soccer Game Optimization

# Utilização

Para executar o algoritmo, baixe o projeto e execute o seguinte comando para fazer o build do pojeto:


```bash
mvn package
```
Depois de ter feito o build, vá até a pasta **target/TCC/** e execute o seguinte comando para executar o projeto:
```bash
java -jar TCC.jar
```
	
Alguns comando adicionais podem ser adicionados na execução.	

Para plotar os gráficos:
```bash
java -jar TCC.jar -PlotGraph=true
```
	
Para salvar os resultados. Os resultados serão salvos na pasta data/:
```bash
java -jar TCC.jar -SaveLog=true
```

Para definir um seed:
```bash
java -jar TCC.jar -Seed=<number>
```

Para definir um algoritmo:
```bash
java -jar TCC.jar -Algorithm=<algorithm>
```

Para definir um arquivo json para realizar um batch de testes:
```bash
java -jar TCC.jar -FilePath=<filePath>
```
Todos comandos podem ser utilizados juntos.
