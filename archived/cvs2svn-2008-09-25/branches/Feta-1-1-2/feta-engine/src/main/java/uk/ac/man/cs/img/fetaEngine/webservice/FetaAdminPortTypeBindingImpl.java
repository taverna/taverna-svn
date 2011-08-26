/*
 *
 * Copyright (C) 2006 The University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 */

package uk.ac.man.cs.img.fetaEngine.webservice;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import java.util.Set;
import java.util.HashSet;

import uk.ac.man.cs.img.fetaEngine.store.IFetaModel;
import uk.ac.man.cs.img.fetaEngine.store.FetaEngineException;
import uk.ac.man.cs.img.fetaEngine.store.FetaEngineProperties;
import org.apache.log4j.Logger;

public class FetaAdminPortTypeBindingImpl implements uk.ac.man.cs.img.fetaEngine.webservice.FetaAdminPortType{


    private IFetaModel feta;
    static Logger logger = Logger.getLogger(FetaPortTypeBindingImpl.class);
    FetaEngineProperties props;
    ////constructor
    
    public FetaAdminPortTypeBindingImpl() throws RemoteException {
            super();

            System.out.println("Debug in Feta Admin Port Type");
            logger.debug("Instantiated Admin Binding Implementation");

            // get some system properties
            try {
                 props = new FetaEngineProperties();
            }catch (FetaEngineException ex) {
                throw new RemoteException("Problem loading configuration", ex);
            }catch (Exception e) {
                throw new RemoteException("Problem loading configuration", e);
            }


            try {
                    //load the provider info from properties file and instantiate
                    String className = props.getPropertyValue("fetaEngine.backend.provider","uk.ac.man.cs.img.fetaEngine.store.JenaModelImpl"); 
                    System.out.println("Provider Class name is "+className);
                    feta = (IFetaModel) Class.forName(className).getDeclaredMethod("getInstance", new Class[] {}).invoke(null, new Object[] {});
            } catch (ClassNotFoundException e) {
                    throw new RemoteException("Problem connecting to fetaEngine", e);
            } catch (InvocationTargetException e) {
                    throw new RemoteException("Problem connecting to fetaEngine", e);
            } catch (NoSuchMethodException e) {
                    throw new RemoteException("Problem connecting to fetaEngine", e);
            } catch (IllegalAccessException e) {
                    throw new RemoteException("Problem connecting to fetaEngine", e);
            }


    }


    public java.lang.String getStoreContent() throws java.rmi.RemoteException {
        IFetaModel fetta = getStore();
        String contents;
        try {

                contents = fetta.getStoreContent();

         } catch (Exception e) {
                    e.printStackTrace();
                    throw new RemoteException("problem during refreshing the Feta engine", e);
         }

        return contents;
    }


     public IFetaModel getStore() throws RemoteException {
	return feta;
	}


}
