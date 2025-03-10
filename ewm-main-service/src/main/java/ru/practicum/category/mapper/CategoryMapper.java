package ru.practicum.category.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.category.dto.CategoryInputDto;
import ru.practicum.category.dto.CategoryOutputDto;
import ru.practicum.category.model.Category;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    public Category toCategory(CategoryInputDto categoryInputDto) {
        return new Category(
                categoryInputDto.getId(),
                categoryInputDto.getName()
        );
    }

    public CategoryOutputDto toCategoryOutputDto(Category category) {
        return new CategoryOutputDto(
                category.getId(),
                category.getName()
        );
    }

    public List<CategoryOutputDto> toCategoryOutputDtoList(List<Category> categories) {
        return categories.stream()
                .map(this::toCategoryOutputDto)
                .collect(Collectors.toList());
    }
}