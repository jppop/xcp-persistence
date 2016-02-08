# xcp-persistence
Documentum xCP 2.1 Persistence using Repository Design pattern.

## Overview

This project aims to provide developers an easy way to create, read, update and delete xCP objects in Java.
As Java Persistence Api (JPA), xcp-persitences let's you declare entities :  
```Java
@XcpEntity(namespace = "todo")
@XcpType(type = XcpTypeCategory.BUSINESS_OBJECT, name = "person")
public class Person extends PersistedObject {

	@Attribute(name = "first_name")
	private String firstName;

	@Attribute(name = "last_name")
	private String lastName;

	@Attribute(name = "object_name")
	private String name;

  // omitted for clarity
}

```
The XcpEntityManager let's you then to persist entities:
```Java
XcpEntityManagerFactory dmsEmFactory = new XcpEntityManagerFactory();
...
em = (XcpEntityManager) dmsEmFactory.createDmsEntityManager(props);

Person person = em.find(Person.class, personId);
person.setEMail("jdoe@company.com");
em.persist(person);
```

xcp-persistence provides also an implementation of the Repository Design Pattern (something like Spring Data).

## Motivation
Documentum xCP can be seen as a simplification of the Documentum data model. xCP is focused on Busines Objects: no more ``dm_sysobject`` neither ``dm_document`` but Person, Address objects.
Documentum xCP handles 3 kinds of objects: Business Object, Content Objects and Folders. And only on type of relation: relations implemented as the old join table, aka ``dm_relation``object.
In other words, xCP eases the repository access by hidding the technical implementation.
In the same way, Java developers need a simplification of the API to access the repository objets (the DFC). xCP provides, out the box, a REST API. In many situations, the xCP REST API can be used but one important feature is missing: transactional operations. There is no way to create complex objets in on single transaction.

Here we are. The main goal of this project is to provide an easy API to an xCP repository supporting transactions.

## Status
Still BETA but going to production in few weeks.

# xCP Entity Manager

xCP Entity Manager handles all the operations to access the xCP repository. Mainly, the Manager converts Entities (objects of classes annotated with ``@XcpEntity``) to xCP objects.

> Notice! xCP Entity Manager is **not** an entity manager like JPA ones. JPA Entity managers can handle complex relational data model. This entity manager handles the very simple xCP data model.

# Repository Pattern

TODO

# Todo
- [ ] Persist entity classes only when they have been modified (enhance entity classes with cglib to handle 'dirty' ?)
