package com.fnusale.product.service.impl;

import com.fnusale.common.dto.product.ProductCategoryDTO;
import com.fnusale.common.entity.ProductCategory;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.product.ProductCategoryVO;
import com.fnusale.product.mapper.ProductCategoryMapper;
import com.fnusale.product.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品品类服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryMapper productCategoryMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public List<ProductCategoryVO> getTree() {
        // 查询所有启用的品类
        List<ProductCategory> allCategories = productCategoryMapper.selectAllEnabled();

        // 构建品类树
        return buildCategoryTree(allCategories);
    }

    @Override
    public List<ProductCategoryVO> getList() {
        List<ProductCategory> categories = productCategoryMapper.selectTopCategories();
        return categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductCategoryVO> getChildren(Long parentId) {
        if (parentId == null) {
            throw new BusinessException("父品类ID不能为空");
        }

        List<ProductCategory> children = productCategoryMapper.selectByParentId(parentId);
        return children.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductCategoryVO getById(Long id) {
        if (id == null) {
            throw new BusinessException("品类ID不能为空");
        }

        ProductCategory category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("品类不存在");
        }

        return convertToVO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(ProductCategoryDTO dto) {
        // 校验品类名称
        if (dto.getCategoryName() == null || dto.getCategoryName().trim().isEmpty()) {
            throw new BusinessException("品类名称不能为空");
        }

        // 检查品类名称是否已存在
        if (productCategoryMapper.countByName(dto.getCategoryName()) > 0) {
            throw new BusinessException("品类名称已存在");
        }

        // 校验父品类
        if (dto.getParentCategoryId() != null && dto.getParentCategoryId() > 0) {
            ProductCategory parent = productCategoryMapper.selectById(dto.getParentCategoryId());
            if (parent == null) {
                throw new BusinessException("父品类不存在");
            }
        }

        ProductCategory category = new ProductCategory();
        BeanUtils.copyProperties(dto, category);
        category.setEnableStatus(dto.getEnableStatus() != null ? dto.getEnableStatus() : 1);

        productCategoryMapper.insert(category);

        // 清除缓存
        clearCategoryCache();

        log.info("品类新增成功，categoryId: {}, name: {}", category.getId(), category.getCategoryName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ProductCategoryDTO dto) {
        ProductCategory category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("品类不存在");
        }

        // 校验品类名称
        if (dto.getCategoryName() != null && !dto.getCategoryName().trim().isEmpty()) {
            if (!dto.getCategoryName().equals(category.getCategoryName())) {
                if (productCategoryMapper.countByName(dto.getCategoryName()) > 0) {
                    throw new BusinessException("品类名称已存在");
                }
                category.setCategoryName(dto.getCategoryName());
            }
        }

        // 校验父品类（不能设置自己为父品类，也不能设置子品类为父品类）
        if (dto.getParentCategoryId() != null) {
            if (dto.getParentCategoryId().equals(id)) {
                throw new BusinessException("不能设置自己为父品类");
            }
            if (dto.getParentCategoryId() > 0) {
                ProductCategory parent = productCategoryMapper.selectById(dto.getParentCategoryId());
                if (parent == null) {
                    throw new BusinessException("父品类不存在");
                }
            }
            category.setParentCategoryId(dto.getParentCategoryId() > 0 ? dto.getParentCategoryId() : null);
        }

        if (dto.getAiMappingValue() != null) {
            category.setAiMappingValue(dto.getAiMappingValue());
        }
        if (dto.getEnableStatus() != null) {
            category.setEnableStatus(dto.getEnableStatus());
        }

        productCategoryMapper.updateById(category);

        // 清除缓存
        clearCategoryCache();

        log.info("品类更新成功，categoryId: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ProductCategory category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("品类不存在");
        }

        // 检查是否有子品类
        if (productCategoryMapper.countChildrenById(id) > 0) {
            throw new BusinessException("该品类下有子品类，不能删除");
        }

        // 检查是否有商品
        if (productCategoryMapper.countProductsByCategoryId(id) > 0) {
            throw new BusinessException("该品类下有商品，不能删除");
        }

        productCategoryMapper.deleteById(id);

        // 清除缓存
        clearCategoryCache();

        log.info("品类删除成功，categoryId: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        ProductCategory category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("品类不存在");
        }

        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态参数不正确");
        }

        category.setEnableStatus(status);
        productCategoryMapper.updateById(category);

        // 清除缓存
        clearCategoryCache();

        log.info("品类状态更新成功，categoryId: {}, status: {}", id, status);
    }

    @Override
    public List<ProductCategoryVO> getHotCategories() {
        // TODO: 根据商品数量统计热门品类
        // 暂时返回所有一级品类
        return getList();
    }

    // ==================== 私有方法 ====================

    private List<ProductCategoryVO> buildCategoryTree(List<ProductCategory> allCategories) {
        // 按父ID分组
        Map<Long, List<ProductCategory>> categoryMap = allCategories.stream()
                .collect(Collectors.groupingBy(c -> c.getParentCategoryId() != null ? c.getParentCategoryId() : 0L));

        // 获取一级品类
        List<ProductCategory> topCategories = categoryMap.getOrDefault(0L, new ArrayList<>());

        // 递归构建树
        return topCategories.stream()
                .map(category -> convertToVOWithChildren(category, categoryMap))
                .collect(Collectors.toList());
    }

    private ProductCategoryVO convertToVOWithChildren(ProductCategory category, Map<Long, List<ProductCategory>> categoryMap) {
        ProductCategoryVO vo = convertToVO(category);

        List<ProductCategory> children = categoryMap.getOrDefault(category.getId(), new ArrayList<>());
        if (!children.isEmpty()) {
            List<ProductCategoryVO> childVOs = children.stream()
                    .map(child -> convertToVOWithChildren(child, categoryMap))
                    .collect(Collectors.toList());
            vo.setChildren(childVOs);
        }

        return vo;
    }

    private ProductCategoryVO convertToVO(ProductCategory category) {
        ProductCategoryVO vo = new ProductCategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }

    private void clearCategoryCache() {
        redisTemplate.delete("product:category:tree");
        redisTemplate.delete("product:category:list");
        redisTemplate.delete("product:category:hot");
    }
}