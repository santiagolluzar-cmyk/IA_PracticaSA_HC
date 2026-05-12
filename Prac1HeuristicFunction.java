import IA.Gasolina.*;

import aima.search.framework.HeuristicFunction;
import java.util.ArrayList;

public class Prac1HeuristicFunction implements HeuristicFunction {

    @Override
    public double getHeuristicValue(Object state) {
        Prac1Estat estat = (Prac1Estat) state;

        int distanciaTotal = 0;
        int beneficiTotal = 0;

        //Suma les distàncies dels camions
        for (Distribucion d : estat.distancies.keySet()) {
            distanciaTotal += estat.distancies.get(d);
        }

        //Suma el benefici total de les peticions ateses
        for (Distribucion d : estat.rutes.keySet()) {
            ArrayList<Gasolinera> ruta = estat.rutes.get(d);
            for (Gasolinera g : ruta) { //iterem per les peticions dels centres
                ArrayList<Integer> peticiones = g.getPeticiones();
                for (Integer dies : peticiones) {

                    //%preu = 100 - 2 * dies
                    int percent = 100 - 2 * dies; //ens dona el  % del que cobrarà la companyia
                    if (percent < 0) percent = 0;


                    int valor = (percent * 1000) / 100;
                    beneficiTotal += valor;
                }
            }
        }

        //Heurística: minimizar cost - benefici. Cada quilòmetre costa 2
        return distanciaTotal * 2 - beneficiTotal;
    }
}