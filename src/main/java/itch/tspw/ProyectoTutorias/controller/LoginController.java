package itch.tspw.ProyectoTutorias.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    // Mostrar la pantalla de login
    @GetMapping("/login")
    public String cargarPantallaLogin(@RequestParam(value = "error", required = false) String error,
                                      Model model) {
        if (error != null) {
            model.addAttribute("error", "Credenciales inválidas o usuario inactivo.");
        }
        return "login"; 
    }

}