package com.mycompany.myproject.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ProductVariant {

    @ValueMapValue
    private String variantName;

    @ValueMapValue
    private String price;

    @ValueMapValue
    private String colors;

    @ValueMapValue
    private String features;

    public String getVariantName() {
        return variantName;
    }

    public String getPrice() {
        return price;
    }

    public String getColors() {
        return colors;
    }

    public String getFeatures() {
        return features;
    }
}
