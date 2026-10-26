package com.store.cosmetic.exception;

import com.store.cosmetic.util.ApiResponse;
import com.store.cosmetic.util.ErrorResponse;
import com.store.cosmetic.util.PageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionAndWrapperTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testApiResponseWrappers() {
        ApiResponse<String> res1 = ApiResponse.ok("data1");
        assertTrue(res1.isSuccess());
        assertEquals("data1", res1.getData());

        ApiResponse<Integer> res2 = ApiResponse.ok("Created successfully", 100);
        assertTrue(res2.isSuccess());
        assertEquals("Created successfully", res2.getMessage());
        assertEquals(100, res2.getData());

        ApiResponse<Void> res3 = ApiResponse.message("Operation done");
        assertTrue(res3.isSuccess());
        assertEquals("Operation done", res3.getMessage());
        assertNull(res3.getData());
    }

    @Test
    void testPageResponse() {
        List<String> items = List.of("item1", "item2");
        PageImpl<String> page = new PageImpl<>(items, PageRequest.of(0, 10), 2);
        PageResponse<String> response = PageResponse.from(page);

        assertEquals(2, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
    }

    @Test
    void testHandleResourceNotFound() {
        ResponseEntity<ErrorResponse> res = handler.handleResourceNotFound(new ResourceNotFoundException("Not found"));
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        assertFalse(res.getBody().isSuccess());
        assertEquals("Not found", res.getBody().getMessage());
    }

    @Test
    void testHandleConflict() {
        ResponseEntity<ErrorResponse> res = handler.handleConflict(new ConflictException("Already exists"));
        assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
        assertFalse(res.getBody().isSuccess());
        assertEquals("Already exists", res.getBody().getMessage());
    }

    @Test
    void testHandleBadRequest() {
        ResponseEntity<ErrorResponse> res = handler.handleBadRequest(new BadRequestException("Invalid data"));
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertFalse(res.getBody().isSuccess());
        assertEquals("Invalid data", res.getBody().getMessage());
    }

    @Test
    void testHandleDataIntegrity() {
        ResponseEntity<ErrorResponse> res = handler.handleDataIntegrity(
                new DataIntegrityViolationException("Constraint violation", new RuntimeException("duplicate key")));
        assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
        assertFalse(res.getBody().isSuccess());
        assertTrue(res.getBody().getMessage().contains("duplicate key"));
    }

    @Test
    void testHandleGenericException() {
        ResponseEntity<ErrorResponse> res = handler.handleGenericException(new RuntimeException("Crash"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());
        assertFalse(res.getBody().isSuccess());
        assertTrue(res.getBody().getMessage().contains("Crash"));
    }

    @Test
    void testHandleValidationErrors() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "email", "must not be blank"));

        Method method = this.getClass().getDeclaredMethod("testApiResponseWrappers");
        MethodParameter parameter = new MethodParameter(method, -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> res = handler.handleValidationErrors(ex);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertFalse(res.getBody().isSuccess());
        assertEquals("Validation failed", res.getBody().getMessage());
        assertEquals("must not be blank", res.getBody().getErrors().get("email"));
    }
}
