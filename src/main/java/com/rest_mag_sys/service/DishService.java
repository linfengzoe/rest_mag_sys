package com.rest_mag_sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rest_mag_sys.dto.DishDTO;
import com.rest_mag_sys.dto.PageQueryDTO;
import com.rest_mag_sys.entity.Dish;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 菜品服务接口
 */
public interface DishService extends IService<Dish> {

    /**
     * 新增菜品，同时插入菜品对应的口味数据
     * @param dishDTO 菜品数据传输对象
     * @return 是否成功
     */
    boolean saveWithFlavor(DishDTO dishDTO);

    /**
     * 分页查询
     * @param pageQueryDTO 分页查询参数
     * @return 分页结果
     */
    Page<DishDTO> pageQuery(PageQueryDTO pageQueryDTO);

    /**
     * 根据ID查询菜品信息和对应的口味信息
     * @param id 菜品ID
     * @return 菜品数据传输对象
     */
    DishDTO getByIdWithFlavor(Long id);

    /**
     * 更新菜品信息，同时更新对应的口味信息
     * @param dishDTO 菜品数据传输对象
     * @return 是否成功
     */
    boolean updateWithFlavor(DishDTO dishDTO);

    /**
     * 删除菜品，同时删除菜品对应的口味数据
     * @param ids 菜品ID列表
     * @return 是否成功
     */
    boolean removeWithFlavor(List<Long> ids);

    /**
     * 根据分类ID查询菜品列表
     * @param categoryId 分类ID
     * @return 菜品列表
     */
    List<DishDTO> listWithFlavor(Long categoryId);

    /**
     * 更新菜品状态
     * @param status 状态
     * @param ids 菜品ID列表
     * @return 操作结果
     */
    boolean updateStatus(Integer status, List<Long> ids);

    /**
     * 上传菜品图片
     * @param file 图片文件
     * @return 图片URL
     */
    String uploadImage(MultipartFile file);
} 