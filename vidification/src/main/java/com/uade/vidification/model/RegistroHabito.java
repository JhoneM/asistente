package com.uade.vidification.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;

/** Registro histórico de cada cumplimiento de un hábito (queda en la tabla SQL). */
@Entity
public class RegistroHabito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Habito habito;

    private LocalDate fecha;
    private int puntosObtenidos;

    protected RegistroHabito() {
    }

    public RegistroHabito(Habito habito, LocalDate fecha, int puntosObtenidos) {
        this.habito = habito;
        this.fecha = fecha;
        this.puntosObtenidos = puntosObtenidos;
    }

    public Long getId() {
        return id;
    }

    public Habito getHabito() {
        return habito;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public int getPuntosObtenidos() {
        return puntosObtenidos;
    }
}
