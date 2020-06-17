package com.company.imagebook.repositories;

import com.company.imagebook.entities.Image;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends PagingAndSortingRepository<Image, String> {

}
