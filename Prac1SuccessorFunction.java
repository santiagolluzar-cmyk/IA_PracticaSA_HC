import IA.Gasolina.*;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;
import java.util.ArrayList;
import java.util.List;


public class Prac1SuccessorFunction implements SuccessorFunction {

    //Aquest mètode genera tots els estats veïns aplicant els operadors: afegirPeticio, eliminarPeticio, swapPeticions
    //Retorna una llista de tots els estats successors
    public List<Successor> getSuccessors(Object stateObj) {
        ArrayList<Successor> successors = new ArrayList<>();
        Prac1Estat estat = (Prac1Estat) stateObj; //convertim l'objecte genèric en la classe Prac1Estat

        for (Distribucion centre : estat.rutes.keySet()) {  //Recorrem els centres
            ArrayList<Gasolinera> ruta = estat.rutes.get(centre);

            //ELIMINAR PETICIÓ:
            for (int i = 0; i < ruta.size(); i++) {
                Prac1Estat nouEstat = estat.clone();
                nouEstat.eliminarPeticio(centre, i); //eliminem la petició "i" de la ruta

                String action = "ELIMINAR petició = " + i + " de centre = " + centre;
                successors.add(new Successor(action, nouEstat));
            }


            //AFEGIR PETICIÓ:
            if (estat.peticions_no_ateses.size() > 0 && ruta.size() < 10) {
                for (int i = 0; i < estat.peticions_no_ateses.size(); i++) {
                    Prac1Estat nouEstat = estat.clone();
                    nouEstat.afegirPeticio(centre, i);

                    Integer dist = nouEstat.distancies.get(centre);
                    if (dist != null && dist <= Prac1Estat.kmMax) {
                        String action = "AFEGIR petició =" + i + " de centre = " + centre;
                        successors.add(new Successor(action, nouEstat));
                    }
                }
            }
        }


        //SWAP DE PETICIONS:
        ArrayList<Distribucion> centres = new ArrayList<>(estat.rutes.keySet());
        for (int i = 0; i < centres.size(); i++) { //Anem comparant 2 centres
            for (int j = i + 1; j < centres.size(); j++) {
                Distribucion c1 = centres.get(i);
                Distribucion c2 = centres.get(j);
                ArrayList<Gasolinera> ruta1 = estat.rutes.get(c1);
                ArrayList<Gasolinera> ruta2 = estat.rutes.get(c2);


                for (int index1 = 0; index1 < ruta1.size(); index1++) {
                    for (int index2 = 0; index2 < ruta2.size(); index2++) {
                        Prac1Estat newState = estat.clone();
                        newState.swapPeticions(c1, c2, index1, index2);

                        Integer d1 = newState.distancies.get(c1);
                        Integer d2 = newState.distancies.get(c2);
                        if ((d1 == null || d1 <= Prac1Estat.kmMax) &&
                                (d2 == null || d2 <= Prac1Estat.kmMax)) {
                            String action = "SWAP centre = " + c1 + " i petició = " + index1 + "] amb centre = " + c2 + " i petició = " + index2;
                            successors.add(new Successor(action, newState));
                        }
                    }
                }
            }
        }


        return successors;
    }
}


