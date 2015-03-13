package org.pockito.xcp.repository;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pockito.xcp.annotations.XcpTypeCategory;
import org.pockito.xcp.entitymanager.api.MetaData;
import org.pockito.xcp.entitymanager.api.PersistentProperty;
import org.pockito.xcp.entitymanager.api.Transaction;
import org.pockito.xcp.repository.test.domain.Document;
import org.pockito.xcp.repository.test.domain.WfEmailTemplate;

@RunWith(MockitoJUnitRunner.class)
public class XcpRepositoryImplTest extends BaseMockedTest {

	@Mock Transaction txMock;
	@Mock MetaData relationMetaDataMock;
	@Mock PersistentProperty parentProp;
	@Mock PersistentProperty childProp;
	
	@Test
	public void testCreateRelation() throws Exception {
		
		// some stub
		when(em.getMetaData(WfEmailTemplate.class)).thenReturn(relationMetaDataMock);
		when(em.getTransaction()).thenReturn(txMock);
		when(relationMetaDataMock.getTypeCategory()).thenReturn(XcpTypeCategory.RELATION);
		when(relationMetaDataMock.getParentMethod()).thenReturn(parentProp);
		when(relationMetaDataMock.getChildMethod()).thenReturn(childProp);
		
		XcpRepository repo = XcpRepositoryFactory.getInstance().create();
		
		Document parent = new Document();
		parent.setName("parent");
		Document child = new Document();
		child.setName("child");
		
		repo.withinTransaction()
		    .create(parent)
		    .create(child)
		    .link(parent).to(child).with(WfEmailTemplate.class)
		    .commit();
		
		verify(parentProp).setProperty(any(WfEmailTemplate.class), eq(parent));
		verify(childProp).setProperty(any(WfEmailTemplate.class), eq(child));
		
		InOrder order = inOrder(txMock, em);
		order.verify(txMock).begin();
		order.verify(em).persist(parent);
		order.verify(em).persist(child);
		order.verify(em).persist(any(WfEmailTemplate.class));
		order.verify(txMock).commit();;
	}

}
