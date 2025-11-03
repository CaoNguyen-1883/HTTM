package dev.CaoNguyen_1883.ecommerce.common.exception;

import dev.CaoNguyen_1883.ecommerce.common.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/test")
@Validated
public class TestController {

    /**
     * Test 404 - ResourceNotFoundException
     * GET /api/test/not-found/{id}
     */
    @GetMapping("/not-found/{id}")
    public ResponseEntity<ApiResponse<Object>> testNotFound(@PathVariable UUID id) {
        throw new ResourceNotFoundException("Product", "id", id);
    }

    /**
     * Test 400 - BadRequestException
     * GET /api/test/bad-request
     */
    @GetMapping("/bad-request")
    public ResponseEntity<ApiResponse<Object>> testBadRequest() {
        throw new BadRequestException("Invalid request parameters");
    }

    /**
     * Test 401 - UnauthorizedException
     * GET /api/test/unauthorized
     */
    @GetMapping("/unauthorized")
    public ResponseEntity<ApiResponse<Object>> testUnauthorized() {
        throw new UnauthorizedException("You must be logged in to access this resource");
    }

    /**
     * Test 403 - ForbiddenException
     * GET /api/test/forbidden
     */
    @GetMapping("/forbidden")
    public ResponseEntity<ApiResponse<Object>> testForbidden() {
        throw new ForbiddenException("You don't have permission to access this resource");
    }

    /**
     * Test 409 - DuplicateResourceException
     * GET /api/test/duplicate
     */
    @GetMapping("/duplicate")
    public ResponseEntity<ApiResponse<Object>> testDuplicate() {
        throw new DuplicateResourceException("User", "email", "test@example.com");
    }

    /**
     * Test 400 - OutOfStockException
     * GET /api/test/out-of-stock
     */
    @GetMapping("/out-of-stock")
    public ResponseEntity<ApiResponse<Object>> testOutOfStock() {
        throw new OutOfStockException("Gaming Mouse X", 5, 10);
    }

    /**
     * Test 400 - @Valid validation (RequestBody)
     * POST /api/test/validation
     * Body: {"email": "invalid", "name": "", "age": -5}
     */
    @PostMapping("/validation")
    public ResponseEntity<ApiResponse<String>> testValidation(@Valid @RequestBody TestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Validation passed", "Success"));
    }

    /**
     * Test 400 - @Validated validation (RequestParam)
     * GET /api/test/param-validation?email=invalid&age=-5
     */
    @GetMapping("/param-validation")
    public ResponseEntity<ApiResponse<String>> testParamValidation(
            @RequestParam @Email(message = "Email must be valid") String email,
            @RequestParam @Positive(message = "Age must be positive") int age) {
        return ResponseEntity.ok(ApiResponse.success("Validation passed"));
    }

    /**
     * Test 400 - Type mismatch
     * GET /api/test/type-mismatch/abc (expecting UUID)
     */
    @GetMapping("/type-mismatch/{id}")
    public ResponseEntity<ApiResponse<String>> testTypeMismatch(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("ID: " + id));
    }

    /**
     * Test 500 - Internal Server Error
     * GET /api/test/server-error
     */
    @GetMapping("/server-error")
    public ResponseEntity<ApiResponse<Object>> testServerError() {
        // Simulate unexpected error
        throw new RuntimeException("Unexpected error occurred");
    }

    /**
     * Test 200 - Success response
     * GET /api/test/success
     */
    @GetMapping("/success")
    public ResponseEntity<ApiResponse<TestDTO>> testSuccess() {
        TestDTO dto = new TestDTO();
        dto.setEmail("test@example.com");
        dto.setName("John Doe");
        dto.setAge(25);
        return ResponseEntity.ok(ApiResponse.success("Data retrieved successfully", dto));
    }

    /**
     * Test 201 - Created response
     * POST /api/test/create
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<TestDTO>> testCreate(@Valid @RequestBody TestDTO dto) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created("Resource created successfully", dto));
    }

    // DTO for testing validation
    @Data
    public static class TestDTO {

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;

        @NotBlank(message = "Name is required")
        private String name;

        @Positive(message = "Age must be positive")
        private Integer age;
    }
}
