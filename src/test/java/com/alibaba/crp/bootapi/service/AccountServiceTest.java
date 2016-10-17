package com.alibaba.crp.bootapi.service;

import org.junit.Test;

public class AccountServiceTest {

	@Test
	public void hash() throws Exception {
		System.out.println("hashPassword:" + AccountService.hashPassword("springside"));
	}

}
