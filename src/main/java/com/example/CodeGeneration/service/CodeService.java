package com.example.CodeGeneration.service;

import com.example.CodeGeneration.model.CodeSnippetEntity;
import com.example.CodeGeneration.repository.CodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CodeService {
    @Autowired
    private CodeRepository codeRepository;

    public CodeSnippetEntity saveCodeSnippet(CodeSnippetEntity codeSnippet) {
        return codeRepository.save(codeSnippet);
    }
}
