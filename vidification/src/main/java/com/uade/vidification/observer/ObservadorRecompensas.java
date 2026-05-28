package com.uade.vidification.observer;

import com.uade.vidification.model.Usuario;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Observador que acredita los puntos ganados al usuario. Corre primero. */
@Component
@Order(0)
public class ObservadorRecompensas implements ObservadorHabito {

    @Override
    public void alCompletarHabito(EventoHabito evento) {
        Usuario usuario = evento.usuario();
        usuario.setPuntos(usuario.getPuntos() + evento.puntosGanados());
    }
}
