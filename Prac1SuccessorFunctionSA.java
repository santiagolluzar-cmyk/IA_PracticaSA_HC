import IA.Gasolina.*;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;
import java.util.*;

public class Prac1SuccessorFunctionSA implements SuccessorFunction {
//Aquesta classe generarà només un únic estat successor aleatori
    private final Random random = new Random();

    @Override
    public List<Successor> getSuccessors(Object stateObj) {
        Prac1Estat state = (Prac1Estat) stateObj; //convertim l'objecte genèric en la classe Prac1Estat
        ArrayList<Successor> successors = new ArrayList<>();

        int operador = random.nextInt(3); //Escull un operador aleatòriament: 0: afegirPeticio  1: eliminarPeticio  2:swapPeticions
        boolean success = false;

        //Intenta generar un successor vàlid fins 30 cops. Si un operador falla provarà amb un altre
        for (int intents = 0; intents < 30 && !success; intents++) {
            Prac1Estat nouEstat = state.clone();

            if(operador == 0) { //AFEGIR PETICIÓ
                if (!nouEstat.peticions_no_ateses.isEmpty()) {
                    Distribucion centre = randomCentre(nouEstat); //agafa un centre aleatori
                    if (centre != null && nouEstat.rutes.get(centre).size() < 10) {
                        int indexPeticio = random.nextInt(nouEstat.peticions_no_ateses.size()); //agafa una petició no atesa aleatòria

                        nouEstat.afegirPeticio(centre, indexPeticio);

                        if (nouEstat.distancies.get(centre) <= Prac1Estat.kmMax) {
                            successors.add(new Successor("AFEGIR", nouEstat));
                            success = true;
                        }
                    }
                }
            }

            else if (operador == 1) { //ELIMINAR PETICIÓ
                Distribucion centre = randomCentreAmbPeticions(nouEstat); //Agafa un centre que tingui peticions
                if (centre != null) {
                    int index = random.nextInt(nouEstat.rutes.get(centre).size()); //Escull petició aleatòria de la ruta

                    nouEstat.eliminarPeticio(centre, index);

                    successors.add(new Successor("ELIMINAR", nouEstat));
                    success = true;
                }
            }

            else { //SWAP DE PETICIONS
                Distribucion c1 = randomCentreAmbPeticions(nouEstat);
                Distribucion c2 = randomCentreAmbPeticions(nouEstat);

                if (c1 != null && c2 != null && !c1.equals(c2)) { //ens hem d'assegurar que no s'hagin escollit els mateixos centres
                    int i1 = random.nextInt(nouEstat.rutes.get(c1).size());
                    int i2 = random.nextInt(nouEstat.rutes.get(c2).size());
                    nouEstat.swapPeticions(c1, c2, i1, i2);

                    if (nouEstat.distancies.get(c1) <= Prac1Estat.kmMax && nouEstat.distancies.get(c2) <= Prac1Estat.kmMax) {
                        successors.add(new Successor("SWAP", nouEstat));
                        success = true;
                    }
                }

            }

            if (!success) operador = (operador + 1) % 3; //Si l'operador no s'ha pogut aplicar, passem al seguent per a un pròxim intent
            //0 -> 1, 1 -> 2, 2 -> 0
        }

        return successors;
    }

    //FUNCIONS AUXILIARS

    //Obté un centre aleatori
    private Distribucion randomCentre(Prac1Estat state) {
        if (state.rutes.isEmpty()) return null;
        List<Distribucion> centres = new ArrayList<>(state.rutes.keySet());
        return centres.get(random.nextInt(centres.size()));
    }


    private Distribucion randomCentreAmbPeticions(Prac1Estat state) {
        List<Distribucion> centresConPeticiones = new ArrayList<>();
        for (Map.Entry<Distribucion, ArrayList<Gasolinera>> entry : state.rutes.entrySet()) { //recorre totes les rutes
            if (!entry.getValue().isEmpty()) { //mira que la ruta no estigui buida
                centresConPeticiones.add(entry.getKey()); //afegeix el centre
            }
        }
        if (centresConPeticiones.isEmpty()) return null;
        return centresConPeticiones.get(random.nextInt(centresConPeticiones.size())); //de tots els centres amb ruta no buida escull un aleatòriament
    }
}