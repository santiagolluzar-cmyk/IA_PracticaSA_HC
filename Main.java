import IA.Gasolina.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;

import aima.search.framework.*;
import aima.search.informed.*;
import java.util.Random;

public final class Main {
    public static long SEED = 1234L;
    public static Random random = new Random(SEED);
    public static void main(String[] args) {
        System.out.println("Pràctica 1: Cerca Local\n");

        SuccessorFunction SuccessorFunctHC = new Prac1SuccessorFunction();
        SuccessorFunction SuccessorFunctSA = new Prac1SuccessorFunctionSA();

        //VALORS QUE ANIREM CANVIANT:
        int numGasolineres = 100;
        int numCentres = 10;
        int numCamionsPerCentre = 1;

        System.out.println("ESTAT INICIAL ALEATORI:\n");
        Prac1Estat estatInicialAleatori = crearEstatInicial(numGasolineres, numCentres, numCamionsPerCentre, 1);
        executarCerca(estatInicialAleatori, SuccessorFunctHC, SuccessorFunctSA);

        System.out.println("ESTAT INICIAL BUIT:\n");
        Prac1Estat estadoInicialVacio = crearEstatInicial(numGasolineres, numCentres, numCamionsPerCentre, 0);
        executarCerca(estadoInicialVacio, SuccessorFunctHC, SuccessorFunctSA);

        System.out.println("ESTAT INICIAL PER PROXIMITAT:\n");
        Prac1Estat estadoInicialProximidad = crearEstatInicial(numGasolineres, numCentres, numCamionsPerCentre, 2);
        executarCerca(estadoInicialProximidad, SuccessorFunctHC, SuccessorFunctSA);

    }

    private static void executarCerca(Prac1Estat estadoInicial, SuccessorFunction HC, SuccessorFunction SA) {
        executarHillClimbing(estadoInicial.clone(), HC);
        executarSimulatedAnnealing(estadoInicial.clone(), SA); //Fem el clone per a què ambdós algorismes comencin desde el mateix punt
    }

    private static Prac1Estat crearEstatInicial(int nGas, int nCentres, int nCamions, int tipus) {
        Gasolineras gasolineres = new Gasolineras(nGas, (int) SEED); //aquí hem generat un array de gasolineres amb la seed indicada
        CentrosDistribucion centres = new CentrosDistribucion(nCentres, nCamions, (int) SEED); //el mateix amb centres de distribució

        Prac1Estat.centres_dist = centres;
        Prac1Estat estat = new Prac1Estat(gasolineres, centres); //Creem l'estat. Inicialitzem les matrius de distàncies i llistem les peticions

        if (tipus == 0)estat.generarEstatInicialBuit();
        else if (tipus == 1) estat.generarEstatInicialAleatori();
        else estat.generarEstatInicialProximitat();

        return estat;
    }
    private static void executarHillClimbing(Prac1Estat estat, SuccessorFunction successorF) {
        System.out.println("- Hill Climbing:\n");
        try {
            //Instanciem Problem:
            Problem problema = new Problem(estat, successorF, new Prac1GoalTest(), new Prac1HeuristicFunction());
            //Definim l'objecte Search (creem una instància de l'algorisme):
            Search search = new HillClimbingSearch();

            Instant start = Instant.now();
            search.search(problema); //EXECUTEM LA CERCA
            Instant fin = Instant.now();

            Prac1Estat resultat = (Prac1Estat) (search).getGoalState(); //es demana a search el millor estat trobat durant la cerca i el converteix a tipus Prac1Estat
            Metrics metrics = search.getMetrics(); //retorna nodesExpanded
            System.out.println("Cerca completada en: " + Duration.between(start, fin).toMillis() + " ms");
            mostrarMetriques(metrics);
            System.out.println("Heurística final: " + new Prac1HeuristicFunction().getHeuristicValue(resultat));
            System.out.println("Peticions no ateses: " + resultat.peticions_no_ateses.size());
            System.out.println("\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void executarSimulatedAnnealing(Prac1Estat estado, SuccessorFunction successorFunction) {
        System.out.println("- Simulated Annealing:\n");
        try {
            Problem problema = new Problem(estado, successorFunction, new Prac1GoalTest(), new Prac1HeuristicFunction());
            //400: Nombre màxim de passos que farà l'algorisme
            //1: La temperatura baixarà cada 100 iteracions
            //1500: Valor inicial de la temperatura
            //0.0001: controla la velocitat a la qual baixa la temperatura
            Search search = new SimulatedAnnealingSearch(400, 1, 1500, 0.0001);


            Instant start = Instant.now();
            search.search(problema);
            Instant fin = Instant.now();

            Prac1Estat resultat = (Prac1Estat) (search).getGoalState();
            Metrics metrics = search.getMetrics();

            System.out.println("Cerca completada en: " + Duration.between(start, fin).toMillis() + " ms");
            mostrarMetriques(metrics); //retorna NodesExpanded
            System.out.println("Heurística final: " + new Prac1HeuristicFunction().getHeuristicValue(resultat));
            System.out.println("Peticions no ateses: " + resultat.peticions_no_ateses.size());
            System.out.println("\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void mostrarMetriques(Metrics metricas) {
        if (metricas == null) return;
        Iterator keys = metricas.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String valor = (String) metricas.get(key);
            System.out.println(key + " : " + valor);
        }
    }


}