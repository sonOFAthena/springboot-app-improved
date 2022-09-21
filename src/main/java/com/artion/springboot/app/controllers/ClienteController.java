package com.artion.springboot.app.controllers;

import ch.qos.logback.core.net.server.Client;
import com.artion.springboot.app.models.entity.Cliente;
import com.artion.springboot.app.models.service.IClienteService;
import com.artion.springboot.app.util.paginator.PageRender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Map;

@Controller
@SessionAttributes("cliente")
public class ClienteController {

    @Autowired
    private IClienteService clienteService;

    @RequestMapping(value="/listar", method = RequestMethod.GET)
    public String listar(@RequestParam(name = "page", defaultValue = "0") int page, Model model){

        Pageable pageRequest = PageRequest.of(page, 4);
        Page<Cliente> clientes = clienteService.findAll(pageRequest);

        PageRender<Cliente> pageRender = new PageRender<>("/listar", clientes);

        model.addAttribute("titulo", "Listado de clientes");
        model.addAttribute("clientes", clientes);
        model.addAttribute("page", pageRender);

        return "listar";
    }

    @RequestMapping(value = "/form")
    public String crear(Map<String, Object> model){
        Cliente cliente = new Cliente();
        model.put("cliente", cliente);
        model.put("titulo", "Formulario de Cliente");
        return "form";
    }

    @RequestMapping(value = "/form/{id}")
    public String editar(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash){
        Cliente cliente = null;

        if (id > 0){
            cliente = clienteService.findOne(id);

            if (cliente == null){
                flash.addFlashAttribute("error", "El Id del cliente no existe en la BBDD");
                return "redirect:/listar";
            }
        } else {
            flash.addFlashAttribute("error", "El Id del cliente no puede ser 0");
            return "redirect:/listar";
        }

        model.put("cliente", cliente);
        model.put("titulo", "Editar Cliente");
        return "form";
    }

    //BindingResult debe estar adyacente a la entity (ex. cliente)
    // el atributo "cliente" del metodo crear se pasa a la vista siempre y cuando el parametro se llame igual "cliente"
    @RequestMapping(value = "/form", method = RequestMethod.POST)
    public String guardar(@Valid Cliente cliente, BindingResult result, Model model, RedirectAttributes flash, SessionStatus status){

        if (result.hasErrors()){
            model.addAttribute("titulo", "Formulario de Cliente");
            return "form";
        }

        String mensajeFlash = (cliente.getId() != null) ? "Cliente editado con exito!" : "Cliente creado con éxito!";

        clienteService.save(cliente);

        //borra la sesion actual del cliente
        status.setComplete();

        flash.addFlashAttribute("success", mensajeFlash);
        return "redirect:listar";
    }

    @RequestMapping(value = "/eliminar/{id}")
    public String eliminar(@PathVariable(value = "id") Long id, RedirectAttributes flash){

        if (id > 0){
           clienteService.delete(id);
           flash.addFlashAttribute("success", "Cliente eliminado con éxito!");
        }

        return "redirect:/listar";
    }
}
