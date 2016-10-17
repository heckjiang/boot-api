package com.alibaba.crp.bootapi.service;

import static org.assertj.core.api.Assertions.*;

import com.alibaba.crp.bootapi.domain.Account;
import com.alibaba.crp.bootapi.domain.Book;
import com.alibaba.crp.bootapi.domain.Message;
import com.alibaba.crp.bootapi.repository.BookDao;
import com.alibaba.crp.bootapi.repository.MessageDao;
import com.alibaba.crp.bootapi.util.LogbackListAppender;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class BookBorrowServiceTest {

	private BookBorrowService service;

	private BookDao mockBookDao;

	private MessageDao mockMessageDao;

	private LogbackListAppender appender;

	@Before
	public void setup() {
		service = new BookBorrowService();
		mockBookDao = Mockito.mock(BookDao.class);
		mockMessageDao = Mockito.mock(MessageDao.class);
		service.bookDao = mockBookDao;
		service.messageDao = mockMessageDao;

		appender = new LogbackListAppender();
		appender.addToLogger(BookBorrowService.class);
	}

	public void tearDown() {
		appender.removeFromLogger(BookBorrowService.class);
	}

	@Test
	public void applyBorrowRequest() {

		Book book = new Book(1L);
		book.status = Book.STATUS_IDLE;
		book.owner = new Account(1L);

		Mockito.when(mockBookDao.findOne(1L)).thenReturn(book);

		service.applyBorrowRequest(1L, new Account(3L));

		Mockito.verify(mockBookDao).save(Mockito.any(Book.class));
		Mockito.verify(mockMessageDao).save(Mockito.any(Message.class));
	}

	@Test
	public void applyBorrowRequestWithError() {

		// 自己借自己的书
		Book book = new Book(1L);
		book.status = Book.STATUS_IDLE;
		book.owner = new Account(1L);

		Mockito.when(mockBookDao.findOne(1L)).thenReturn(book);
		try {
			service.applyBorrowRequest(1L, new Account(1L));
			fail("should fail here");
		} catch (Exception e) {
			assertThat(e).hasMessageContaining("User shouldn't borrower the book which is himeself");
			assertThat(appender.getLastMessage()).contains("user id:1,book id:1");
		}
		// 保证BookDao没被调用
		Mockito.verify(mockBookDao, Mockito.never()).save(Mockito.any(Book.class));

		// 借已借出的书
		book.status = Book.STATUS_REQUEST;

		try {
			service.applyBorrowRequest(1L, new Account(3L));
			fail("should fail here");
		} catch (Exception e) {
			assertThat(e).hasMessageContaining("The book is not idle");
			assertThat(appender.getLastMessage()).contains("user id:3,book id:1,status:request");
		}
		Mockito.verify(mockBookDao, Mockito.never()).save(Mockito.any(Book.class));
	}
}