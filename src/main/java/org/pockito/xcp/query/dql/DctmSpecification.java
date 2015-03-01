package org.pockito.xcp.query.dql;

import org.pockito.xcp.query.DqlCountSpecification;
import org.pockito.xcp.query.DqlSpecification;
import org.pockito.xcp.query.FolderPath;
import org.pockito.xcp.query.FolderSpecification;
import org.pockito.xcp.query.IdSpecification;
import org.pockito.xcp.repository.Relation;
import org.pockito.xcp.repository.SystemId;

public final class DctmSpecification {

	private DctmSpecification() {
		
	}
	
	public static <T> DqlSpecification<T> dql(final Class<T> entity, final String whereClause) {
		return null;
	}
	
	public static <T> DqlCountSpecification<T> dqlCount(final Class<T> entity, final String whereClause) {
		return null;
	}
	
	public static <T> FolderSpecification folder(final FolderPath folderPath) {
		return null;
	}
	
	public static <T> FolderSpecification folder(final SystemId folderId) {
		return null;
	}

	public static <T> IdSpecification id(final SystemId folderId) {
		return null;
	}

    public static <P, C> Relation<P, C> relation(final Class<P> parent, final Class<C> child, final String name) {
		return null;
	}

    public static <P, C> Relation<P, C> relation(final Class<P> entity, final String relationName) {
		return null;
	}
}
