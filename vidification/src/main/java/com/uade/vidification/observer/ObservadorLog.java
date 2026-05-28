package com.uade.vidification.observer;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Observador que registra por consola el cumplimiento. Corre último para mostrar los totales finales. */
@Component
@Order(2)
public class ObservadorLog implements ObservadorHabito {

    @Override
    public void alCompletarHabito(EventoHabito evento) {
        System.out.println("   📝 " + evento.usuario().getNombre() + " completó \""
                + evento.habito().getNombre() + "\" (+" + evento.puntosGanados()
                + " pts, total " + evento.usuario().getPuntos() + ")");
    }
}
