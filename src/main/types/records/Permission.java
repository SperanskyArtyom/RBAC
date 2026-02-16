package types.records;

public record Permission(String name, String resource, String description) {

    public Permission(String name, String resource, String description) {
        this.name = normalizeName(name);
        this.resource = normalizeResource(resource);
        this.description = description;
        validate(this.name, this.resource, this.description);
    }

    private static String normalizeName(String name) {
        return name != null ? name.toUpperCase() : null;
    }

    private static String normalizeResource(String resource) {
        return resource != null ? resource.toLowerCase() : null;
    }

    private static void validate(String name, String resource, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Name must not be null or blank. Given: " + name);
        }

        if (resource == null || resource.isBlank()) {
            throw new IllegalArgumentException(
                    "Resource must not be null or blank. Given: " + resource);
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Description must not be null or blank. Given: " + description);
        }

        if (name.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException(
                    "Name must not contain spaces. Given: " + name);
        }
    }

    public String format() {
        return String.format("%s on %s: %s", name, resource, description);
    }

    public boolean matches(String namePattern, String resourcePattern) {
        boolean nameMatch = namePattern == null || name.matches(namePattern);
        boolean resourceMatch = resourcePattern == null || resource.matches(resourcePattern);

        return nameMatch && resourceMatch;
    }
}
