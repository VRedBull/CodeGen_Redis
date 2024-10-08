package com.example.CodeGeneration.repository;

import com.example.CodeGeneration.model.CodeSnippetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeRepository extends JpaRepository<CodeSnippetEntity, Long> {

}
