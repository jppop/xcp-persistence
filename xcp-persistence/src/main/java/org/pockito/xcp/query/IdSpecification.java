package org.pockito.xcp.query;

import org.pockito.xcp.repository.SystemId;


public interface IdSpecification extends BasicSpecification {

	void set(SystemId id);
}
