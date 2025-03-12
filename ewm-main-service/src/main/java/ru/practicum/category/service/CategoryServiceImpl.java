package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryInputDto;
import ru.practicum.category.dto.CategoryOutputDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.dao.CategoryRepository;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.DuplicatedDataException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryOutputDto createCategory(CategoryInputDto categoryInputDto) {
        if (categoryRepository.findAll().contains(categoryMapper.toCategory(categoryInputDto))) {
            throw new DuplicatedDataException("Эта категория уже существует.");
        }
        Category category = categoryRepository.save(categoryMapper.toCategory(categoryInputDto));
        return categoryMapper.toCategoryOutputDto(category);
    }

    @Override
    public CategoryOutputDto updateCategory(long catId, CategoryInputDto categoryInputDto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категории с id = {} не существует." + catId));

        if (Objects.nonNull(categoryInputDto.getName())) {
            category.setName(categoryInputDto.getName());
        }

        return categoryMapper.toCategoryOutputDto(category);
    }

    @Override
    public void deleteCategory(long catId) {
        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("Нельзя удалить категорию, с которой связаны события.");
        }
        categoryRepository.deleteById(catId);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryOutputDto getCategoryById(long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категории с id = {} не существует." + catId));
        return categoryMapper.toCategoryOutputDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryOutputDto> getAllCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        return categoryMapper.toCategoryOutputDtoList(categoryPage.getContent());
    }
}