package com.uade.vidification.controller;

import com.uade.vidification.dto.AvatarDTO;
import com.uade.vidification.dto.CrearHabitoRequest;
import com.uade.vidification.dto.CrearUsuarioRequest;
import com.uade.vidification.dto.HabitoDTO;
import com.uade.vidification.dto.ResultadoCompletarDTO;
import com.uade.vidification.model.Habito;
import com.uade.vidification.model.Usuario;
import com.uade.vidification.service.VidificationFacade;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class VidificationController {

    private final VidificationFacade facade;

    public VidificationController(VidificationFacade facade) {
        this.facade = facade;
    }

    @PostMapping("/usuarios")
    public Usuario crearUsuario(@RequestBody @Valid CrearUsuarioRequest request) {
        return facade.crearUsuario(request.nombre());
    }

    @PostMapping("/usuarios/{id}/habitos")
    public Habito agregarHabito(@PathVariable Long id, @RequestBody @Valid CrearHabitoRequest request) {
        return facade.agregarHabito(id, request.nombre(), request.categoria(), request.dificultad());
    }

    @GetMapping("/usuarios/{id}/habitos")
    public List<HabitoDTO> listarHabitos(@PathVariable Long id) {
        return facade.listarHabitos(id);
    }

    @GetMapping("/usuarios/{id}/avatar")
    public AvatarDTO verAvatar(@PathVariable Long id) {
        return facade.verAvatar(id);
    }

    @PostMapping("/habitos/{id}/completar")
    public ResultadoCompletarDTO completar(@PathVariable Long id) {
        return facade.completarHabito(id);
    }

    @PostMapping("/usuarios/{id}/pasar-dia")
    public AvatarDTO pasarDia(@PathVariable Long id) {
        facade.pasarDia(id);
        return facade.verAvatar(id);
    }
}
