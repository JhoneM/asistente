package com.uade.vidification.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Compuesto del patrón Composite: agrupa hábitos (hojas) y/o sub-rutinas (otros compuestos),
 * y los trata de forma uniforme a través de {@link ComponenteHabito}.
 */
public class Rutina implements ComponenteHabito {

    private final String nombre;
    private final List<ComponenteHabito> componentes = new ArrayList<>();

    public Rutina(String nombre) {
        this.nombre = nombre;
    }

    public Rutina agregar(ComponenteHabito componente) {
        componentes.add(componente);
        return this;
    }

    @Override
    public int puntajeTotal() {
        return componentes.stream().mapToInt(ComponenteHabito::puntajeTotal).sum();
    }

    @Override
    public void imprimir(String sangria) {
        System.out.println(sangria + "📁 " + nombre + " (total: " + puntajeTotal() + " pts)");
        for (ComponenteHabito componente : componentes) {
            componente.imprimir(sangria + "   ");
        }
    }

    @Override
    public String getNombre() {
        return nombre;
    }
}
