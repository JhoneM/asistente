package com.uade.vidification.observer;

import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Sujeto observable. Spring inyecta automáticamente todos los {@link ObservadorHabito}
 * registrados como beans (respetando su orden) y este sujeto los notifica al ocurrir un evento.
 */
@Component
public class SujetoHabitos {

    private final List<ObservadorHabito> observadores;

    public SujetoHabitos(List<ObservadorHabito> observadores) {
        this.observadores = observadores;
    }

    public void notificarCompletado(EventoHabito evento) {
        for (ObservadorHabito observador : observadores) {
            observador.alCompletarHabito(evento);
        }
    }
}
