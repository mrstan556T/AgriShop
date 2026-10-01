package com.agrishop.web.converter;

import com.agrishop.dto.ProductDTO;
import com.agrishop.service.ProductServiceLocal;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;

@Named
@RequestScoped
@FacesConverter(value = "productConverter", managed = true)
public class ProductConverter implements Converter<ProductDTO> {

    @Inject
    private ProductServiceLocal productService;

    @Override
    public ProductDTO getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        Long id = null;
        try {
            id = Long.valueOf(value.trim());
        } catch (NumberFormatException ignored) {}

        List<ProductDTO> all = productService.getAllActiveProducts();
        for (ProductDTO dto : all) {
            if (id != null && dto.getId().equals(id)) {
                return dto;
            }
            if (dto.getName() != null && dto.getName().trim().equalsIgnoreCase(value.trim())) {
                return dto;
            }
            if (dto.getProductCode() != null && dto.getProductCode().trim().equalsIgnoreCase(value.trim())) {
                return dto;
            }
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, ProductDTO value) {
        if (value == null || value.getId() == null) {
            return "";
        }
        return String.valueOf(value.getId());
    }
}
