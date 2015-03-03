package org.pockito.xcp.entitymanager;

import org.junit.Test;

public class JPAQueryParserTest {

	@Test
	public void test() {
        AbstractJPQLQuery query = new AbstractJPQLQuery();
        JPAQueryParser parser;

        parser = new JPAQueryParser(query, ("select o from MyTestObject o where o.myTestObject2.id = :id2 and 1=1 OR o.myTestObject2.name = 'larry'"));
        parser.parse();
 	}

}
