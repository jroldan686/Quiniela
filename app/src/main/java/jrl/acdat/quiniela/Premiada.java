package jrl.acdat.quiniela;

enum Categoria {
    ESPECIAL,       // 14 aciertos + el pleno al 15
    PRIMERA,        // 14 aciertos
    SEGUNDA,        // 13 aciertos
    TERCERA,        // 12 aciertos
    CUARTA,         // 11 aciertos
    QUINTA          // 10 aciertos
}

public class Premiada {

    private String apuesta;
    private Categoria categoria;
    private double premio;

    public String getApuesta() {
        return apuesta;
    }

    public void setApuesta(String apuesta) {
        this.apuesta = apuesta;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public double getPremio() {
        return premio;
    }

    public void setPremio(double premio) {
        this.premio = premio;
    }
}
