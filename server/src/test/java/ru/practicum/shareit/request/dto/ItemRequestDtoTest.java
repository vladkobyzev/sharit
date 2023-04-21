package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import javax.validation.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JsonTest
public class ItemRequestDtoTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    public void serializeJson() throws Exception {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(1L);
        itemRequestDto.setDescription("test description");
        itemRequestDto.setCreated(LocalDateTime.now());
        itemRequestDto.setItems(new ArrayList<>());

        String actualJson = this.json.write(itemRequestDto).getJson();

        String jsonContent = "{\"id\":1,\"description\":\"test description\",\"created\":\"" +
                itemRequestDto.getCreated() + "\",\"items\":[]}";

        assertThat(actualJson).isEqualTo(jsonContent);
    }

    @Test
    public void testDeserialize() throws Exception {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(1L);
        itemRequestDto.setDescription("test description");
        itemRequestDto.setCreated(LocalDateTime.parse("2023-04-03T13:15:30"));
        itemRequestDto.setItems(new ArrayList<>());
        String content = "{\"id\":1,\"description\":\"test description\",\"created\":\"2023-04-03T13:15:30\",\"items\":[]}";

        assertThat(this.json.parse(content))
                .isEqualTo(itemRequestDto);
        assertThat(this.json.parseObject(content).getDescription())
                .isEqualTo("test description");
    }

    @Test
    public void shouldThrowExceptionWhenDescriptionIsNull() {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription(null);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<ItemRequestDto>> constraintViolations = validator.validate(itemRequestDto);


        assertEquals(1, constraintViolations.size());
    }
}
