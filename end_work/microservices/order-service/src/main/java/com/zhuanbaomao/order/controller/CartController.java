package com.zhuanbaomao.order.controller;

import com.zhuanbaomao.common.Result;
import com.zhuanbaomao.dto.CartItemDTO;
import com.zhuanbaomao.security.CurrentUser;
import com.zhuanbaomao.order.service.CartService;
import com.zhuanbaomao.vo.CartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 购物车控制器
 */
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    public Result<?> add(@CurrentUser Long userId, @Valid @RequestBody CartItemDTO dto) {
        return cartService.addItem(userId, dto);
    }

    /**
     * 查看购物车
     */
    @GetMapping("/list")
    public Result<CartVO> list(@CurrentUser Long userId) {
        return cartService.list(userId);
    }

    /**
     * 更新商品数量
     */
    @PutMapping("/quantity")
    public Result<?> updateQuantity(@CurrentUser Long userId, @RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        Integer quantity = Integer.valueOf(body.get("quantity").toString());
        return cartService.updateQuantity(userId, productId, quantity);
    }

    /**
     * 更新商品选中状态
     */
    @PutMapping("/selected")
    public Result<?> updateSelected(@CurrentUser Long userId, @RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        Integer selected = Integer.valueOf(body.get("selected").toString());
        return cartService.updateSelected(userId, productId, selected);
    }

    /**
     * 从购物车移除商品
     */
    @DeleteMapping("/{productId}")
    public Result<?> remove(@CurrentUser Long userId, @PathVariable Long productId) {
        return cartService.removeItem(userId, productId);
    }
}
