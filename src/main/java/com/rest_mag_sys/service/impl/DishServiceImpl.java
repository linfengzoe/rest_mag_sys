package com.rest_mag_sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rest_mag_sys.common.CustomException;
import com.rest_mag_sys.dto.DishDTO;
import com.rest_mag_sys.dto.PageQueryDTO;
import com.rest_mag_sys.entity.Category;
import com.rest_mag_sys.entity.Dish;
import com.rest_mag_sys.mapper.CategoryMapper;
import com.rest_mag_sys.mapper.DishMapper;
import com.rest_mag_sys.service.CategoryService;
import com.rest_mag_sys.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 菜品服务实现
 */
@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private CategoryService categoryService;

    @Value("${upload.path:/upload/images}")
    private String uploadPath;

    /**
     * 新增菜品，同时插入菜品对应的口味数据
     * @param dishDTO 菜品数据传输对象
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean saveWithFlavor(DishDTO dishDTO) {
        try {
            log.info("准备新增菜品：{}", dishDTO);
            
            // 检查必要字段
            if (dishDTO.getName() == null || dishDTO.getName().trim().isEmpty()) {
                throw new CustomException("菜品名称不能为空");
            }
            if (dishDTO.getCategoryId() == null) {
                throw new CustomException("菜品分类不能为空");
            }
            if (dishDTO.getPrice() == null) {
                throw new CustomException("菜品价格不能为空");
            }
            
            // 保存菜品基本信息
            Dish dish = new Dish();
            BeanUtils.copyProperties(dishDTO, dish);
            
            // 确保时间字段为null，让数据库自动填充
            dish.setCreateTime(null);
            dish.setUpdateTime(null);
            
            boolean result = save(dish);
            log.info("新增菜品结果：{}", result);
            
            // 设置分类名称
            Category category = categoryService.getById(dish.getCategoryId());
            if (category != null) {
                dishDTO.setCategoryName(category.getName());
            }
            
            return result;
        } catch (Exception e) {
            log.error("新增菜品异常：{}", dishDTO, e);
            throw new CustomException("新增菜品失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询
     * @param pageQueryDTO 分页查询参数
     * @return 分页结果
     */
    @Override
    public Page<DishDTO> pageQuery(PageQueryDTO pageQueryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(pageQueryDTO.getName()), Dish::getName, pageQueryDTO.getName());
        queryWrapper.eq(pageQueryDTO.getCategoryId() != null, Dish::getCategoryId, pageQueryDTO.getCategoryId());
        queryWrapper.eq(pageQueryDTO.getStatus() != null, Dish::getStatus, pageQueryDTO.getStatus());
        // 先按分类sort排序，再按菜品id排序
        // 由于MyBatis-Plus的LambdaQueryWrapper不支持跨表排序，这里需要自定义SQL或用XML实现
        // 这里仅做注释说明，实际应在Mapper层自定义SQL
        // queryWrapper.orderByAsc(Category::getSort).orderByAsc(Dish::getId);
        // 临时方案：先查所有菜品，Java代码中按分类sort和菜品id排序
        List<Dish> allDishes = list(queryWrapper);
        List<Category> allCategories = categoryService.list();
        // 按分类sort和菜品id排序
        allDishes.sort((d1, d2) -> {
            Integer sort1 = allCategories.stream().filter(c -> c.getId().equals(d1.getCategoryId())).findFirst().map(Category::getSort).orElse(0);
            Integer sort2 = allCategories.stream().filter(c -> c.getId().equals(d2.getCategoryId())).findFirst().map(Category::getSort).orElse(0);
            if (!sort1.equals(sort2)) {
                return sort1 - sort2;
            } else {
                return d1.getId().compareTo(d2.getId());
            }
        });
        // 构建分页
        int fromIndex = (int) ((pageQueryDTO.getPage() - 1) * pageQueryDTO.getPageSize());
        int toIndex = Math.min(fromIndex + pageQueryDTO.getPageSize(), allDishes.size());
        List<Dish> pageRecords = fromIndex < toIndex ? allDishes.subList(fromIndex, toIndex) : List.of();
        // 构建结果对象
        Page<DishDTO> dishDTOPage = new Page<>();
        dishDTOPage.setCurrent(pageQueryDTO.getPage());
        dishDTOPage.setSize(pageQueryDTO.getPageSize());
        dishDTOPage.setTotal(allDishes.size());
        List<DishDTO> dishDTOList = pageRecords.stream().map(dish -> {
            DishDTO dishDTO = new DishDTO();
            BeanUtils.copyProperties(dish, dishDTO);
            // 设置分类名称
            Category category = allCategories.stream().filter(c -> c.getId().equals(dish.getCategoryId())).findFirst().orElse(null);
            if (category != null) {
                dishDTO.setCategoryName(category.getName());
            }
            return dishDTO;
        }).collect(Collectors.toList());
        dishDTOPage.setRecords(dishDTOList);
        return dishDTOPage;
    }

    /**
     * 根据ID查询菜品信息和对应的口味信息
     * @param id 菜品ID
     * @return 菜品数据传输对象
     */
    @Override
    public DishDTO getByIdWithFlavor(Long id) {
        // 查询菜品基本信息
        Dish dish = getById(id);
        
        DishDTO dishDTO = new DishDTO();
        BeanUtils.copyProperties(dish, dishDTO);
        
        // 设置分类名称
        Category category = categoryService.getById(dish.getCategoryId());
        if (category != null) {
            dishDTO.setCategoryName(category.getName());
        }
        
        return dishDTO;
    }

    /**
     * 更新菜品信息，同时更新对应的口味信息
     * @param dishDTO 菜品数据传输对象
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean updateWithFlavor(DishDTO dishDTO) {
        try {
            log.info("准备更新菜品：{}", dishDTO);
            
            // 检查菜品是否存在
            if (dishDTO.getId() == null) {
                throw new CustomException("菜品ID不能为空");
            }
            
            Dish existingDish = getById(dishDTO.getId());
            if (existingDish == null) {
                throw new CustomException("菜品不存在");
            }
            
            // 更新菜品基本信息
            Dish dish = new Dish();
            BeanUtils.copyProperties(dishDTO, dish);
            
            // 确保价格不为空
            if (dish.getPrice() == null) {
                dish.setPrice(existingDish.getPrice());
            }
            
            // 不更新时间字段，让数据库自动填充
            dish.setCreateTime(null);
            dish.setUpdateTime(null);
            
            boolean result = updateById(dish);
            log.info("更新菜品结果：{}", result);
            
            // 设置分类名称
            if (dish.getCategoryId() != null) {
                Category category = categoryService.getById(dish.getCategoryId());
                if (category != null) {
                    dishDTO.setCategoryName(category.getName());
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("更新菜品异常：{}", dishDTO, e);
            throw new CustomException("更新菜品失败：" + e.getMessage());
        }
    }

    /**
     * 删除菜品，同时删除菜品对应的口味数据
     * @param ids 菜品ID列表
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean removeWithFlavor(List<Long> ids) {
        try {
            log.info("准备删除菜品，ID列表：{}", ids);
            
            // 检查菜品是否存在
            List<Dish> dishes = listByIds(ids);
            if (dishes.isEmpty()) {
                log.warn("要删除的菜品不存在：{}", ids);
                throw new CustomException("菜品不存在");
            }
            
            // 先将菜品状态设为停售，然后删除
            for (Long id : ids) {
                Dish dish = new Dish();
                dish.setId(id);
                dish.setStatus(0); // 设为停售状态
                updateById(dish);
            }
            
            // 删除菜品
            boolean result = removeByIds(ids);
            log.info("删除菜品结果：{}", result);
            return result;
        } catch (Exception e) {
            log.error("删除菜品异常，ID列表：{}", ids, e);
            throw new CustomException("删除菜品失败：" + e.getMessage());
        }
    }

    /**
     * 更新菜品状态
     * @param status 状态
     * @param ids 菜品ID列表
     * @return 操作结果
     */
    @Override
    public boolean updateStatus(Integer status, List<Long> ids) {
        // 批量更新状态
        List<Dish> dishes = ids.stream().map(id -> {
            Dish dish = new Dish();
            dish.setId(id);
            dish.setStatus(status);
            return dish;
        }).collect(Collectors.toList());
        
        return updateBatchById(dishes);
    }

    /**
     * 上传菜品图片
     * @param file 图片文件
     * @return 图片URL
     */
    @Override
    public String uploadImage(MultipartFile file) {
        try {
            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 使用UUID重新生成文件名
            String fileName = UUID.randomUUID().toString() + suffix;

            // 目标目录 改为项目根目录/upload/dishes/
            String dirPath = System.getProperty("user.dir") + File.separator + "upload" + File.separator + "dishes" + File.separator;
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // 转存文件
            file.transferTo(new File(dirPath + fileName));

            return "/upload/dishes/" + fileName;
        } catch (IOException e) {
            throw new CustomException("文件上传失败");
        }
    }

    /**
     * 根据分类ID查询菜品列表
     * @param categoryId 分类ID
     * @return 菜品列表
     */
    @Override
    public List<DishDTO> listWithFlavor(Long categoryId) {
        // 查询菜品信息
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(categoryId != null, Dish::getCategoryId, categoryId);
        queryWrapper.eq(Dish::getStatus, 1); // 只查询起售状态的菜品
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        List<Dish> dishes = list(queryWrapper);

        // 转换为DTO
        List<DishDTO> dishDTOList = dishes.stream().map(dish -> {
            DishDTO dishDTO = new DishDTO();
            BeanUtils.copyProperties(dish, dishDTO);
            
            // 查询分类名称
            Category category = categoryService.getById(dish.getCategoryId());
            if (category != null) {
                dishDTO.setCategoryName(category.getName());
            }
            
            return dishDTO;
        }).collect(Collectors.toList());

        return dishDTOList;
    }
} 