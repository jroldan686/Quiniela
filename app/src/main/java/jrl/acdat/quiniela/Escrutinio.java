package jrl.acdat.quiniela;

import java.util.ArrayList;

public class Escrutinio {

    private ArrayList<Premiada> premiadas;
    private String temporada;
    private String jornada;
    private Premio premios;

    public ArrayList<Premiada> getPremiadas() {
        return premiadas;
    }

    public void setPremiadas(ArrayList<Premiada> premiadas) {
        this.premiadas = premiadas;
    }

    public String getTemporada() {
        return temporada;
    }

    public void setTemporada(String temporada) {
        this.temporada = temporada;
    }

    public String getJornada() {
        return jornada;
    }

    public void setJornada(String jornada) {
        this.jornada = jornada;
    }

    public Premio getPremios() {
        return premios;
    }

    public void setPremios(Premio premios) {
        this.premios = premios;
    }
}
