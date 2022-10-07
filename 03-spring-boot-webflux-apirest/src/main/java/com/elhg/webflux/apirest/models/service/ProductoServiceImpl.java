package com.elhg.webflux.apirest.models.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.elhg.webflux.apirest.models.dao.CategoriaDao;
import com.elhg.webflux.apirest.models.dao.ProductoDao;
import com.elhg.webflux.apirest.models.documents.Categoria;
import com.elhg.webflux.apirest.models.documents.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductoServiceImpl implements ProductoService{

	private static final Logger log = LoggerFactory.getLogger(ProductoServiceImpl.class); 

	private final ProductoDao dao;
	private final CategoriaDao categoriaDao;
	
	public ProductoServiceImpl(ProductoDao dao,
							   CategoriaDao categoriaDao) {
		this.dao = dao;
		this.categoriaDao = categoriaDao;
	}

	@Override
	public Flux<Producto> findAll() {
		return dao.findAll();
	}

	@Override
	public Mono<Producto> findById(String id) {
		return dao.findById(id);
	}

	@Override
	public Mono<Producto> save(Producto producto) {
		return dao.save(producto);
	}

	@Override
	public Mono<Void> delete(Producto producto) {
		return dao.delete(producto);
	}

	@Override
	public Flux<Producto> findAllConNombreUpperCase() {
		Flux<Producto> productos = dao.findAll()
				  .map(prod ->  {
					  prod.setNombre(prod.getNombre().toUpperCase());
					  return prod;
				  });
		productos.collectList().subscribe(p -> log.info(p.toString()));
		return productos;
	}

	@Override
	public Flux<Producto> findAllConNombreUpperCaseRepeat() {
		Flux<Producto> productos = dao.findAll()
				  .map(prod ->  {
					  prod.setNombre(prod.getNombre().toUpperCase());
					  return prod;
				  });
		productos.collectList().subscribe(p -> log.info(p.toString()));
		productos.repeat(5000);
		return productos;
	}

	@Override
	public Flux<Categoria> findAllCategoria() {
		return categoriaDao.findAll();
	}

	@Override
	public Mono<Categoria> findCategoriaById(String id) {
		return categoriaDao.findById(id);
	}

	@Override
	public Mono<Categoria> saveCategoria(Categoria categoria) {
		return categoriaDao.save(categoria);
	}

}
