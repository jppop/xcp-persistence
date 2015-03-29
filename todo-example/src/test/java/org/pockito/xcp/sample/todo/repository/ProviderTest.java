package org.pockito.xcp.sample.todo.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pockito.xcp.sample.todo.domain.Person;
import org.pockito.xcp.sample.todo.domain.Task;
import org.pockito.xcp.sample.todo.repository.config.AppConfig;
import org.pockito.xcp.sample.todo.repository.config.RepoProvider;
import org.pockito.xcp.sample.todo.repository.impl.PersonRepoImpl;

@RunWith(MockitoJUnitRunner.class)
public class ProviderTest {

	@Mock
	PersonRepo mockPersonRepo;
	
	@Mock
	TaskRepo mockTaskRepo;
	
	@InjectMocks
	RepoProvider mockProvider;
	
	@Test
	public void testMockInjection() {
		
		assertNotNull(mockProvider);
		PersonRepo personRepo = (PersonRepo) mockProvider.getRepo(Person.class);
		assertEquals(mockPersonRepo, personRepo);
		
		TaskRepo taskRepo = (TaskRepo) mockProvider.getRepo(Task.class);
		assertEquals(mockTaskRepo, taskRepo);

	}
	
	@Test
	public void testGuiceInjection() {

		PersonRepo personRepo = AppConfig.instance.getPersonRepo();
		assertNotNull(personRepo);
		assertEquals(PersonRepoImpl.class, personRepo.getClass());
		
		
		PersonRepo personRepo2 = AppConfig.instance.getPersonRepo();
		assertNotNull(personRepo2);
		assertEquals(PersonRepoImpl.class, personRepo2.getClass());
		assertEquals(personRepo, personRepo2);
		
	}

}
