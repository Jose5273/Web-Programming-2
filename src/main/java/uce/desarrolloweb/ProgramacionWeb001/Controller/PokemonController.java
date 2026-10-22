package uce.desarrolloweb.ProgramacionWeb001.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PokemonController {

    @GetMapping("/pokemon")
    public String paginaPokemon() {
        return "pokemon";
    }
}