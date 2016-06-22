package de.epiceric.shopchest.language;

import org.bukkit.entity.EntityType;

public class EntityName {

    private String localizedName;
    private EntityType entityType;

    public EntityName(EntityType entityType, String localizedName) {
        this.entityType = entityType;
        this.localizedName = localizedName;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public String getLocalizedName() {
        return localizedName;
    }
}
