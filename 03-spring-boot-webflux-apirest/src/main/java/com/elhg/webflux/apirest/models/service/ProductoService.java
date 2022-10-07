package com.elhg.webflux.apirest.models.service;

import com.elhg.webflux.apirest.models.documents.Categoria;
import com.elhg.webflux.apirest.models.documents.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoService {
	//Metodos Productos
	public Flux<Producto> findAll();
	public Flux<Producto> findAllConNombreUpperCase();
	public Flux<Producto> findAllConNombreUpperCaseRepeat();
	public Mono<Producto> findById(String id);
	public Mono<Producto> save(Producto producto);
	public Mono<Void> delete(Producto producto);
	
	//Metodos Categorias
	public Flux<Categoria> findAllCategoria();
	public Mono<Categoria> findCategoriaById(String id);
	public Mono<Categoria> saveCategoria(Categoria categoria);


}
