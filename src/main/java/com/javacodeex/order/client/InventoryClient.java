package com.javacodeex.order.client;

import com.javacodeex.order.dto.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "inventory-service", url = "${inventory.service.url}") // http://localhost:8082
public interface InventoryClient {

    @GetMapping("/api/inventory/{productCode}")
    InventoryResponse checkStock(
            @PathVariable("productCode") String productCode,
            @RequestParam("quantity") Integer quantity
    );

}
