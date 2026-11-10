package com.springboot.roadready.config;

import com.springboot.roadready.dto.response.ErrorDto;
import com.springboot.roadready.exception.InvalidCallException;
import com.springboot.roadready.exception.OwnershipException;
import com.springboot.roadready.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDto> handleResourceNotFountException(
            ResourceNotFoundException e
    ){
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(e.getMessage(),
                                "Id not found in DB",
                                Instant.now())
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ){
        Map<String,String> map = new HashMap<>();

        BindingResult bindingResult=e.getBindingResult();
        List<FieldError> list = bindingResult.getFieldErrors();

        list.forEach(fieldError -> map.put(fieldError.getField(), fieldError.getDefaultMessage()));

        return ResponseEntity
                .badRequest()
                .body(
                        map
                );
    }

    @ExceptionHandler(InvalidCallException.class)
    public ResponseEntity<ErrorDto> handleInvalidCallException(
            InvalidCallException e
    ){
        //always reply the object not the string

        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(e.getMessage(),
                                "Call is Invalid due to malformed data",
                                Instant.now())
                );
    }

    @ExceptionHandler(OwnershipException.class)
    public ResponseEntity<ErrorDto> handleOwnershipException(
            OwnershipException e
    ){
        //always reply the object not the string

        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(e.getMessage(),
                                "Logged-in User cannot perform this operation",
                                Instant.now())
                );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorDto> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e
    ){
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto("The image is too large. The maximum size is 10 MB.",
                                "Upload rejected",
                                Instant.now())
                );
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorDto> handleIOException(
            IOException e
    ){
        return ResponseEntity
                .status(500)
                .body(
                        new ErrorDto("The file could not be saved on the server.",
                                "Check the upload folder configured in application.properties",
                                Instant.now())
                );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDto> handleDataIntegrityViolationException(
            DataIntegrityViolationException e
    ){
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto("This record conflicts with existing data (for example a duplicate).",
                                "Data integrity violation",
                                Instant.now())
                );
    }
}
