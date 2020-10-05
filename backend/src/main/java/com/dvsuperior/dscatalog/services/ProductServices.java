package com.dvsuperior.dscatalog.services;

import com.dvsuperior.dscatalog.dto.CategoryDTO;
import com.dvsuperior.dscatalog.dto.ProductDTO;

import com.dvsuperior.dscatalog.entities.Category;
import com.dvsuperior.dscatalog.entities.Product;
import com.dvsuperior.dscatalog.repositories.CategoryRepository;
import com.dvsuperior.dscatalog.repositories.ProductRepository;
import com.dvsuperior.dscatalog.services.exceptions.DatabaseException;
import com.dvsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class ProductServices {

    @Autowired
    private ProductRepository repository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<ProductDTO> findAllPaged(PageRequest pageRequest){
        Page<Product> list =  repository.findAll(pageRequest);
        return list.map(x -> new ProductDTO(x));
    }

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id){
        Optional<Product> obj = repository.findById(id);
        Product entity = obj.orElseThrow(()-> new ResourceNotFoundException("Entity not found!"));
        return new ProductDTO(entity, entity.getCategories());
    }

    @Transactional
    public ProductDTO insert(ProductDTO dto){
        Product entity = new Product();
        copyDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return new ProductDTO(entity);
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO dto){
        try{
            Product entity = repository.getOne(id);
            copyDtoToEntity(dto,entity);
            entity = repository.save(entity);
            return new ProductDTO(entity);
        }catch(EntityNotFoundException e){
            throw new ResourceNotFoundException("Id not Found: " + id);
        }
    }

    public void delete(Long id){
        try{
            repository.deleteById(id);
        }
        catch(EmptyResultDataAccessException e){
            throw new ResourceNotFoundException("Id not Found: " + id );
        }
        catch(DataIntegrityViolationException e){
            throw new DatabaseException("Integrity violation");
        }
    }

    private void copyDtoToEntity(ProductDTO dto, Product entity) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setDate(dto.getDate());
        entity.setImgUrl(dto.getImgUrl());
        entity.setPrice(dto.getPrice());

        entity.getCategories().clear();
        for(CategoryDTO catDTO : dto.getCategories()){
            Category category = categoryRepository.getOne(catDTO.getId());
            entity.getCategories().add(category);
        }
    }
}
