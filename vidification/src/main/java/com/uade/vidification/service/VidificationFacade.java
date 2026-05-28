package com.uade.vidification.service;

import com.uade.vidification.dto.AvatarDTO;
import com.uade.vidification.dto.HabitoDTO;
import com.uade.vidification.dto.ResultadoCompletarDTO;
import com.uade.vidification.factory.HabitoFactory;
import com.uade.vidification.model.Avatar;
import com.uade.vidification.model.CategoriaHabito;
import com.uade.vidification.model.Dificultad;
import com.uade.vidification.model.Habito;
import com.uade.vidification.model.RegistroHabito;
import com.uade.vidification.model.Usuario;
import com.uade.vidification.observer.EventoHabito;
import com.uade.vidification.observer.SujetoHabitos;
import com.uade.vidification.repository.HabitoRepository;
import com.uade.vidification.repository.RegistroHabitoRepository;
import com.uade.vidification.repository.UsuarioRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Patrón Facade: expone una interfaz simple para toda la lógica de Vidification y orquesta
 * los subsistemas (repositorios JPA, fábrica de hábitos, estrategias, avatar y observadores).
 * Es el único punto de entrada que usan el controller REST y la demo por consola.
 */
@Service
public class VidificationFacade {

    private static final int VITALIDAD_INCREMENTO_POR_PESO = 4;
    private static final int PENALIZACION_POR_INCUMPLIMIENTO = 6;

    private final UsuarioRepository usuarioRepository;
    private final HabitoRepository habitoRepository;
    private final RegistroHabitoRepository registroRepository;
    private final HabitoFactory habitoFactory;
    private final SujetoHabitos sujetoHabitos;

    public VidificationFacade(UsuarioRepository usuarioRepository,
                              HabitoRepository habitoRepository,
                              RegistroHabitoRepository registroRepository,
                              HabitoFactory habitoFactory,
                              SujetoHabitos sujetoHabitos) {
        this.usuarioRepository = usuarioRepository;
        this.habitoRepository = habitoRepository;
        this.registroRepository = registroRepository;
        this.habitoFactory = habitoFactory;
        this.sujetoHabitos = sujetoHabitos;
    }

    @Transactional
    public Usuario crearUsuario(String nombre) {
        return usuarioRepository.save(new Usuario(nombre));
    }

    @Transactional
    public Habito agregarHabito(Long usuarioId, String nombre, CategoriaHabito categoria, Dificultad dificultad) {
        Usuario usuario = obtenerUsuario(usuarioId);
        Habito habito = habitoFactory.crear(nombre, categoria, dificultad);
        habito.setUsuario(usuario);
        return habitoRepository.save(habito);
    }

    @Transactional
    public ResultadoCompletarDTO completarHabito(Long habitoId) {
        Habito habito = obtenerHabito(habitoId);
        Usuario usuario = habito.getUsuario();

        habito.registrarCumplimiento();
        int puntosGanados = habito.puntajeTotal();
        int incrementoVitalidad = habito.getDificultad().getPeso() * VITALIDAD_INCREMENTO_POR_PESO;

        sujetoHabitos.notificarCompletado(new EventoHabito(usuario, habito, puntosGanados, incrementoVitalidad));

        registroRepository.save(new RegistroHabito(habito, LocalDate.now(), puntosGanados));
        habitoRepository.save(habito);
        usuarioRepository.save(usuario);

        Avatar avatar = new Avatar(usuario);
        return new ResultadoCompletarDTO(habito.getNombre(), puntosGanados, usuario.getPuntos(),
                usuario.getVitalidad(), avatar.getEstado().getNombre(), avatar.getEstado().getEmoji());
    }

    /** Cierra el día: penaliza los hábitos no cumplidos (el avatar se deteriora) y reinicia el día. */
    @Transactional
    public void pasarDia(Long usuarioId) {
        Usuario usuario = obtenerUsuario(usuarioId);
        Avatar avatar = new Avatar(usuario);
        for (Habito habito : habitoRepository.findByUsuarioId(usuarioId)) {
            if (!habito.isCompletadoHoy()) {
                habito.marcarFallado();
                avatar.penalizar(PENALIZACION_POR_INCUMPLIMIENTO);
            }
            habito.nuevoDia();
            habitoRepository.save(habito);
        }
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public AvatarDTO verAvatar(Long usuarioId) {
        Usuario usuario = obtenerUsuario(usuarioId);
        Avatar avatar = new Avatar(usuario);
        return new AvatarDTO(usuario.getNombre(), usuario.getVitalidad(), avatar.getEstado().getNombre(),
                avatar.getEstado().getEmoji(), avatar.getEstado().getMensaje(), usuario.getPuntos());
    }

    @Transactional(readOnly = true)
    public List<HabitoDTO> listarHabitos(Long usuarioId) {
        return habitoRepository.findByUsuarioId(usuarioId).stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public Habito buscarHabito(Long habitoId) {
        return obtenerHabito(habitoId);
    }

    private HabitoDTO aDTO(Habito habito) {
        return new HabitoDTO(habito.getId(), habito.getNombre(), habito.getCategoria().name(),
                habito.getDificultad().name(), habito.getRachaActual(), habito.isCompletadoHoy(),
                habito.puntajeTotal());
    }

    private Usuario obtenerUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
    }

    private Habito obtenerHabito(Long id) {
        return habitoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Hábito no encontrado: " + id));
    }
}
