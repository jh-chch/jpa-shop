package com.jpabook.jpashop.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.jpabook.jpashop.domain.item.Book;
import com.jpabook.jpashop.domain.item.Item;
import com.jpabook.jpashop.service.ItemService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items/new")
    public String createForm(Model model) {
        model.addAttribute("form", new Book());
        return "items/createItemForm";
    }

    @PostMapping("/items/new")
    public String create(BookForm bookForm) {
        Book book = new Book();
        book.setName(bookForm.getName());
        book.setPrice(bookForm.getPrice());
        book.setStockQuantity(bookForm.getStockQuantity());
        book.setAuthor(bookForm.getAuthor());
        book.setIsbn(bookForm.getIsbn());

        itemService.saveItem(book);

        return "redirect:/";
    }

    @GetMapping("/items")
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }

    @GetMapping("/items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {
        Book item = (Book) itemService.findOne(itemId);

        BookForm bookForm = new BookForm();
        bookForm.setId(item.getId());
        bookForm.setName(item.getName());
        bookForm.setPrice(item.getPrice());
        bookForm.setStockQuantity(item.getStockQuantity());
        bookForm.setAuthor(item.getAuthor());
        bookForm.setIsbn(item.getIsbn());

        model.addAttribute("form", bookForm);
        return "items/updateItemForm";
    }

    @PostMapping("/items/{itemId}/edit")
    public String updateItem(@ModelAttribute BookForm bookForm) {
        /**
         * 엔티티를 변경할 때는 변경 감지를 사용하자.
         * - 컨트롤러에서 어설프게 엔티티를 생성하지 말자.
         * - 트랜잭션이 있는 서비스 계층에서 식별자(id)와 변경할 데이터를 명확하게 전달하자 (파라미터 또는 DTO)
         * - 트랜잭션이 있는 서비스 계층에서 영속 상태의 엔티티를 조회하고, 엔티티의 데이터를 직접 변경하자.
         */
        // Book book = new Book();
        // book.setId(bookForm.getId());
        // book.setName(bookForm.getName());
        // book.setPrice(bookForm.getPrice());
        // book.setStockQuantity(bookForm.getStockQuantity());
        // book.setAuthor(bookForm.getAuthor());
        // book.setIsbn(bookForm.getIsbn());
        // itemService.saveItem(book);

        // 서비스 계층에서 식별자와 파라미터를 사용해서 update (변경감지)
        itemService.updateItem(bookForm.getId(), bookForm.getName(), bookForm.getPrice(), bookForm.getStockQuantity());

        return "redirect:/items";
    }

}
