package com.elhg.webflux.apirest.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.elhg.webflux.apirest.models.documents.Categoria;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String> {

}
