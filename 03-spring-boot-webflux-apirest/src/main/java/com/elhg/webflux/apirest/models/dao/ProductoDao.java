package com.elhg.webflux.apirest.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.elhg.webflux.apirest.models.documents.Producto;

public interface ProductoDao extends ReactiveMongoRepository<Producto, String> {

}
