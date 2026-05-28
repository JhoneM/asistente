package com.uade.vidification.factory;

import com.uade.vidification.model.CategoriaHabito;
import com.uade.vidification.model.Dificultad;
import com.uade.vidification.model.Habito;
import com.uade.vidification.model.strategy.TipoEstrategia;
import org.springframework.stereotype.Component;

/**
 * Simple Factory: centraliza la creación de hábitos ya configurados, eligiendo la
 * estrategia de puntaje adecuada según la categoría (evita lógica de instanciación dispersa).
 */
@Component
public class HabitoFactory {

    public Habito crear(String nombre, CategoriaHabito categoria, Dificultad dificultad) {
        TipoEstrategia estrategia = switch (categoria) {
            case EJERCICIO, ESTUDIO -> TipoEstrategia.POR_RACHA; // premian la constancia
            default -> TipoEstrategia.BASE;
        };
        return new Habito(nombre, categoria, dificultad, estrategia);
    }
}
