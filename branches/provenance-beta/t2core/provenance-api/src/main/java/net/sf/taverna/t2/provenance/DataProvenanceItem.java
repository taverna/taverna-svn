package net.sf.taverna.t2.provenance;

import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;

import org.jdom.Element;

public abstract class DataProvenanceItem implements ProvenanceItem {

	private Map<String, T2Reference> dataMap;
	private ReferenceService referenceService;

	protected abstract boolean isInput();

	public DataProvenanceItem(Map<String, T2Reference> dataMap) {
		super();
		this.dataMap = dataMap;
	}

	public Element getAsXML(ReferenceService referenceService) {
		this.referenceService = referenceService;
		String name = isInput() ? "inputdata" : "outputdata";
		Element result = new Element(name);
		for (String port : dataMap.keySet()) {
			Element portElement = new Element("port");
			portElement.setAttribute("name", port);
			result.addContent(portElement);
			referenceService.renderIdentifier(dataMap.get(port), Object.class, null);
			portElement.addContent(resolveToElement(dataMap.get(port)));
			Element element = new Element("some_stuff");
			portElement.addContent(element);
		}
		return result;
	}

	public Map<String, T2Reference> getDataMap() {
		return dataMap;
	}
	
	/**
	 * Given a {@link T2Reference} return all the other {@link T2Reference}s which
	 * it contains as an XML Element
	 * 
	 * @param entityIdentifier
	 * @return
	 * @throws NotFoundException
	 * @throws RetrievalException
	 */
	public org.jdom.Element resolveToElement(T2Reference reference) {

		org.jdom.Element element = new org.jdom.Element("resolvedReference");
			if (reference.getReferenceType().equals(
				T2ReferenceType.ErrorDocument)) {
				ErrorDocument error = referenceService.getErrorDocumentService().getError(reference);
			
			element.setName("error");
			element.setAttribute("id", reference.toString());
			org.jdom.Element messageElement = new org.jdom.Element("message");
			messageElement.addContent(error.getExceptionMessage());
			element.addContent(messageElement);
		} else if (reference.getReferenceType().equals(
				T2ReferenceType.ReferenceSet)) {
			element.setName("referenceSet");
			element.setAttribute("id", reference.toString());
			ReferenceSet referenceSet = referenceService.getReferenceSetService().getReferenceSet(reference);
			Set<ExternalReferenceSPI> externalReferences = referenceSet.getExternalReferences();
			for (ExternalReferenceSPI externalReference:externalReferences) {
				//FIXME does this make sense?
				org.jdom.Element refElement = new org.jdom.Element("reference");
				refElement.addContent(externalReference.getDataNature().toString());
				element.addContent(refElement);
			}
			
		} else if (reference.getReferenceType().equals(
				T2ReferenceType.IdentifiedList)) {
			IdentifiedList<T2Reference> list = referenceService.getListService().getList(reference);
						
			element.setName("list");
			element.setAttribute("id", reference.toString());
			for (T2Reference ref:list) {
				element.addContent(resolveToElement(ref));
			}
		} else {
			// throw something (maybe a tantrum)
		}
		return element;
	}
	
	public String getAsString() {
		return null;
	}

	public void setDataMap(Map<String, T2Reference> dataMap) {
		this.dataMap = dataMap;
	}

}
