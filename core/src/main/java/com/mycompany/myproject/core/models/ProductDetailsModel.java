package com.mycompany.myproject.core.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

@Model(adaptables = Resource.class)
public class ProductDetailsModel {

    @SlingObject
    private Resource currentResource;

    private List<ProductVariant> variants;

    @PostConstruct
    protected void init() {
        variants = new ArrayList<>();
        Resource variantsResource = currentResource.getChild("variants");
        if (variantsResource != null) {
            for (Resource variantItem : variantsResource.getChildren()) {
                ProductVariant variant = variantItem.adaptTo(ProductVariant.class);
                if (variant != null) {
                    variants.add(variant);
                }
            }
        }
    }

    public List<ProductVariant> getVariants() {
        return Collections.unmodifiableList(variants);
    }
}
