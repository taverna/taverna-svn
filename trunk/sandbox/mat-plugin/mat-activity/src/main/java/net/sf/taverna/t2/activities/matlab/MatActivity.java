package net.sf.taverna.t2.activities.matlab;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.sf.taverna.matlabactivity.matserver.api.MatArray;
import net.sf.taverna.matlabactivity.matserver.api.MatEngine;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

/**
 * An Activity providing Matlab Engine access functionality.
 * @author petarj
 */
public class MatActivity extends AbstractAsynchronousActivity<MatActivityConfigurationBean> {

    private MatActivityConfigurationBean configurationBean;
    //private MatEngine engine;
    private MatServerConnectionManager connectionManager;

    public MatActivity() {
        connectionManager = new MatServerConnectionManager();
        MatActivityConnectionSettings connectionSettings=new MatActivityConnectionSettings();
        connectionSettings.setHost("localhost");
        connectionSettings.setPort(MatActivityConnectionSettings.DEFAULT_PORT);
        
        connectionManager.configure(connectionSettings);
    }

    @Override
    public void configure(MatActivityConfigurationBean conf) throws
            ActivityConfigurationException {
        this.configurationBean = conf;
        configurePorts(configurationBean);
    }

    @Override
    public MatActivityConfigurationBean getConfiguration() {
        return configurationBean;
    }

    @Override
    public void executeAsynch(final Map<String, T2Reference> data,
            final AsynchronousActivityCallback callback) {
        callback.requestRun(new Runnable() {

            public void run() {
                try {
                    //synchronized (engine) {
                        ReferenceService referenceService = callback.getContext().
                                getReferenceService();
                        Map<String, T2Reference> outputData = new HashMap<String, T2Reference>();
                        
                        MatEngine engine = connectionManager.getEngine();

                        for (String inputName : data.keySet()) {
                            ActivityInputPort inputPort = getInputPort(inputName);
                            MatArray input = (MatArray) referenceService.
                                    renderIdentifier(data.get(inputName),
                                    inputPort.getTranslatedElementClass(),
                                    callback.getContext());

                            engine.setVar(inputName, input);
                        }
                        ArrayList<String> outputNamesList = new ArrayList<String>();
                        for (OutputPort outputPort : getOutputPorts()) {
                            outputNamesList.add(outputPort.getName());
                        }

                        engine.setOutputNames(outputNamesList.toArray(
                                new String[]{}));

                        engine.execute(configurationBean.getSctipt());
                        Map<String, MatArray> outs = engine.getOutputVars();

                        for (OutputPort outputPort : getOutputPorts()) {
                            String name = outputPort.getName();
                            Object value = outs.get(name);

                            if (value != null) {
                                outputData.put(name, referenceService.register(
                                        value,
                                        outputPort.getDepth(), true,
                                        callback.getContext()));
                            }
                        }

                        engine.clearVars();
                        callback.receiveResult(outputData, new int[0]);
                    //}
                } catch (ReferenceServiceException referenceServiceException) {
                    callback.fail("Error accessing input/output data",
                            referenceServiceException);
                } catch (MalformedURLException ex) {
                    callback.fail("Error accessing engine", ex);
                }
            }
        });
    }

    private ActivityInputPort getInputPort(String name) {
        for (ActivityInputPort port : getInputPorts()) {
            if (port.getName().equals(name)) {
                return port;
            }
        }
        return null;
    }
}
