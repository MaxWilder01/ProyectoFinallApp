package com.ufro.appfinanzas.appfianzas;

public class InformacionTotales {
    private int totalIngresos;
    private int totalGastos;

    public InformacionTotales(int totalIngresos, int totalGastos) {
        this.totalIngresos = totalIngresos;
        this.totalGastos = totalGastos;
    }

    public InformacionTotales() {
        this.totalIngresos = 0;
        this.totalGastos = 0;
    }

    public int getTotalGastos() {
        return totalGastos;
    }

    public void setTotalGastos(int totalGastos) {
        this.totalGastos = totalGastos;
    }

    public int getTotalIngresos() {

        return totalIngresos;
    }

    public void setTotalIngresos(int totalIngresos) {
        this.totalIngresos = totalIngresos;
    }
}
