package org.example.sbb.category;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public String create(CategoryForm categoryForm, Model model) {
        return "category_create";
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public String create(@Valid CategoryForm categoryForm, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) return "category_create";
        categoryService.create(categoryForm.getCategoryName());
        return "redirect:/";
    }
}
