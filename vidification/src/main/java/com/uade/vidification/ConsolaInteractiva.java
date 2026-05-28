package com.uade.vidification;

import com.uade.vidification.dto.AvatarDTO;
import com.uade.vidification.dto.HabitoDTO;
import com.uade.vidification.dto.ResultadoCompletarDTO;
import com.uade.vidification.model.CategoriaHabito;
import com.uade.vidification.model.Dificultad;
import com.uade.vidification.model.Habito;
import com.uade.vidification.model.Usuario;
import com.uade.vidification.service.VidificationFacade;
import java.util.List;
import java.util.Scanner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Consola interactiva. Al arrancar la app deja precargados el usuario y sus dos hábitos,
 * y ofrece un menú para que el jugador decida en vivo: darle de comer, hacerlo entrenar,
 * terminar el día, etc. Usa el mismo {@link VidificationFacade} que la API REST.
 */
@Component
public class ConsolaInteractiva implements CommandLineRunner {

    private static final int SEGMENTOS_BARRA = 10;

    private final VidificationFacade facade;

    public ConsolaInteractiva(VidificationFacade facade) {
        this.facade = facade;
    }

    @Override
    public void run(String... args) {
        Usuario usuario = facade.crearUsuario("Jugador");
        Long uid = usuario.getId();
        Habito comer = facade.agregarHabito(uid, "Comer saludable", CategoriaHabito.ALIMENTACION, Dificultad.FACIL);
        Habito entrenar = facade.agregarHabito(uid, "Entrenar", CategoriaHabito.EJERCICIO, Dificultad.MEDIA);

        Scanner scanner = new Scanner(System.in);
        System.out.println();
        System.out.println("====== VIDIFICATION — cuidá a tu avatar 🐾 ======");

        boolean salir = false;
        while (!salir) {
            mostrarAvatar(uid);
            mostrarMenu();
            if (!scanner.hasNextLine()) {
                break; // fin de la entrada
            }
            String opcion = scanner.nextLine().trim();
            switch (opcion) {
                case "1" -> completar(comer.getId());
                case "2" -> completar(entrenar.getId());
                case "3" -> terminarDia(uid);
                case "4" -> mostrarHabitos(uid);
                case "0" -> salir = true;
                default -> System.out.println("\n⚠️  Opción inválida, probá de nuevo.");
            }
        }

        System.out.println("\n¡Hasta luego! 👋");
        scanner.close();
        System.exit(0);
    }

    private void mostrarMenu() {
        System.out.println();
        System.out.println("¿Qué querés hacer?");
        System.out.println("  1) 🍎 Darle de comer  (completar 'Comer saludable')");
        System.out.println("  2) 🏃 Hacerlo entrenar (completar 'Entrenar')");
        System.out.println("  3) 🌙 Terminar el día  (lo no cumplido lo deteriora)");
        System.out.println("  4) 📋 Ver los hábitos");
        System.out.println("  0) 🚪 Salir");
        System.out.print("Opción: ");
    }

    private void completar(Long habitoId) {
        ResultadoCompletarDTO resultado = facade.completarHabito(habitoId);
        System.out.println("\n✅ Completaste \"" + resultado.habito() + "\"  (+" + resultado.puntosGanados()
                + " pts, total " + resultado.puntosTotales() + ")");
    }

    private void terminarDia(Long usuarioId) {
        facade.pasarDia(usuarioId);
        System.out.println("\n🌙 Terminó el día. Los hábitos sin cumplir afectaron al avatar.");
    }

    private void mostrarHabitos(Long usuarioId) {
        List<HabitoDTO> habitos = facade.listarHabitos(usuarioId);
        System.out.println();
        for (HabitoDTO habito : habitos) {
            String marca = habito.completadoHoy() ? "✔" : "▫";
            System.out.println("  " + marca + " " + habito.nombre() + " — racha " + habito.rachaActual()
                    + ", vale " + habito.puntaje() + " pts");
        }
    }

    private void mostrarAvatar(Long usuarioId) {
        AvatarDTO avatar = facade.verAvatar(usuarioId);
        System.out.println();
        System.out.println("   " + caritaAscii(avatar.estado()) + "   " + avatar.emoji() + " " + avatar.estado());
        System.out.println("   Vitalidad " + barra(avatar.vitalidad()) + " " + avatar.vitalidad() + "/100"
                + "    Puntos: " + avatar.puntos());
        System.out.println("   " + avatar.mensaje());
    }

    private String barra(int vitalidad) {
        int llenos = (int) Math.round(vitalidad / 100.0 * SEGMENTOS_BARRA);
        return "[" + "█".repeat(llenos) + "░".repeat(SEGMENTOS_BARRA - llenos) + "]";
    }

    private String caritaAscii(String estado) {
        return switch (estado) {
            case "SALUDABLE" -> "\\(^_^)/";
            case "NORMAL" -> "( ^_^ )";
            case "DECAIDO" -> "( >_< )";
            default -> "( x_x )";
        };
    }
}
