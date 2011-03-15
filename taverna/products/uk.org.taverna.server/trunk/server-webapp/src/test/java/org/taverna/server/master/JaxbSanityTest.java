/*
 * Copyright (C) 2010-2011 The University of Manchester
 * 
 * See the file "LICENSE.txt" for license terms.
 */
package org.taverna.server.master;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.junit.Before;
import org.junit.Test;
import org.taverna.server.master.common.Credential;
import org.taverna.server.master.common.DirEntryReference;
import org.taverna.server.master.common.InputDescription;
import org.taverna.server.master.common.Permission;
import org.taverna.server.master.common.RunReference;
import org.taverna.server.master.common.Status;
import org.taverna.server.master.common.Trust;
import org.taverna.server.master.common.Uri;
import org.taverna.server.master.common.Workflow;
import org.taverna.server.master.rest.DirectoryContents;
import org.taverna.server.master.rest.ListenerDefinition;
import org.taverna.server.master.rest.MakeOrUpdateDirEntry;
import org.taverna.server.master.rest.TavernaServerInputREST.InDesc;
import org.taverna.server.master.rest.TavernaServerInputREST.InputsDescriptor;
import org.taverna.server.master.rest.TavernaServerListenersREST.ListenerDescription;
import org.taverna.server.master.rest.TavernaServerListenersREST.Listeners;
import org.taverna.server.master.rest.TavernaServerListenersREST.Properties;
import org.taverna.server.master.rest.TavernaServerListenersREST.PropertyDescription;
import org.taverna.server.master.rest.TavernaServerREST.PermittedListeners;
import org.taverna.server.master.rest.TavernaServerREST.PermittedWorkflows;
import org.taverna.server.master.rest.TavernaServerREST.PolicyView.PolicyDescription;
import org.taverna.server.master.rest.TavernaServerREST.RunList;
import org.taverna.server.master.rest.TavernaServerREST.ServerDescription;
import org.taverna.server.master.rest.TavernaServerRunREST.RunDescription;
import org.taverna.server.master.rest.TavernaServerSecurityREST;
import org.taverna.server.master.soap.PermissionList;

/**
 * This test file ensures that the JAXB bindings will work once deployed instead
 * of mysteriously failing in service.
 * 
 * @author Donal Fellows
 */
public class JaxbSanityTest {
	SchemaOutputResolver sink;
	StringWriter schema;

	String schema() {
		return schema.toString();
	}

	@Before
	public void init() {
		schema = new StringWriter();
		sink = new SchemaOutputResolver() {
			@Override
			public Result createOutput(String namespaceUri,
					String suggestedFileName) throws IOException {
				StreamResult sr = new StreamResult(schema);
				sr.setSystemId("/dev/null");
				return sr;
			}
		};
		assertEquals("", schema());
	}

	private boolean printSchema = false;

	private void testJAXB(Class<?>... classes) throws Exception {
		JAXBContext.newInstance(classes).generateSchema(sink);
		if (printSchema)
			System.out.println(schema());
		assertTrue(schema().length() > 0);
	}

	@Test
	public void testJAXBForDirEntryReference() throws Exception {
		JAXBContext.newInstance(DirEntryReference.class).generateSchema(sink);
		assertTrue(schema().length() > 0);
	}

	@Test
	public void testJAXBForInputDescription() throws Exception {
		testJAXB(InputDescription.class);
	}

	@Test
	public void testJAXBForRunReference() throws Exception {
		testJAXB(RunReference.class);
	}

	@Test
	public void testJAXBForWorkflow() throws Exception {
		testJAXB(Workflow.class);
	}

	@Test
	public void testJAXBForStatus() throws Exception {
		testJAXB(Status.class);
	}

	@Test
	public void testJAXBForUri() throws Exception {
		testJAXB(Uri.class);
	}

	@Test
	public void testJAXBForDirectoryContents() throws Exception {
		testJAXB(DirectoryContents.class);
	}

	@Test
	public void testJAXBForListenerDefinition() throws Exception {
		testJAXB(ListenerDefinition.class);
	}

	@Test
	public void testJAXBForMakeOrUpdateDirEntry() throws Exception {
		testJAXB(MakeOrUpdateDirEntry.class);
	}

	@Test
	public void testJAXBForInDesc() throws Exception {
		testJAXB(InDesc.class);
	}

	@Test
	public void testJAXBForInputsDescriptor() throws Exception {
		testJAXB(InputsDescriptor.class);
	}

	@Test
	public void testJAXBForListenerDescription() throws Exception {
		testJAXB(ListenerDescription.class);
	}

	@Test
	public void testJAXBForListeners() throws Exception {
		testJAXB(Listeners.class);
	}

	@Test
	public void testJAXBForProperties() throws Exception {
		testJAXB(Properties.class);
	}

	@Test
	public void testJAXBForPropertyDescription() throws Exception {
		testJAXB(PropertyDescription.class);
	}

	@Test
	public void testJAXBForPermittedListeners() throws Exception {
		testJAXB(PermittedListeners.class);
	}

	@Test
	public void testJAXBForPermittedWorkflows() throws Exception {
		testJAXB(PermittedWorkflows.class);
	}

	@Test
	public void testJAXBForServerDescription() throws Exception {
		testJAXB(ServerDescription.class);
	}

	@Test
	public void testJAXBForRunDescription() throws Exception {
		testJAXB(RunDescription.class);
	}

	@Test
	public void testJAXBForRunList() throws Exception {
		testJAXB(RunList.class);
	}

	@Test
	public void testJAXBForPolicyDescription() throws Exception {
		testJAXB(PolicyDescription.class);
	}

	@Test
	public void testJAXBForSecurityCredential() throws Exception {
		testJAXB(Credential.class);
	}

	@Test
	public void testJAXBForSecurityCredentialList() throws Exception {
		testJAXB(TavernaServerSecurityREST.CredentialList.class);
	}

	@Test
	public void testJAXBForSecurityTrust() throws Exception {
		testJAXB(Trust.class);
	}

	@Test
	public void testJAXBForSecurityTrustList() throws Exception {
		testJAXB(TavernaServerSecurityREST.TrustList.class);
	}

	@Test
	public void testJAXBForPermission() throws Exception {
		testJAXB(Permission.class);
	}

	@Test
	public void testJAXBForSecurityPermissionDescription() throws Exception {
		testJAXB(TavernaServerSecurityREST.PermissionDescription.class);
	}

	@Test
	public void testJAXBForSecurityPermissionsDescription() throws Exception {
		testJAXB(TavernaServerSecurityREST.PermissionsDescription.class);
	}

	@Test
	public void testJAXBForSecurityDescriptor() throws Exception {
		testJAXB(TavernaServerSecurityREST.Descriptor.class);
	}

	@Test
	public void testJAXBForPermissionList() throws Exception {
		testJAXB(PermissionList.class);
	}

	@Test
	public void testJAXBForEverythingAtOnce() throws Exception {
		// printSchema = true;
		testJAXB(DirEntryReference.class, InputDescription.class,
				RunReference.class, Workflow.class, Status.class,
				DirectoryContents.class, InDesc.class,
				ListenerDefinition.class, MakeOrUpdateDirEntry.class,
				InputsDescriptor.class, ListenerDescription.class,
				Listeners.class, Properties.class, PropertyDescription.class,
				PermittedListeners.class, PermittedWorkflows.class,
				ServerDescription.class, RunDescription.class, Uri.class,
				RunList.class, PolicyDescription.class, PermissionList.class,
				Credential.class, Trust.class,
				TavernaServerSecurityREST.CredentialList.class,
				TavernaServerSecurityREST.TrustList.class, Permission.class,
				TavernaServerSecurityREST.Descriptor.class,
				TavernaServerSecurityREST.PermissionDescription.class,
				TavernaServerSecurityREST.PermissionsDescription.class);
	}
}
