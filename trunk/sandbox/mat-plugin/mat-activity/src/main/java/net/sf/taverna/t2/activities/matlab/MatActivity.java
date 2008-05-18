package net.sf.taverna.t2.activities.matlab;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.taverna.t2.cloudone.datamanager.EmptyListException;
import net.sf.taverna.t2.cloudone.datamanager.MalformedListException;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.UnsupportedObjectTypeException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

/**
 * An Activity providing Matlab Engine access functionality.
 * @author petarj
 */
public class MatActivity extends AbstractAsynchronousActivity<MatActivityConfigurationBean>
{

    private MatActivityConfigurationBean configurationBean;

    public MatActivity()
    {
    }

    @Override
    public void configure(MatActivityConfigurationBean conf) throws ActivityConfigurationException
    {
        this.configurationBean = conf;
        configurePorts(configurationBean);
    }

    @Override
    public MatActivityConfigurationBean getConfiguration()
    {
        return this.configurationBean;
    }

    @Override
    public void executeAsynch(final Map<String, EntityIdentifier> data, final AsynchronousActivityCallback callback)
    {
        callback.requestRun(new Runnable()
        {

            DataFacade dataFacade = new DataFacade(callback.getContext().getDataManager());
            Map<String, EntityIdentifier> outputData = new HashMap<String, EntityIdentifier>();

            public void run()
            {
                try
                {
                    Object exampleInput = dataFacade.resolve(data.get("example_input"), String.class);

                    String exampleOutput = exampleInput + "_example";

                    outputData.put("example_output", dataFacade.register(exampleOutput));

                    callback.receiveResult(outputData, new int[0]);

                } catch (EmptyListException ex)
                {
                    Logger.getLogger(MatActivity.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MalformedListException ex)
                {
                    Logger.getLogger(MatActivity.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnsupportedObjectTypeException ex)
                {
                    Logger.getLogger(MatActivity.class.getName()).log(Level.SEVERE, null, ex);
                } catch (RetrievalException ex)
                {
                    Logger.getLogger(MatActivity.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NotFoundException ex)
                {
                    Logger.getLogger(MatActivity.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
    }
}
