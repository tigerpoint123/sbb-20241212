package org.example.sbb.category;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.example.sbb.DataNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public void create(@NotEmpty(message = "카테고리 이름은 필수입니다") String name) {
        Category category = new Category();
        category.setCategoryName(name);
        categoryRepository.save(category);
    }

    public List<Category> findName() {
        return this.categoryRepository.findAll();
    }

    public Category findCategory(Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isPresent()) return category.get();
        else throw new DataNotFoundException("카테고리가 없습니다");
    }
}
