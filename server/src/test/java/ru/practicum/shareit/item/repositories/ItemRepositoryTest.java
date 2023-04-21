package ru.practicum.shareit.item.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import ru.practicum.shareit.item.model.Item;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    public void testSearchItemByText_shouldReturnSuccessResult() {
        Item item1 = new Item();
        item1.setName("iPhone");
        item1.setDescription("Apple. This is an iPhone.");
        item1.setAvailable(true);
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("MacBook Pro");
        item2.setDescription("Apple. This is a MacBook Pro.");
        item2.setAvailable(true);
        itemRepository.save(item2);

        Item item3 = new Item();
        item3.setName("iPad");
        item3.setDescription("Apple. This is an iPad.");
        item3.setAvailable(true);
        itemRepository.save(item3);

        List<Item> foundItems = itemRepository.searchItemByText("iPhone");
        assertEquals(1, foundItems.size());
        assertTrue(foundItems.contains(item1));

        foundItems = itemRepository.searchItemByText("pro");
        assertEquals(1, foundItems.size());
        assertTrue(foundItems.contains(item2));

        foundItems = itemRepository.searchItemByText("apple");
        assertEquals(3, foundItems.size());
        assertTrue(foundItems.contains(item1));
        assertTrue(foundItems.contains(item3));
    }

    @Test
    public void testSearchItemByText_shouldReturnNegativeResult() {
        Item item1 = new Item();
        item1.setName("iPhone");
        item1.setDescription("Apple. This is an iPhone.");
        item1.setAvailable(false);
        itemRepository.save(item1);

        List<Item> foundItems = itemRepository.searchItemByText("iPhone");
        assertEquals(0, foundItems.size());
    }

    @Test
    public void testFindAllByOwner() {
        long ownerId = 1L;
        Item item1 = new Item();
        item1.setName("item1");
        item1.setDescription("description1");
        item1.setOwner(ownerId);
        Item item2 = new Item();
        item2.setName(("item2"));
        item2.setDescription("description2");
        item2.setOwner(ownerId);
        Item item3 = new Item();
        item3.setName(("item3"));
        item3.setDescription("description3");
        item3.setOwner(2L);

        itemRepository.saveAll(Arrays.asList(item1, item2, item3));

        List<Item> items = itemRepository.findAllByOwner(ownerId);

        assertNotNull(items);
        assertEquals(items.size(), 2);
    }

    @Test
    public void testFindAllByOwner_withOneItem() {
        long ownerId = 1L;
        Item item = new Item();
        item.setOwner(ownerId);
        item.setName("Test item");
        item.setDescription("Test item description");
        itemRepository.save(item);

        Slice<Item> items = itemRepository.findAllByOwner(ownerId, PageRequest.of(0, 10));

        // then
        assertEquals(items.getNumberOfElements(), 1);
        assertEquals(items.getContent().get(0).getName(), item.getName());
    }

    @Test
    public void testFindAllByOwner_withDifferentOwner() {
        long ownerId = 1L;
        Item item = new Item();
        item.setOwner(ownerId);
        item.setName("Test item");
        item.setDescription("Test item description");
        itemRepository.save(item);

        Slice<Item> items = itemRepository.findAllByOwner(ownerId + 1, PageRequest.of(0, 10));

        assertTrue(items.isEmpty());
    }
}