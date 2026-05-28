package com.uade.vidification.observer;

import com.uade.vidification.model.Avatar;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Observador que actualiza la vitalidad y el estado del avatar (patrón State) ante un cumplimiento. */
@Component
@Order(1)
public class ObservadorAvatar implements ObservadorHabito {

    @Override
    public void alCompletarHabito(EventoHabito evento) {
        Avatar avatar = new Avatar(evento.usuario());
        boolean cambioEstado = avatar.recompensar(evento.incrementoVitalidadBase());
        if (cambioEstado) {
            System.out.println("   🔔 ¡El avatar ahora está " + avatar.getEstado().getNombre()
                    + " " + avatar.getEstado().getEmoji() + "!");
        }
    }
}
