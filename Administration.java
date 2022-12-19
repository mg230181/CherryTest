package com.systematic.kombit.administration;

import com.systematic.kombit.invoicehandling.client.persistence.dto.SFTPInvoiceSourceDTO;
import com.systematic.kombit.oir.configuration.entities.OiRConfigurationEntity;
import com.systematic.kombit.ping.Pingable;
import com.systematic.kombit.serviceconfiguration.dto.ServiceDescriptionDTO;

import java.util.List;
import java.util.Map;

/**
 * This interface defines the interface for the administering the connection and service agreements
 */
public interface Administration extends Pingable {
	List<Map<String, String>> getServices();

	String getServiceEntityID(String serviceUUID);

	OiRConfigurationEntity getOiRConfiguration(String userUUID);
	OiRConfigurationEntity saveOiRConfiguration(OiRConfigurationEntity configuration);
	SFTPInvoiceSourceDTO getSftpInvSource(String userUUID);
	void saveSftpInvSource(SFTPInvoiceSourceDTO dto);

    void editServiceDescription(ServiceDescriptionDTO dto);
}
