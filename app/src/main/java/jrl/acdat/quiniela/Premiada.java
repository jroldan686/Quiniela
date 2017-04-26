package jrl.acdat.quiniela;

// Información sobre las categorías en: https://www.ventura24.es/quiniela
enum Categoria {
    especial,       // 14 aciertos + el pleno al 15
    primera,        // 14 aciertos
    segunda,        // 13 aciertos
    tercera,        // 12 aciertos
    cuarta,         // 11 aciertos
    quinta          // 10 aciertos
}

public class Premiada {

    private String apuesta;
    private Categoria categoria;
    private float premio;

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

    public float getPremio() {
        return premio;
    }

    public void setPremio(float premio) {
        this.premio = premio;
    }
}
