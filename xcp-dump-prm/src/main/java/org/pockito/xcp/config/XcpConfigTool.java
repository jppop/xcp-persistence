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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XcpConfigTool {

	private static final Logger logger = LoggerFactory.getLogger(XcpConfigTool.class);
	
	private XcpParameterRepo repo;

	public XcpConfigTool(XcpParameterRepo repo) {
		super();
		this.repo = repo;
	}

	public void exportConfig(final String repository, final String username, final String password,
			final String[] namespaces, final OutputStream output, final String[] typeFilter) throws JAXBException {

		repo.createSharedCmd(repository, username, password).withoutTransaction();
		List<XcpParameter> prmList = repo.findByNamespaces(namespaces, typeFilter);

		XcpParameters params = new XcpParameters(prmList);
		params.normalizeProperties();
		JAXBContext jaxbContext = JAXBContext.newInstance(XcpParameters.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(params, output);
	}

	public interface ProgressListener {
		void progress(String message, String status, XcpParameter param);
	}
	
	private static class ProgressNoop implements ProgressListener {

		@Override
		public void progress(String message, String status, XcpParameter param) {
		}
		
	}

	public Collection<XcpParameter> importConfig(final String repository, final String username, final String password,
			final InputStream input, final boolean createIfNotExists, final Excluder excluder, ProgressListener listener) throws Exception {

		if (listener == null) {
			listener = new ProgressNoop();
		}
		
		JAXBContext jaxbContext = JAXBContext.newInstance(XcpParameters.class);
		Unmarshaller unmarchaller = jaxbContext.createUnmarshaller();
		XcpParameters localParams = (XcpParameters) unmarchaller.unmarshal(input);

		ArrayList<XcpParameter> remotePrmList = new ArrayList<XcpParameter>();

		repo.createSharedCmd(repository, username, password).withinTransaction();

		try {
			for (XcpParameter localPrm : localParams.getParameters()) {
				localPrm.denormalizeProperty();
				XcpParameter remotePrm = repo.findByName(localPrm.getNamespace(), localPrm.getConfigName());
				boolean ignored = false;
				if (excluder != null) {
					ignored = excluder.isTypeExluded(localPrm.getConfigType())
							|| excluder.isNameExluded(localPrm.getConfigName());
				}
				if (ignored) {
					listener.progress("Parameter " + localPrm.toString() + " ignored", "ignored", localPrm);
				} else {
					if (remotePrm == null) {
						if (createIfNotExists) {
							listener.progress("Parameter " + localPrm.toString() + " updated", "updated", localPrm);
							// the parameter is new. Create it (if asked)
							remotePrmList.add(localPrm);
						}
					} else {
						listener.progress("Parameter " + localPrm.toString() + " updated", "updated", localPrm);
						remotePrm.setPropertyName(localPrm.getPropertyName());
						remotePrm.setPropertyValue(localPrm.getPropertyValue());
						remotePrmList.add(remotePrm);
					}
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
