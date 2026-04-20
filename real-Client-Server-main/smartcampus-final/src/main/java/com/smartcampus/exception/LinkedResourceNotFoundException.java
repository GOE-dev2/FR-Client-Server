package com.smartcampus.exception;

/**
 * PART 5.2 – Thrown when a POST body references a resource ID that does not exist.
 *
 * Example: POST /sensors with a roomId that has never been created.
 * The JSON is syntactically valid but semantically unprocessable.
 *
 * Mapped to HTTP 422 Unprocessable Entity by LinkedResourceNotFoundExceptionMapper.
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final String referencedId;

    public LinkedResourceNotFoundException(String resourceType, String referencedId) {
        super("Referenced " + resourceType + " with ID '" + referencedId
              + "' does not exist in the system.");
        this.resourceType = resourceType;
        this.referencedId = referencedId;
    }

    public String getResourceType() { return resourceType; }
    public String getReferencedId() { return referencedId; }
}
