package org.pockito.xcp.config;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.pockito.xcp.config.domain.XcpParameter;
import org.pockito.xcp.config.domain.XcpParameterRepo;
import org.pockito.xcp.config.domain.XcpParameters;

public class XcpConfigTool {

	private XcpParameterRepo repo;

	public XcpConfigTool(XcpParameterRepo repo) {
		super();
		this.repo = repo;
	}

	public void exportConfig(final String repository, final String username, final String password,
			final String[] namespaces, final OutputStream output) throws JAXBException {

		repo.createSharedCmd(repository, username, password).withoutTransaction();
		List<XcpParameter> prmList = repo.findByNamespaces(namespaces);

		XcpParameters params = new XcpParameters(prmList);
		params.normalizeProperties();
		JAXBContext jaxbContext = JAXBContext.newInstance(XcpParameters.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(params, output);
	}

	public Collection<XcpParameter> importConfig(final String repository, final String username, final String password,
			final InputStream input, final boolean createIfNotExists) throws JAXBException {

		JAXBContext jaxbContext = JAXBContext.newInstance(XcpParameters.class);
		Unmarshaller unmarchaller = jaxbContext.createUnmarshaller();
		XcpParameters localParams = (XcpParameters) unmarchaller.unmarshal(input);
		
		ArrayList<XcpParameter> remotePrmList = new ArrayList<XcpParameter>();

		repo.createSharedCmd(repository, username, password).withinTransaction();

		try {
			for (XcpParameter localPrm : localParams.getParameters()) {
				localPrm.denormalizeProperty();
				XcpParameter remotePrm = repo.findByName(localPrm.getNamespace(), localPrm.getConfigName());
				if (remotePrm == null) {
					if (createIfNotExists) {
						// the parameter is new. Create it (if asked)
						remotePrmList.add(localPrm);
					}
				} else {
					remotePrm.setPropertyName(localPrm.getPropertyName());
					remotePrm.setPropertyValue(localPrm.getPropertyValue());
					remotePrmList.add(remotePrm);
				}
			}
			repo.update(remotePrmList);
			repo.commitSharedCmd();
		} catch (Exception e) {
			repo.rollbackSharedCmd();
			throw e;
		}
		
		return remotePrmList;
	}
}
