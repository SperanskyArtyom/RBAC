package io.github.speranskyartyom.rbac.models.records;

import io.github.speranskyartyom.rbac.utils.ValidationUtils;

public record Permission(String name, String resource, String description) {

    public Permission(
            String name,
            String resource,
            String description) {
        validate(name, resource, description);
        this.name = ValidationUtils.normalizeString(name, true);
        this.resource = ValidationUtils.normalizeString(resource);
        this.description = description;
    }

    private static void validate(String name, String resource, String description) {
        ValidationUtils.requireNonEmpty(name, "name");
        ValidationUtils.requireNonEmpty(resource, "resource");
        ValidationUtils.requireNonEmpty(description, "description");

        if (name.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException(
                    "name must not contain spaces. Given: " + name);
        }
    }

    public String format() {
        return String.format(
                "%s on %s: %s",
                name, resource, description);
    }

    public boolean matches(String namePattern, String resourcePattern) {
        boolean nameMatch = namePattern == null || name.matches(namePattern);
        boolean resourceMatch = resourcePattern == null || resource.matches(resourcePattern);

        return nameMatch && resourceMatch;
    }
}
