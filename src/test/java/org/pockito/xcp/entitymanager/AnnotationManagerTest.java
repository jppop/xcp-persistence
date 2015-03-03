package org.pockito.xcp.entitymanager;

import static org.junit.Assert.*;

import org.junit.Test;
import org.pockito.xcp.test.domain.Task;

public class AnnotationManagerTest {

	// TODO: add more tests (covering parsing & caching)
	
	@Test
	public void basicParsing() {
		
		// get an annotation manager
		AnnotationManager aiMgr = new AnnotationManager();
		
		AnnotationInfo ai;
		
		ai = aiMgr.addAnnotationInfo(Task.class);
		
		assertNotNull(ai);
		assertEquals(8, ai.getPersistentProperties().size());
		assertEquals("todo_task", ai.getDmsType());
		assertEquals("id", ai.getIdMethod().getFieldName());
		assertEquals("subject", ai.getPersistentProperty("what").getAttributeName());
		assertEquals("priority", ai.getPersistentProperty("priority").getAttributeName());
		assertEquals("r_creation_date", ai.getPersistentProperty("creationDate").getAttributeName());
		assertEquals("i_vstamp", ai.getPersistentProperty("vStamp").getAttributeName());
		assertEquals("due_date", ai.getPersistentProperty("dueDate").getAttributeName());
		assertEquals("i_has_folder", ai.getPersistentProperty("hasFolder").getAttributeName());
	}

}
