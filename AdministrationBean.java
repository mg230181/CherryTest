package com.systematic.kombit.administration;

import com.systematic.kombit.common.configuration.ServiceDTO;
import com.systematic.kombit.common.exception.KombitException;
import com.systematic.kombit.invoicehandling.client.persistence.dao.SFTPInvoiceSourceDAOLocal;
import com.systematic.kombit.invoicehandling.client.persistence.dto.SFTPInvoiceSourceDTO;
import com.systematic.kombit.oir.configuration.client.OiRConfigurationBeanLocal;
import com.systematic.kombit.oir.configuration.entities.OiRConfigurationEntity;
import com.systematic.kombit.ping.*;
import com.systematic.kombit.serviceconfiguration.client.ServiceConfigurationAdministrationLocal;
import com.systematic.kombit.serviceconfiguration.dto.ServiceDescriptionDTO;
import lombok.extern.log4j.Log4j;

import javax.ejb.*;
import java.util.*;
/**
 * This class implements the bean for the administering the connection and service agreements.
 * It ensures that calls involving several calls take place in one transaction.
 */
@Stateless(mappedName = "ejb/Administration")
@Local(AdministrationLocal.class)
@Remote(AdministrationRemote.class)
@Log4j
@EJBs({
@EJB(name = "java:global/AdministrationBean/local",beanInterface = AdministrationLocal.class),
@EJB(name = "java:jboss/exported/AdministrationBean/remote",beanInterface = AdministrationRemote.class),
@EJB(name = Pingable.JNDI_PREFIX + AdministrationServiceConstants.SERVICE_UUID, beanInterface = AdministrationLocal.class)
})
public class AdministrationBean implements Administration {

	@EJB(mappedName = ServiceConfigurationAdministrationLocal.LOCAL_NAME)
	ServiceConfigurationAdministrationLocal serviceConfigurationAdministration;

	@EJB(mappedName = "java:global/OiRConfigurationBean/local")
	OiRConfigurationBeanLocal oirConfigLocal;

	@EJB(mappedName = "java:global/SFTPInvoiceSourceDAOBean/local")
	SFTPInvoiceSourceDAOLocal sftpInvSourceDAO;

	@EJB(lookup = PingLogLocal.LOCAL_NAME)
	private PingLogLocal pingLog;

	@Override
	public List<Map<String, String>> getServices() throws KombitException {
		final List<Map<String, String>> serviceList = new ArrayList<>();

		List<ServiceDTO> services = serviceConfigurationAdministration.getAllServices();
		if(services == null){
			return Collections.emptyList();
		}
		for (final ServiceDTO s : services) {
			final Map<String,String> service = new HashMap<>();
			service.put("Name", s.getName());
			service.put("UUID", s.getUuid());
			service.put("Description", s.getDescription());
			serviceList.add(service);
		}
		return serviceList;
	}

	@Override
	public String getServiceEntityID(String serviceUUID){
		return serviceConfigurationAdministration.getServiceEntityID(serviceUUID);
	}

	@Override
	public OiRConfigurationEntity getOiRConfiguration(String userUUID){
		return oirConfigLocal.getByUserUuid(userUUID);
	}

	@Override
	public OiRConfigurationEntity saveOiRConfiguration(OiRConfigurationEntity configuration){
		return oirConfigLocal.save(configuration);
	}

	@Override
	public SFTPInvoiceSourceDTO getSftpInvSource(String userUUID) {
		return sftpInvSourceDAO.getByUserUuid(userUUID);
	}

	@Override
	public void saveSftpInvSource(SFTPInvoiceSourceDTO dto) {
		sftpInvSourceDAO.saveSftpInvoiceEntity(dto);
	}

    @Override
    public void editServiceDescription(ServiceDescriptionDTO dto){
        serviceConfigurationAdministration.editServiceDescription(dto);
    }

	@Override
	public PingOutput ping(PingContext ctx){
		PingOutput pingOutput;
		PingOperation operation = new PingOperation("Calling Administration service", "Calling service administration database", 1000);

		try{
			//Look for this service
			serviceConfigurationAdministration.getServiceEntityID(AdministrationServiceConstants.SERVICE_UUID);
			pingOutput = new PingOutput(PingStatus.OK);
			pingOutput.addElement(PingElement.ok(operation));
		}catch (Exception e){
			pingLog.logFailedPing(ctx.getServiceUUID(), operation.getCode() + " failed with error: " + e.getMessage(),
					e.getClass().getName(), ctx.getServiceCallLog().getCallUUID());
			pingOutput = new PingOutput(PingStatus.ERROR);
		}

		return pingOutput;
	}

}
