package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
public class UserDtoTest {
    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    public void testSerializeDto() throws Exception {
        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setName("Test User");
        dto.setEmail("test@test.com");

        String jsonOutput = json.write(dto).getJson();
        String expectedJson = "{\"id\":1,\"name\":\"Test User\",\"email\":\"test@test.com\"}";

        assertThat(jsonOutput).isEqualTo(expectedJson);
    }

    @Test
    public void testDeserializeDto() throws Exception {
        String jsonInput = "{\"id\":1,\"name\":\"Test User\",\"email\":\"test@test.com\"}";
        UserDto expectedDto = new UserDto();
        expectedDto.setId(1L);
        expectedDto.setName("Test User");
        expectedDto.setEmail("test@test.com");

        UserDto dto = json.parse(jsonInput).getObject();

        assertThat(dto).isEqualTo(expectedDto);
    }

    @Test
    public void shouldThrowExceptionWhenEmailIsIncorrect() {
        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setName("Test User");
        dto.setEmail("aadccadcad");

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<UserDto>> constraintViolations = validator.validate(dto);


        assertEquals(1, constraintViolations.size());
    }

    @Test
    public void shouldThrowExceptionWhenEmailIsNull() {
        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setName("Test User");
        dto.setEmail(null);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<UserDto>> constraintViolations = validator.validate(dto);


        assertEquals(1, constraintViolations.size());
    }
}
