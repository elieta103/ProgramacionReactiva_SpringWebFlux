package com.elhg.springboot.webflux.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.elhg.springboot.webflux.app.models.documents.Producto;
import com.elhg.springboot.webflux.app.models.services.ProductoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {

	private static final Logger log = LoggerFactory.getLogger(ProductoController.class); 
	
	private ProductoService service;

	public ProductoRestController(ProductoService service) {
		this.service=service;
	}
	
	@GetMapping()
	public Flux<Producto> index () {
		Flux<Producto> productos = service.findAllConNombreUpperCase();
									  
		productos.collectList().subscribe(p -> log.info(p.toString()));
		return productos; 
	}

	@GetMapping("/{id}")
	public Mono<Producto> show (@PathVariable String id) {
		//Mono<Producto> producto = dao.findById(id);
		Flux<Producto> productos = service.findAll();	   
		Mono<Producto> producto = productos
								  .filter(prod -> prod.getId().equals(id))
								  .next();
		producto.subscribe(p -> log.info(p.toString()));
		return producto; 
	}
}
