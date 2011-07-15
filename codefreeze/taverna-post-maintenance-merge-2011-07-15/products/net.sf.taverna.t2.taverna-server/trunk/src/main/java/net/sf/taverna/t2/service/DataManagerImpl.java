/*******************************************************************************
 * Copyright (C) 2008 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.StackTraceElementBean;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.service.model.Data;
import net.sf.taverna.t2.service.store.DataDao;
import net.sf.taverna.t2.service.webservice.resource.DataValue;
import net.sf.taverna.t2.service.webservice.resource.ErrorValue;
import net.sf.taverna.t2.service.webservice.resource.ErrorTrace;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class DataManagerImpl implements DataManager, InitializingBean {

	private DataDao dataDao;

	private AuthorizationManager authorizationManager;

	private ReferenceService referenceService;

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(dataDao, "dataDao required");
		Assert.notNull(referenceService, "referenceService required");
	}

	public void addData(final Data data) {
		dataDao.save(data);
		if (authorizationManager != null) {
			authorizationManager.createAclEntry(data);
		}
	}

	public void deleteData(Long id) {
		dataDao.delete(id);
		if (authorizationManager != null) {
			authorizationManager.deleteAclEntry(Data.class, id);
		}
	}

	public Data getData(Long id) {
		return dataDao.get(id);
	}

	public Collection<Data> getAllData() {
		return dataDao.getAll();
	}

	public Data createData(Map<String, T2Reference> referenceMap) {
		Data data = new Data();
		data.setReferenceMap(referenceMap);
		addData(data);
		return data;
	}

	public Map<String, T2Reference> registerData(Map<String, DataValue> data) {
		Map<String, T2Reference> referenceMap = new HashMap<String, T2Reference>();
		for (Entry<String, DataValue> entry : data.entrySet()) {
			referenceMap.put(entry.getKey(), registerValue(entry.getValue()));
		}
		return referenceMap;
	}

	private T2Reference registerValue(DataValue value) {
		T2Reference t2Reference = null;
		if (value.depth() == 0) {
			t2Reference = referenceService.register(value.getValue(), 0, true,
					null);
		} else {
			List<T2Reference> list = new ArrayList<T2Reference>();
			for (DataValue element : value.getList()) {
				list.add(registerValue(element));
			}
			t2Reference = referenceService.register(list, value.depth(), true,
					null);
		}
		return t2Reference;
	}

	public Map<String, DataValue> dereferenceData(
			Map<String, T2Reference> outputs) {
		Map<String, DataValue> dataMap = new HashMap<String, DataValue>();
		for (Entry<String, T2Reference> entry : outputs.entrySet()) {
			dataMap.put(entry.getKey(), dereferenceValue(entry.getValue()));
		}
		return dataMap;
	}

	@SuppressWarnings("unchecked")
	private DataValue dereferenceValue(T2Reference t2Reference) {
		DataValue value = new DataValue();
		Identified identified = referenceService.resolveIdentifier(t2Reference,
				null, null);
		value.setContainsError(t2Reference.containsErrors());
		if (identified instanceof ErrorDocument) {
			value.setErrorValue(dereferenceError((ErrorDocument) identified));
		} else if (identified instanceof IdentifiedList) {
			value.setList(dereferenceList((IdentifiedList) identified));
		} else {
			value.setValue(referenceService.renderIdentifier(
					identified.getId(), String.class, null));
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	private List<DataValue> dereferenceList(IdentifiedList identifiedList) {
		List<DataValue> valueList = new ArrayList<DataValue>();
		for (Object element : identifiedList) {
			DataValue value = null;
			if (element instanceof IdentifiedList) {
				IdentifiedList list = (IdentifiedList) element;
				value = new DataValue(dereferenceList(list));
				value.setContainsError(list.getId().containsErrors());
			} else if (element instanceof Identified) {
				Identified identified = (Identified) element;
				value = dereferenceValue(identified.getId());
			}
			valueList.add(value);
		}
		return valueList;
	}

	private ErrorValue dereferenceError(ErrorDocument errorDocument) {
		ErrorValue errorValue = new ErrorValue();
		errorValue.setExceptionMessage(errorDocument.getExceptionMessage());
		errorValue.setMessage(errorDocument.getMessage());
		List<ErrorTrace> stackTrace = new ArrayList<ErrorTrace>();
		for (StackTraceElementBean element : errorDocument
				.getStackTraceStrings()) {
			stackTrace.add(new ErrorTrace(element.getClassName(),
					element.getMethodName(), element.getFileName(), element
							.getLineNumber()));
		}
		errorValue.setStackTrace(stackTrace);
		return errorValue;
	}

	public void setDataDao(DataDao dataDao) {
		this.dataDao = dataDao;
	}

	public void setReferenceService(ReferenceService referenceService) {
		this.referenceService = referenceService;
	}

	public void setAuthorizationManager(
			AuthorizationManager authorizationManager) {
		this.authorizationManager = authorizationManager;
	}

}
