package com.example.minorapp.controller;

import com.example.minorapp.dto.ApiResponse;
import com.example.minorapp.model.Book;
import com.example.minorapp.service.LibraryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/library")
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping("/books")
    public ApiResponse<List<Book>> getBooks() {
        return new ApiResponse<>("SUCCESS", "Books fetched.", libraryService.getBooks());
    }
}


