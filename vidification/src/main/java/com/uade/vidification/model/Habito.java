package com.uade.vidification.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uade.vidification.model.strategy.TipoEstrategia;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Hoja del patrón Composite y entidad persistida. Calcula su puntaje delegando
 * en una {@link com.uade.vidification.model.strategy.EstrategiaPuntaje} (patrón Strategy).
 */
@Entity
public class Habito implements ComponenteHabito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Enumerated(EnumType.STRING)
    private CategoriaHabito categoria;

    @Enumerated(EnumType.STRING)
    private Dificultad dificultad;

    @Enumerated(EnumType.STRING)
    private TipoEstrategia tipoEstrategia;

    private int rachaActual;
    private boolean completadoHoy;

    @ManyToOne
    @JsonIgnore
    private Usuario usuario;

    protected Habito() {
    }

    public Habito(String nombre, CategoriaHabito categoria, Dificultad dificultad, TipoEstrategia tipoEstrategia) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.dificultad = dificultad;
        this.tipoEstrategia = tipoEstrategia;
        this.rachaActual = 0;
        this.completadoHoy = false;
    }

    @Override
    public int puntajeTotal() {
        return tipoEstrategia.crear().calcular(this);
    }

    @Override
    public void imprimir(String sangria) {
        String marca = completadoHoy ? "✔" : "▫";
        System.out.println(sangria + marca + " " + categoria.getEmoji() + " " + nombre
                + " (" + dificultad + ", racha " + rachaActual + ") → " + puntajeTotal() + " pts");
    }

    /** Registra que el hábito se cumplió hoy: incrementa la racha y lo marca como completado. */
    public void registrarCumplimiento() {
        this.rachaActual++;
        this.completadoHoy = true;
    }

    /** El hábito no se cumplió: se corta la racha. */
    public void marcarFallado() {
        this.rachaActual = 0;
        this.completadoHoy = false;
    }

    /** Comienza un nuevo día: vuelve a quedar pendiente. */
    public void nuevoDia() {
        this.completadoHoy = false;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    public CategoriaHabito getCategoria() {
        return categoria;
    }

    public Dificultad getDificultad() {
        return dificultad;
    }

    public TipoEstrategia getTipoEstrategia() {
        return tipoEstrategia;
    }

    public int getRachaActual() {
        return rachaActual;
    }

    public boolean isCompletadoHoy() {
        return completadoHoy;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
