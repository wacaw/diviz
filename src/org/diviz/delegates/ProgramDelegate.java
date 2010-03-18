// LICENSE
/**
 * 
 */
package org.diviz.delegates;

import org.jdom.Element;

import eu.telecom_bretagne.praxis.common.InvalidXMLException;
import eu.telecom_bretagne.praxis.common.Log;
import eu.telecom_bretagne.praxis.common.RemoteComponents;
import eu.telecom_bretagne.praxis.core.resource.ProgramDescription;
import eu.telecom_bretagne.praxis.core.workflow.Program;
import eu.telecom_bretagne.praxis.core.workflow.Workflow.XMLWarnings;

;

/**
 * @author Sébastien Bigaret
 */
public class ProgramDelegate
    extends eu.telecom_bretagne.praxis.core.workflow.ProgramDelegate
{
	@Override
	public ProgramDescription findAlternateProgramDescription(String prgDescriptionId, Element xmlNode,
	                                                          XMLWarnings warnings, InvalidXMLException xmlExc)
	        throws InvalidXMLException
	{
		ProgramDescription p = super.findAlternateProgramDescription(prgDescriptionId, xmlNode, warnings, xmlExc);
		if (p != null)
			return p;
		if (!prgDescriptionId.startsWith("DecisionDeck"))
			return null;
		
		// All programs' providers were formerly identical: DecisionDeck
		// it's time to find the corresponding ones
		
		// 1st, adapt those whose name has changed
		final String[] provider_service_version = prgDescriptionId.split(ProgramDescription.ID_SEPARATOR);
		if (provider_service_version.length != 3)
		{
			Log.log.severe("Request for a invalid id: " + prgDescriptionId);
			return null;
		}
		
		String name = provider_service_version[1];
		if ("ACUTAR".equals(name))
			name = "ACUTA";
		else
			if ("jsmaa".equals(name))
				name = "smaa2";
		
		// 2nd, let's find the corresponding description, i.e. simply one that as the same name:
		// when the services'providers are renamed from DecisionDeck, none of them had the same name; hopefully all
		// the users' workflows will be migrated before this happens.
		String[] declaredResources = RemoteComponents.resourceRepository().getResources();
		for (String declaredResource: declaredResources)
		{
			final String[] dR_p_s_v = declaredResource.split(ProgramDescription.ID_SEPARATOR);
			if (name.equals(dR_p_s_v[1]))
			{
				warnings.add(XMLWarnings.PRG_DESC_REPLACED_BY_ALTERNATIVE, new String[] {
				        xmlNode.getAttributeValue(Program.PRG_INFO), declaredResource });
				return RemoteComponents.resourceRepository().programDescriptionForId(declaredResource);
			}
		}
		return null;
	}
}
