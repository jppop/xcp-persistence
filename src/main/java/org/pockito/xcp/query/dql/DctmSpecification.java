package org.pockito.xcp.query.dql;

import org.pockito.xcp.query.DqlCountSpecification;
import org.pockito.xcp.query.DqlSpecification;
import org.pockito.xcp.query.FolderPath;
import org.pockito.xcp.query.FolderSpecification;
import org.pockito.xcp.query.IdSpecification;
import org.pockito.xcp.repository.Relation;
import org.pockito.xcp.repository.SystemId;

public class DctmSpecification {

	public static <T> DqlSpecification<T> dql(Class<T> entity, String whereClause) {
		return null;
	}
	
	public static <T> DqlCountSpecification<T> dqlCount(Class<T> entity, String whereClause) {
		return null;
	}
	
	public static <T> FolderSpecification folder(FolderPath folderPath) {
		return null;
	}
	
	public static <T> FolderSpecification folder(SystemId folderId) {
		return null;
	}

	public static <T> IdSpecification id(SystemId folderId) {
		return null;
	}

    public static <P, C> Relation<P, C> relation(Class<P> parent, Class<C> Child, String name) {
		return null;
	}

    public static <P, C> Relation<P, C> relation(Class<P> entity, String relationName) {
		return null;
	}
}
