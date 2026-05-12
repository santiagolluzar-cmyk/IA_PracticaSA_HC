import IA.Gasolina.*;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.ArrayList;


public class Prac1Estat {

    static ArrayList<Gasolinera> peticions_totals;
    ArrayList<Gasolinera> peticions_no_ateses;

    static CentrosDistribucion centres_dist;

    private static int[][] distCentresGasolineres;
    private static int[][] distGasolineres;

    Map<Distribucion, ArrayList<Gasolinera>> rutes;
    Map<Distribucion, Integer> distancies;


    static int kmMax = 640;
    int benefici;

    public int calcularDistGas(Gasolinera i, Gasolinera j) {
        return Math.abs(i.getCoordX() - j.getCoordX()) + Math.abs(i.getCoordY() - j.getCoordY());
    }

    public int calcDistanciaGasCentr(Gasolinera i, Distribucion c) {
        return Math.abs(i.getCoordX() - c.getCoordX()) + Math.abs(i.getCoordY() - c.getCoordY());
    }

    public Prac1Estat() {}

    public Prac1Estat(Gasolineras gasolineres, CentrosDistribucion centres){
        distGasolineres = new int[gasolineres.size()][gasolineres.size()];
        distCentresGasolineres = new int[centres.size()][gasolineres.size()];

        //INICIALITZEM MATRIUS DISTÀNCIES
        for(int i =0;i<gasolineres.size();i++) {
            for (int j = 0; j <= i; j++) {
                int distance = calcularDistGas(gasolineres.get(i), gasolineres.get(j));
                distGasolineres[i][j] = distGasolineres[j][i] = distance;
            }
        }
        for(int i = 0; i<gasolineres.size();i++) {
            for (int c = 0; c < centres.size(); c++) {
                int distance = calcDistanciaGasCentr(gasolineres.get(i),centres.get(c));
                distCentresGasolineres[c][i] = distance;
            }
        }

        //LLISTEM LES PETICIONS TOTALS
        peticions_totals = new ArrayList <Gasolinera>();
        for (int i=0; i < gasolineres.size(); i++){
            ArrayList<Integer> peticions = gasolineres.get(i).getPeticiones();
            for (int j = 0; j < peticions.size(); j++){
                ArrayList<Integer> peticioaux = new ArrayList<>();
                peticioaux.add(peticions.get(j));
                peticions_totals.add(new Gasolinera(gasolineres.get(i).getCoordX(), gasolineres.get(i).getCoordY(), peticioaux));
            }
        }
    }

//FUNCIONS AUXILIARS
    public void sumaDistanciaAux (Gasolinera peticio_no_atesa, Distribucion centreDist, int index){
        if (index%2==0) {
            int res = calcDistanciaGasCentr(peticio_no_atesa, centreDist);
            int dist_anterior = distancies.get(centreDist);
            //Ha de tornar al centre de distribució ja que és la última petició
            res =  res*2;
            res+=dist_anterior;

            distancies.put(centreDist, res);
        }
        else{
            //Calcula la distància entre la última gasolinera i la nova
            Gasolinera ultima_visitada = rutes.get(centreDist).get(index-1);
            int res = calcularDistGas (ultima_visitada, peticio_no_atesa);
            int dist_anterior = distancies.get(centreDist);
            res+=dist_anterior; // dist total + gasolinera->gasolinera

            //Ha de tornar al centre de distribució (res2) i a més borrem la tornada al centre de distribució de l'ultima_visitada ja que ha deixat de ser la última (res3)
            int res3 = calcDistanciaGasCentr(ultima_visitada, centreDist);
            int res2 = calcDistanciaGasCentr(peticio_no_atesa, centreDist);
            res = res + res2 - res3;

            distancies.put(centreDist, res);
        }
    }

    private int recalcularDistanciaTotal(Distribucion centre) {
        ArrayList<Gasolinera> ruta = rutes.get(centre);
        if (ruta == null || ruta.isEmpty()) {
            return 0;
        }

        int distanciaTotal= 0;

        for (int i = 0; i < ruta.size(); i += 2) {
            Gasolinera primeraParada = ruta.get(i);
            if (i + 1 < ruta.size()) {
                Gasolinera segonaParada = ruta.get(i + 1);

                distanciaTotal+= calcDistanciaGasCentr(primeraParada, centre); //Centre -> primeraParada
                distanciaTotal+= calcularDistGas(primeraParada, segonaParada); //primeraParada -> segonaParada
                distanciaTotal+= calcDistanciaGasCentr(segonaParada, centre); //segonaParada -> Centre
            } else {
                distanciaTotal+= calcDistanciaGasCentr(primeraParada, centre) * 2; //Centre -> primeraParada -> Centre
            }
        }
        return distanciaTotal;
    }
    //Mateixa funció que l'anterior però passant-li una ruta que no té perquè ser la seva
    private int recalcularDistanciaTotal(Distribucion centre, ArrayList<Gasolinera> ruta) {
        if (ruta == null || ruta.isEmpty()) {
            return 0;
        }
        int distanciaTotal = 0;
        for (int i = 0; i < ruta.size(); i += 2) {
            Gasolinera primeraParada = ruta.get(i);
            if (i + 1 < ruta.size()) {
                Gasolinera segundaParada = ruta.get(i + 1);
                distanciaTotal += calcDistanciaGasCentr(primeraParada, centre);
                distanciaTotal += calcularDistGas(primeraParada, segundaParada);
                distanciaTotal += calcDistanciaGasCentr(segundaParada, centre);
            } else {
                distanciaTotal += calcDistanciaGasCentr(primeraParada, centre) * 2;
            }
        }
        return distanciaTotal;
    }

//OPERADORS
    //AFEGIR PETICIÓ: A partir d'un centre i un índex d'una petició no atesa, s'afegeix aquesta petició al centre
    public void afegirPeticio(Distribucion centreDist, int indexPeticio) {
        if (indexPeticio >= 0 && indexPeticio < peticions_no_ateses.size()) {
            Gasolinera peticioEspecifica = peticions_no_ateses.remove(indexPeticio);

            int indexRuta = rutes.get(centreDist).size();
            sumaDistanciaAux(peticioEspecifica, centreDist, indexRuta);

            rutes.get(centreDist).add(peticioEspecifica);
        }
    }

    //ELIMINAR PETICIÓ: Eliminem la petició del centre i la afegim a peticions_no_ateses
    public void eliminarPeticio(Distribucion centreDist, int indexPeticio) {
        ArrayList<Gasolinera> ruta = rutes.get(centreDist);

        if (ruta != null && indexPeticio >= 0 && indexPeticio < ruta.size()) {
            Gasolinera per_borrar = ruta.remove(indexPeticio);
            peticions_no_ateses.add(per_borrar);

            distancies.put(centreDist, recalcularDistanciaTotal(centreDist));
        }
    }

    //SWAP PETICIONS: Intecanviem les peticions entre els dos centres definides per cada índex
    public void swapPeticions(Distribucion centre1, Distribucion centre2, int index1, int index2) {

        Gasolinera gas1 = rutes.get(centre1).get(index1);
        Gasolinera gas2 = rutes.get(centre2).get(index2);

        rutes.get(centre1).set(index1, gas2);
        rutes.get(centre2).set(index2, gas1);

        distancies.put(centre1, recalcularDistanciaTotal(centre1));
        distancies.put(centre2, recalcularDistanciaTotal(centre2));
    }


//ESTATS INICIALS
    //Estat inicial aleatori: Genera un estat aleatori. Reparteix aproximadament el mateix nombre de peticions a cada camió de forma aleatòria
    public void generarEstatInicialAleatori() {
        this.rutes = new HashMap<>();
        this.distancies = new HashMap<>();
        this.benefici = 0;
        this.peticions_no_ateses = new ArrayList<>(peticions_totals);
        Collections.shuffle(this.peticions_no_ateses);

        int peticions_per_camio = 0;
        if (centres_dist != null && !centres_dist.isEmpty()) {
            peticions_per_camio = peticions_totals.size() / centres_dist.size();
        }
        peticions_per_camio = Math.min(peticions_per_camio, 10);

        for (Distribucion centreActual : centres_dist) {
            rutes.put(centreActual, new ArrayList<>());
            distancies.put(centreActual, 0);

            for (int i = 0; i < peticions_per_camio; ++i) {
                if (peticions_no_ateses.isEmpty()) {
                    break;
                }
                ArrayList<Gasolinera> rutaSimulada = new ArrayList<>(rutes.get(centreActual));
                rutaSimulada.add(peticions_no_ateses.get(0));
                int newDist = recalcularDistanciaTotal(centreActual, rutaSimulada);

                if (newDist <= kmMax) {
                    this.afegirPeticio(centreActual, 0);
                } else {
                    break;
                }
            }
        }
    }

    //Estat inicial per proximitat: Per cada petició busca el camió més proper i l'assigna si es compleixen les condicions.
    public void generarEstatInicialProximitat() {
        this.rutes = new HashMap<>();
        this.distancies = new HashMap<>();
        this.peticions_no_ateses = new ArrayList<>();

        for (Distribucion centre : centres_dist) {
            rutes.put(centre, new ArrayList<>());
            distancies.put(centre, 0);
        }

        ArrayList<Gasolinera> peticionsAAssignar = new ArrayList<>(peticions_totals);

        for (Gasolinera peticio : peticionsAAssignar) { //itera per les peticions_totals
            Distribucion centreMesProper = findCentreMesProper(peticio); //per a la petició, busca el centre de distribució que estigui més aprop
            boolean assignat = false;

            //mira si el centre pot acceptar més peticions
            if (centreMesProper != null && rutes.get(centreMesProper).size() < 10) {

                ArrayList<Gasolinera> rutaCentre = new ArrayList<>(rutes.get(centreMesProper));
                rutaCentre.add(peticio); //afegim la petició a la ruta del centre més proper (vector auxiliar) -> no és definitiu, només per recalcular la distància
                int novaDistancia = recalcularDistanciaTotal(centreMesProper, rutaCentre);

                if (novaDistancia <= kmMax) { //si la nova distància recorreguda compleix la restricció afegim la petició
                    rutes.get(centreMesProper).add(peticio);
                    distancies.put(centreMesProper, novaDistancia);
                    assignat = true;
                }
            }

            if (!assignat) {
                this.peticions_no_ateses.add(peticio); //en cas de que no es pugui afegir la petició la posem a no_ateses
            }
        }
    }

    //Aquesta funció auxiliar busca el centre més proper de cada petició
    private Distribucion findCentreMesProper(Gasolinera peticio) {
        Distribucion centreMesProper = null;
        int minDistance = Integer.MAX_VALUE; //comencem amb una distància infinita i així ens assegurem de que el primer centre sempre sigui el més proper

        if (centres_dist == null) return null;

        for (Distribucion centre : centres_dist) { //recorrem tots els centres de distribució
            int distance = calcDistanciaGasCentr(peticio, centre);
            if (distance < minDistance) {//s'actualitza el centre més proper
                minDistance = distance;
                centreMesProper = centre;
            }
        }
        return centreMesProper;
    }

    //Estat inicial buit: Genera un estat inicial buit. Cap camió te rutes assignades i totes les peticions són no ateses
    public void generarEstatInicialBuit() {
        this.rutes = new HashMap<>();
        this.distancies = new HashMap<>();
        this.benefici = 0;

        for (Distribucion centreActual : centres_dist) {
            rutes.put(centreActual, new ArrayList<>());
            distancies.put(centreActual, 0);
        }

        this.peticions_no_ateses = new ArrayList<>(peticions_totals);
    }



//CLONAR

    //CLONE: Retorna una còpia de l'estat actual, així es pot modificar de manera segura sense modificar l'original
    public Prac1Estat clone() {
        Prac1Estat copy = new Prac1Estat();

        if (this.peticions_no_ateses != null) { //còpia de peticions_no_ateses
            copy.peticions_no_ateses = new ArrayList<>();
            for (Gasolinera g : this.peticions_no_ateses) copy.peticions_no_ateses.add(cloneGasolinera(g));
        } else copy.peticions_no_ateses = new ArrayList<>();


        if (this.rutes != null) { //còpia del mapa rutes
            copy.rutes = new HashMap<>();
            for (Map.Entry<Distribucion, ArrayList<Gasolinera>> e : this.rutes.entrySet()) {
                ArrayList<Gasolinera> rutaCopy = new ArrayList<>();
                for (Gasolinera g : e.getValue()) rutaCopy.add(cloneGasolinera(g)); //es clona cada gasolinera

                copy.rutes.put(e.getKey(), rutaCopy);
            }
        } else copy.rutes = new HashMap<>();


        if (this.distancies != null) copy.distancies = new HashMap<>(this.distancies);
        else copy.distancies = new HashMap<>();

        copy.benefici = this.benefici;
        return copy;
    }

        //Funció auxiliar per a clonar gasolineres
    private Gasolinera cloneGasolinera(Gasolinera g) {
        ArrayList<Integer> peticionesCopy = new ArrayList<>();
        if (g.getPeticiones() != null) peticionesCopy.addAll(g.getPeticiones());
        return new Gasolinera(g.getCoordX(), g.getCoordY(), peticionesCopy);
    }

}