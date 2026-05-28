package com.uade.vidification.model;

/**
 * Componente del patrón Composite. Lo implementan tanto la hoja ({@link Habito})
 * como el compuesto ({@link Rutina}), de modo que el cliente los trata de forma uniforme.
 */
public interface ComponenteHabito {

    String getNombre();

    int puntajeTotal();

    void imprimir(String sangria);
}
